package com.wildtrail.app.util

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Stores user-captured photos and audio notes for hikes in the app's
 * private internal storage (`filesDir/hike_media/`). Files in this
 * directory are readable by the app forever and need no extra permission
 * — they're scoped to the app sandbox.
 *
 * Files are intentionally **local-only**: their absolute path is the
 * stable handle we save on the hike, and we don't upload them anywhere.
 * Keeping things local sidesteps Firebase Storage rules / quota and makes
 * the feature work fully offline.
 */
class HikeMediaStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, MEDIA_DIR).apply { mkdirs() }

    /** Returns the absolute path under which a freshly recorded audio clip
     *  should be written. The recorder writes the file itself. */
    fun newAudioFile(): File = File(dir, "audio_${System.currentTimeMillis()}.m4a")

    /** Save the given bitmap as a JPEG and return the file. */
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
