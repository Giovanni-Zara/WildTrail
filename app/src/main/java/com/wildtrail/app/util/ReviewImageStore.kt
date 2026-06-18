package com.wildtrail.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Copies user-picked review photos into app-owned internal storage.
 *
 * Same rationale as [LocalImageStore]: the Android Photo Picker hands back
 * `content://` URIs carrying only a *temporary* read grant tied to the
 * calling activity. Persisting those raw URIs is unsafe — once the grant is
 * revoked (or the process restarts) Coil can no longer read them. We copy the
 * bytes into our own `filesDir/review_images/<reviewId>/` instead, which we
 * can read forever. That enables the offline-first "show the photos
 * immediately" path while a background Storage upload swaps in the
 * cross-device HTTPS URLs.
 *
 * Each review owns a sub-directory keyed by its id, so re-submitting a review
 * (REPLACE on the unique (reviewerUid, hikeId) index) cleanly overwrites its
 * previous photos rather than leaking files.
 */
open class ReviewImageStore(private val context: Context) {

    private fun dirFor(reviewId: String): File =
        File(File(context.filesDir, REVIEW_DIR), reviewId).apply { mkdirs() }

    /**
     * Copy each [uris] entry into this review's directory and return the
     * resulting files, in the same order. A single unreadable source is
     * skipped rather than failing the whole batch, so a flaky picker grant
     * never loses the other photos.
     */
    open suspend fun saveReviewImages(reviewId: String, uris: List<Uri>): List<File> =
        withContext(Dispatchers.IO) {
            val dir = dirFor(reviewId)
            // Start from a clean directory so a re-submission doesn't mix old
            // and new photos.
            dir.listFiles()?.forEach { it.delete() }
            uris.mapIndexedNotNull { index, uri ->
                runCatching {
                    val target = File(dir, "img_${index}_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input) { "Cannot open input stream for $uri" }
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    target
                }.getOrNull()
            }
        }

    private companion object {
        const val REVIEW_DIR = "review_images"
    }
}
