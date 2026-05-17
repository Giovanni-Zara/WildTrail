package com.wildtrail.app.data.repository

import com.wildtrail.app.domain.model.User

/**
 * Guard for the Firestore → Room mirror.
 *
 * A `file://` [User.profilePictureUrl] is an offline-first *local* copy of a
 * just-picked image whose Firebase Storage upload hasn't landed yet (or has
 * failed because Storage is disabled / offline). It is intentionally never
 * pushed to Firestore — a device-local path is meaningless on other devices.
 *
 * The problem that creates: at sign-up we write the user doc to Firestore
 * with the picture cleared, the snapshot listener immediately echoes that
 * `null` back, and a blind `userDao.upsert(remote)` would erase the picture
 * the user just chose. So whenever we hydrate Room from a remote copy, if
 * the remote still has no picture but the local row holds a pending
 * `file://` one, keep the local one. A real `https://` URL (upload
 * succeeded) always wins, so the picture is upgraded the moment it syncs.
 */
internal fun User.keepingLocalPicture(local: User?): User {
    val localPic = local?.profilePictureUrl
    return if (profilePictureUrl.isNullOrBlank() &&
        localPic != null &&
        localPic.startsWith("file://")
    ) {
        copy(profilePictureUrl = localPic)
    } else {
        this
    }
}
