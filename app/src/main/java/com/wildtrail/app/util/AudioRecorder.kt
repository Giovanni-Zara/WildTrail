package com.wildtrail.app.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Thin wrapper around [MediaRecorder] for recording short voice notes during
 * a hike. Output is AAC inside an MP4 container (`.m4a`) — well-supported by
 * Android's built-in [android.media.MediaPlayer] for playback.
 *
 * Lifecycle: call [start] once, then [stop] once. The instance is single-use;
 * create a new one for the next note.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /**
     * Begin recording into [target]. Caller must hold the
     * `android.permission.RECORD_AUDIO` runtime permission.
     */
    fun start(target: File) {
        @Suppress("DEPRECATION")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        try {
            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96_000)
                setAudioSamplingRate(44_100)
                setOutputFile(target.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            outputFile = target
        } catch (t: Throwable) {
            // Make sure the native MediaRecorder resource isn't leaked if
            // prepare/start failed — e.g. permission revoked at runtime.
            runCatching { rec.release() }
            throw t
        }
    }

    /** Stop the in-flight recording and return the file it wrote, or null
     *  if [start] was never called. Safe to call from any thread. */
    fun stop(): File? {
        val rec = recorder ?: return null
        runCatching {
            rec.stop()
        }
        runCatching { rec.release() }
        recorder = null
        val file = outputFile
        outputFile = null
        return file
    }
}
