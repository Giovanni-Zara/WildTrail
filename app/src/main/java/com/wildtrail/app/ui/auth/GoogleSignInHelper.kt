package com.wildtrail.app.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Thin wrapper around the AndroidX Credential Manager + Google ID library.
 *
 * Returns the raw Google ID token, which the [com.wildtrail.app.data.repository.AuthRepository]
 * then exchanges for a Firebase user. The caller is responsible for the UI
 * spinner / error display.
 *
 * Setup requirements (see README):
 *   1. In the Firebase console: enable Google as a sign-in provider.
 *   2. Add your Android app's debug + release SHA-1 fingerprints to the
 *      Firebase project, then re-download `google-services.json`.
 *   3. Find the **Web client ID** (Firebase console → Authentication →
 *      Sign-in method → Google → Web SDK configuration). Add it to
 *      `local.properties` as `GOOGLE_WEB_CLIENT_ID=…`.
 *
 * Without those three steps Google Sign-In will fail at runtime — the
 * helper detects the empty Web Client ID and returns a friendly error.
 */
class GoogleSignInHelper(private val context: Context) {

    suspend fun requestIdToken(serverClientId: String): Result<String> = runCatching {
        require(serverClientId.isNotBlank()) {
            "Google Sign-In isn't configured. Add GOOGLE_WEB_CLIENT_ID to local.properties."
        }
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            // false ⇒ also offer accounts that have signed up to other apps,
            // so first-time use of *this* app still works.
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val response = CredentialManager.create(context).getCredential(
            context = context,
            request = request,
        )
        val credential = response.credential
        require(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type ${credential.javaClass.simpleName}"
        }
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
