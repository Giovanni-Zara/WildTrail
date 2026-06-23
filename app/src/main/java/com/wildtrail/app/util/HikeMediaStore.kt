package com.wildtrail.app.util

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class HikeMediaStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, MEDIA_DIR).apply { mkdirs() }

    fun newAudioFile(): File = File(dir, "audio_${System.currentTimeMillis()}.m4a")

    suspend fun savePhoto(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val target = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        target
    }

    private companion object {
        const val MEDIA_DIR = "hike_media"
        const val JPEG_QUALITY = 88
    }
}
