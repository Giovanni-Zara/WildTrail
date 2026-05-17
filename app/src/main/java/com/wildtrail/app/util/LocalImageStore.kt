package com.wildtrail.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Copies user-picked images into app-owned internal storage.
 *
 * The Android Photo Picker hands back a `content://` URI carrying only a
 * *temporary* read grant tied to the calling activity. Persisting that raw
 * URI is unsafe: once the grant is revoked (or the process restarts) Coil
 * can no longer read it. We therefore copy the bytes into our own
 * `filesDir`, which we can read forever — enabling the offline-first
 * "show the picture immediately" path while a background upload runs.
 *
 * Files are named `profile_<uid>_<millis>.jpg`. The timestamp doubles as a
 * cache-buster: Coil keys its memory/disk cache by the model string, so a
 * fresh filename guarantees the new picture is shown rather than a stale
 * cached bitmap from the previous one. Older files for the same uid are
 * pruned so we don't leak storage.
 */
open class LocalImageStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, PROFILE_DIR).apply { mkdirs() }

    /**
     * Copy [srcUri] into internal storage and return the resulting file.
     * Throws if the source can't be read so the caller can fall back.
     */
    open suspend fun saveProfilePicture(uid: String, srcUri: Uri): File =
        withContext(Dispatchers.IO) {
            val target = File(dir, "profile_${uid}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(srcUri).use { input ->
                requireNotNull(input) { "Cannot open input stream for $srcUri" }
                target.outputStream().use { output -> input.copyTo(output) }
            }
            pruneOldPictures(uid, keep = target.name)
            target
        }

    private fun pruneOldPictures(uid: String, keep: String) {
        dir.listFiles()
            ?.filter { it.name.startsWith("profile_${uid}_") && it.name != keep }
            ?.forEach { it.delete() }
    }

    private companion object {
        const val PROFILE_DIR = "profile_pictures"
    }
}
