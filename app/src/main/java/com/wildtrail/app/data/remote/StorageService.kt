package com.wildtrail.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around Firebase Storage for profile-picture uploads.
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
}
