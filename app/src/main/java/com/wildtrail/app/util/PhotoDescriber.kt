package com.wildtrail.app.util

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Lightweight, on-device "what's in this photo?" describer powered by
 * Google ML Kit Image Labeling.
 *
 *  - Bundled model — labels ~400 common entities (animals, plants,
 *    monuments, everyday objects) with confidence scores.
 *  - Free, no API key, no network — runs entirely on the device, so it
 *    works on the trail with no signal.
 *  - The model is ~3 MB shipped with the APK.
 *
 * Not a chat LLM — it can't write paragraphs — but it perfectly fits the
 * "tell me roughly what this photo is" use-case while respecting the
 * "no paid API keys, lightweight, runs on a phone" constraint.
 */
class PhotoDescriber {

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.55f)
                .build(),
        )
    }

    /**
     * Inspect [file] and return a short human-readable description of
     * what's in it. Falls back to a friendly placeholder when the labeler
     * has nothing confident to say.
     */
    suspend fun describe(file: File): String = withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: return@withContext "Couldn't read this photo"
        val image = InputImage.fromBitmap(bitmap, 0)
        val labels = labeler.process(image).await()
        formatLabels(labels)
    }

    private fun formatLabels(labels: List<ImageLabel>): String {
        if (labels.isEmpty()) return "Nothing obvious detected — could be a scene or distant view."
        val top = labels.sortedByDescending { it.confidence }
            .take(3)
            .joinToString(", ") { it.text }
        return "Looks like: $top"
    }
}
