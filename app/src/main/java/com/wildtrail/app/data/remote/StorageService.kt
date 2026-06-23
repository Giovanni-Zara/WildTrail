package com.wildtrail.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

open class StorageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {

    open suspend fun uploadProfilePicture(uid: String, localUri: Uri): String {
        val ref = storage.reference.child("profile_pictures/$uid.jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }

    open suspend fun uploadReviewImage(reviewId: String, index: Int, localUri: Uri): String {
        val ref = storage.reference.child("review_images/$reviewId/$index.jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }
}
