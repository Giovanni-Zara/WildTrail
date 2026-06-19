package com.wildtrail.app.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

/** A single bird identified in a recording, with the model's confidence. */
data class BirdDetection(
    val commonName: String,
    val scientificName: String,
    /** 0..1 — the highest confidence this species reached across all windows. */
    val confidence: Float,
)

/**
 * On-device bird-sound classifier powered by **BirdNET** running through
 * TensorFlow Lite — the audio counterpart to [PhotoDescriber]. Runs entirely
 * on the phone (no network, no API key) so it works on the trail.
 *
 * Pipeline (per call to [detect]):
 *  1. Decode the recording to mono 48 kHz float PCM ([AudioPcmDecoder]).
 *  2. Slide a 3-second window over the signal; BirdNET expects exactly one
 *     window (≈144 000 samples) per inference.
 *  3. For each window, run the model and (optionally) sigmoid the logits into
 *     0..1 confidences, keeping the running **max per species**.
 *  4. Return the highest-confidence species above [MIN_CONFIDENCE].
 *
 * ## Model assets (you must add these — they are NOT in the repo)
 * BirdNET's model + labels are CC BY-NC-SA 4.0 (non-commercial). Download from
 * the BirdNET-Analyzer project and drop them in:
 *   - `app/src/main/assets/birdnet/model.tflite`   (the FP32 GLOBAL model)
 *   - `app/src/main/assets/birdnet/labels.txt`     (one `Scientific_Common` per line)
 * See `assets/birdnet/README.txt`.
 *
 * The class is created once (held by the DI container) so the interpreter and
 * its memory-mapped model stay loaded between taps. [Interpreter] is not
 * thread-safe, so inference is serialised with [mutex].
 */
class BirdNetClassifier(private val context: Context) {

    private val mutex = Mutex()

    /**
     * Whether the model + labels are actually bundled under `assets/birdnet/`.
     * Lets the UI tell "you haven't installed the model" apart from a genuine
     * analysis failure, instead of blaming the recording.
     */
    fun isModelInstalled(): Boolean = runCatching {
        val dir = MODEL_ASSET.substringBeforeLast('/')
        val names = context.assets.list(dir).orEmpty()
        MODEL_ASSET.substringAfterLast('/') in names &&
            LABELS_ASSET.substringAfterLast('/') in names
    }.getOrDefault(false)

    private val interpreter: Interpreter by lazy {
        Interpreter(loadModelAsset(), Interpreter.Options().apply { setNumThreads(4) })
    }

    /** Labels file, one entry per output class, in `Scientific_Common` form. */
    private val labels: List<String> by lazy {
        context.assets.open(LABELS_ASSET).bufferedReader().useLines { seq ->
            seq.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    }

    /**
     * Analyse [file] and return the top [topN] detected species (highest
     * confidence first). Empty when nothing clears [MIN_CONFIDENCE] or the file
     * has no decodable audio. Throws if the model assets are missing — callers
     * should wrap in runCatching.
     */
    suspend fun detect(file: File, topN: Int = 5): List<BirdDetection> =
        withContext(Dispatchers.Default) {
            try {
                detectInternal(file, topN)
            } catch (t: Throwable) {
                // Surface the real cause in logcat (filter by tag "BirdNet") —
                // the UI only shows a generic message.
                Log.e(TAG, "BirdNET analysis failed for ${file.name}", t)
                throw t
            }
        }

    private suspend fun detectInternal(file: File, topN: Int): List<BirdDetection> {
        val samples = AudioPcmDecoder.decodeToMonoFloat(file, SAMPLE_RATE)
        if (samples.isEmpty()) return emptyList()

        return mutex.withLock {
                // Read the real shapes from the model so we don't hard-code them.
                val windowSize = interpreter.getInputTensor(0).shape().last()
                val numClasses = interpreter.getOutputTensor(0).shape().last()
                val usableClasses = minOf(numClasses, labels.size)

                val maxConfidence = FloatArray(numClasses)
                val input = ByteBuffer
                    .allocateDirect(windowSize * 4)
                    .order(ByteOrder.nativeOrder())
                val output = Array(1) { FloatArray(numClasses) }

                var start = 0
                while (start < samples.size) {
                    input.clear()
                    for (i in 0 until windowSize) {
                        val s = start + i
                        input.putFloat(if (s < samples.size) samples[s] else 0f)
                    }
                    input.rewind()

                    interpreter.run(input, output)

                    val row = output[0]
                    for (c in 0 until numClasses) {
                        val conf = if (APPLY_SIGMOID) sigmoid(row[c]) else row[c]
                        if (conf > maxConfidence[c]) maxConfidence[c] = conf
                    }

                    // A clip shorter than one window is analysed once (padded).
                    if (samples.size <= windowSize) break
                    start += windowSize // bump to windowSize / 2 for 50% overlap
                }

                (0 until usableClasses)
                    .asSequence()
                    .map { idx -> idx to maxConfidence[idx] }
                    .filter { it.second >= MIN_CONFIDENCE }
                    .sortedByDescending { it.second }
                    .take(topN)
                    .map { (idx, conf) -> toDetection(labels[idx], conf) }
                    .toList()
            }
        }

    private fun toDetection(label: String, confidence: Float): BirdDetection {
        // BirdNET labels look like "Cyanocitta cristata_Blue Jay".
        val parts = label.split('_', limit = 2)
        return if (parts.size == 2) {
            BirdDetection(commonName = parts[1].trim(), scientificName = parts[0].trim(), confidence = confidence)
        } else {
            BirdDetection(commonName = label, scientificName = "", confidence = confidence)
        }
    }

    private fun loadModelAsset(): MappedByteBuffer {
        val fd = context.assets.openFd(MODEL_ASSET)
        FileInputStream(fd.fileDescriptor).use { fis ->
            return fis.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    private fun sigmoid(x: Float): Float = 1f / (1f + exp(-x))

    private companion object {
        const val TAG = "BirdNet"

        /** BirdNET's required input sample rate. */
        const val SAMPLE_RATE = 48_000
        const val MODEL_ASSET = "birdnet/model.tflite"
        const val LABELS_ASSET = "birdnet/labels.txt"

        /** Hide low-confidence noise; ~0.1 is BirdNET's usual floor. */
        const val MIN_CONFIDENCE = 0.10f

        /**
         * BirdNET's TFLite model emits logits, so we sigmoid them into 0..1.
         * If your exported variant already applies the activation (confidences
         * look squashed around 0.5–0.7), set this to false.
         */
        const val APPLY_SIGMOID = true
    }
}
