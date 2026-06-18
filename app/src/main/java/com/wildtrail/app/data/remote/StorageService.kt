package com.wildtrail.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around Firebase Storage for image uploads (profile pictures
 * and review photos).
 *
 *  - Files are stored at `profile_pictures/<uid>.jpg`. Re-uploading
 *    overwrites the previous file so we always have a single canonical
 *    picture per user.
 *  - We return the **public download URL** (an HTTPS URL Firebase generates
 *    with a token), which we then store on the User document. Unlike the
 *    `content://…` URI returned by the Photo Picker, this URL is valid on
 *    *every* device — exactly what we need for cross-device profile
 *    pictures.
 */
open class StorageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {

    open suspend fun uploadProfilePicture(uid: String, localUri: Uri): String {
        val ref = storage.reference.child("profile_pictures/$uid.jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Upload a single review photo, returning its cross-device HTTPS download
     * URL. Photos are namespaced under the owning review
     * (`review_images/<reviewId>/<index>.jpg`) so re-submitting the same
     * review overwrites its previous photos instead of leaking orphans.
     */
    open suspend fun uploadReviewImage(reviewId: String, index: Int, localUri: Uri): String {
        val ref = storage.reference.child("review_images/$reviewId/$index.jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }
}
