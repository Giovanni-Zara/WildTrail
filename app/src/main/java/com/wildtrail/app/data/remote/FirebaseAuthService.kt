package com.wildtrail.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper over [FirebaseAuth] that exposes:
 *  - login / signup as suspending functions ([await] from kotlinx-coroutines-play-services),
 *  - the current user as a hot [Flow] driven by [FirebaseAuth.AuthStateListener].
 *
 * The wrapper makes the Repository layer trivially testable: in unit tests
 * we can replace this object with a fake.
 */
/** `open` so that tests can substitute a no-op subclass without needing
 *  `mockito-inline`. Production code uses the real [FirebaseAuth] backed
 *  default constructor. */
open class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    /** The current logged-in [FirebaseUser], or null if signed out. */
    open val currentUser: FirebaseUser? get() = auth.currentUser

    /** Hot stream that emits whenever the auth state changes. */
    open fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        // Emit current value immediately so collectors don't have to wait.
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    open suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: error("Firebase returned a null user after sign-in")
    }

    open suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: error("Firebase returned a null user after sign-up")
    }

    open fun signOut() = auth.signOut()

    /** Exchanges a Google ID token (from Credential Manager) for a Firebase
     *  user. Caller is responsible for actually obtaining the token. */
    open suspend fun signInWithGoogleIdToken(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user ?: error("Firebase returned a null user after Google sign-in")
    }
}
