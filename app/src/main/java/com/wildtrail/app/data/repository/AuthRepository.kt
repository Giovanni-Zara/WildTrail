package com.wildtrail.app.data.repository

import android.net.Uri
import android.util.Log
import com.wildtrail.app.data.local.dao.UserDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirebaseAuthService
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.StorageService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.DEFAULT_EMERGENCY_NUMBER
import com.wildtrail.app.domain.model.Sex
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.util.LocalImageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The single source of truth for "is the user logged in, and who are they?".
 *
 * # Why a Repository?
 * The Repository pattern abstracts away *where* the data came from. Our
 * ViewModels never call FirebaseAuth directly — they call this repo. That
 * keeps them unit-testable, lets us add caching, and gives us a place to
 * synchronise the local Room cache with the remote Firestore state.
 *
 * # Offline-first
 * On every successful login/signup we mirror the user document into Room.
 * Subsequent app starts can therefore display the user's profile *before*
 * Firestore has finished its handshake.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}

/**
 * Open class so tests can override individual methods with a fake. The
 * [DefaultAuthRepository] (this very class) is what the production DI
 * container wires up; the [com.wildtrail.app.testing.FakeAuthRepository]
 * used in unit tests subclasses it.
 */
open class AuthRepository(
    private val authService: FirebaseAuthService,
    private val firestore: FirestoreService,
    private val storage: StorageService,
    private val imageStore: LocalImageStore,
    private val userDao: UserDao,
    /** Application scope; collection of remote streams must outlive any one screen. */
    private val externalScope: CoroutineScope,
) {

    /**
     * True once we've finished the very first auth-state resolution.
     * MainActivity uses this to gate the splash screen so we don't flash
     * the login screen at an already-signed-in user.
     */
    private val _isInitialised = MutableStateFlow(false)
    open val isInitialised: StateFlow<Boolean> = _isInitialised.asStateFlow()

    /** The auth state surfaced to the UI. Cold flow upgraded to a hot StateFlow. */
    open val authState: StateFlow<AuthState> = authService.authStateFlow()
        .map { firebaseUser ->
            if (firebaseUser == null) {
                _isInitialised.value = true
                AuthState.SignedOut
            } else {
                // Try cache first — if found we can render instantly while we
                // refresh from Firestore in the background.
                val cached = userDao.getById(firebaseUser.uid)?.toDomain()
                externalScope.launch {
                    // Wrapped: a Firestore read failure (network, security
                    // rules, transient SDK error) must NEVER crash the app.
                    runCatching {
                        val remote = firestore.getUser(firebaseUser.uid)?.toDomain()
                        if (remote != null) {
                            val local = userDao.getById(firebaseUser.uid)?.toDomain()
                            userDao.upsert(remote.keepingLocalPicture(local).toEntity())
                        }
                    }.onFailure { Log.w(TAG, "Background user refresh failed", it) }
                }
                _isInitialised.value = true
                AuthState.SignedIn(cached ?: bootstrapUser(firebaseUser.uid, firebaseUser.email))
            }
        }
        .stateIn(externalScope, SharingStarted.Eagerly, AuthState.Loading)

    open suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val fbUser = authService.signIn(email, password)
        // Pull the profile if Firestore allows; if it doesn't, fall back to
        // a freshly-bootstrapped local user. We never let a Firestore error
        // turn a successful auth into a failed login.
        val remote = runCatching { firestore.getUser(fbUser.uid)?.toDomain() }.getOrNull()
        val local = userDao.getById(fbUser.uid)?.toDomain()
        // Prefer remote, but never let it (or a Firestore miss) wipe a
        // locally-pending profile picture that hasn't finished uploading.
        val user = remote?.keepingLocalPicture(local)
            ?: local
            ?: bootstrapUser(fbUser.uid, fbUser.email)
        userDao.upsert(user.toEntity())
        user
    }

    /**
     * Sign-up now collects the demographic profile in one go: sex / DOB /
     * country are mandatory, bio + profile picture are optional.
     *
     * The [profilePictureUri] is a local `content://` URI from the system
     * Photo Picker. We follow the same offline-first path as a later
     * picture change: copy it into app storage and save it on the user
     * immediately (so the profile shows it even with Storage disabled /
     * offline), then best-effort upload and swap to the cross-device HTTPS
     * URL once it is available.
     */
    open suspend fun signUp(
        email: String,
        password: String,
        username: String,
        sex: Sex,
        dateOfBirth: Long,
        country: String,
        bio: String? = null,
        profilePictureUri: Uri? = null,
        emergencyContactNumber: String? = null,
    ): Result<User> = runCatching {
        val fbUser = authService.signUp(email, password)
        // Copy the picked image into app-owned storage so the new account
        // shows it immediately — even when the Storage upload below fails
        // (Storage disabled / offline). The Photo Picker only grants a
        // transient read on the content:// URI, so the bytes must be copied
        // now rather than the URI persisted.
        val localFile = profilePictureUri?.let { uri ->
            runCatching { imageStore.saveProfilePicture(fbUser.uid, uri) }
                .onFailure { Log.w(TAG, "Local profile picture copy failed", it) }
                .getOrNull()
        }
        val localPath = localFile?.let { Uri.fromFile(it).toString() }
        val now = System.currentTimeMillis()
        val user = User(
            firebaseUid = fbUser.uid,
            username = username,
            sex = sex,
            dateOfBirth = dateOfBirth,
            country = country,
            level = 1,
            xpPoints = 0,
            totalDistanceKm = 0f,
            totalHikesCount = 0,
            profilePictureUrl = localPath,
            bio = bio,
            emergencyContactNumber = emergencyContactNumber?.takeIf { it.isNotBlank() }
                ?: DEFAULT_EMERGENCY_NUMBER,
            createdAt = now,
            lastActive = now,
            isPublic = true,
        )
        // Room gets the local path (offline-first). Firestore must NOT get a
        // device-local file:// path, so push the doc with the picture cleared
        // — the HTTPS URL is synced once the upload below succeeds.
        userDao.upsert(user.toEntity())
        runCatching { firestore.upsertUser(user.copy(profilePictureUrl = null).toDto()) }
            .onFailure { Log.w(TAG, "Firestore profile sync skipped on signUp", it) }

        // Best-effort upload of the file we own; swap to the cross-device
        // HTTPS URL on success (Room + Firestore).
        val uploadSource = localFile?.let(Uri::fromFile) ?: profilePictureUri
        if (uploadSource != null) {
            runCatching {
                val url = storage.uploadProfilePicture(fbUser.uid, uploadSource)
                val patched = user.copy(profilePictureUrl = url)
                userDao.upsert(patched.toEntity())
                runCatching { firestore.upsertUser(patched.toDto()) }
                    .onFailure { Log.w(TAG, "Firestore picture URL sync skipped on signUp", it) }
            }.onFailure { Log.w(TAG, "Profile picture upload skipped", it) }
        }
        user
    }

    open fun signOut() = authService.signOut()

    /** The signed-in account's email (used to prefill the settings screen). */
    open fun currentEmail(): String? = authService.currentEmail

    /** Change the account password. Failure (e.g. stale login) is returned,
     *  never thrown, so the UI can show a friendly message. */
    open suspend fun changePassword(newPassword: String): Result<Unit> = runCatching {
        authService.updatePassword(newPassword)
    }

    /** Begin an email change — Firebase emails a verification link to the new
     *  address and only switches it once the user confirms. */
    open suspend fun changeEmail(newEmail: String): Result<Unit> = runCatching {
        authService.sendEmailChangeVerification(newEmail)
    }

    /**
     * Sign in with a Google ID token. We treat it as either a sign-in (the
     * Firebase UID already has a user doc) or an automatic first-time
     * sign-up (we create a minimal user doc).
     */
    open suspend fun signInWithGoogleIdToken(idToken: String): Result<User> = runCatching {
        val fbUser = authService.signInWithGoogleIdToken(idToken)
        val remote = runCatching { firestore.getUser(fbUser.uid)?.toDomain() }.getOrNull()
        val user = if (remote != null) {
            remote
        } else {
            // First time this Google account hits our app — provision a
            // minimal profile. The user can fill in sex / DOB / country
            // later from the profile screen.
            val now = System.currentTimeMillis()
            val fresh = User(
                firebaseUid = fbUser.uid,
                username = fbUser.displayName?.takeIf { it.isNotBlank() }
                    ?: fbUser.email?.substringBefore("@")
                    ?: "hiker",
                sex = null,
                dateOfBirth = null,
                country = null,
                level = 1,
                xpPoints = 0,
                totalDistanceKm = 0f,
                totalHikesCount = 0,
                profilePictureUrl = fbUser.photoUrl?.toString(),
                bio = null,
                emergencyContactNumber = DEFAULT_EMERGENCY_NUMBER,
                createdAt = now,
                lastActive = now,
                isPublic = true,
            )
            runCatching { firestore.upsertUser(fresh.toDto()) }
                .onFailure { Log.w(TAG, "Firestore profile sync skipped on Google sign-in", it) }
            fresh
        }
        userDao.upsert(user.toEntity())
        user
    }

    /** Local cache observer — useful for screens that need the latest user. */
    open fun observeUser(uid: String) = userDao.observeById(uid).map { it?.toDomain() }

    /** Used when we have an auth UID but no cached user (e.g. restored session). */
    private suspend fun bootstrapUser(uid: String, email: String?): User {
        val now = System.currentTimeMillis()
        return User(
            firebaseUid = uid,
            username = email?.substringBefore("@") ?: "hiker",
            sex = null,
            dateOfBirth = null,
            country = null,
            level = 1,
            xpPoints = 0,
            totalDistanceKm = 0f,
            totalHikesCount = 0,
            profilePictureUrl = null,
            bio = null,
            emergencyContactNumber = DEFAULT_EMERGENCY_NUMBER,
            createdAt = now,
            lastActive = now,
            isPublic = true,
        ).also { userDao.upsert(it.toEntity()) }
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
