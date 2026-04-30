package com.wildtrail.app.di

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildtrail.app.data.local.WildTrailDatabase
import com.wildtrail.app.data.remote.FirebaseAuthService
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.repository.AchievementRepository
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.EmergencyContactRepository
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.util.LocationTracker
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual DI container.
 *
 * The contract:
 *  - [AppContainer] is the *interface* that ViewModels depend on.
 *  - [DefaultAppContainer] is the production wiring (real Firebase, real Room).
 *  - In tests we substitute a `FakeAppContainer` that returns in-memory fakes.
 *
 * Why a single big container?
 *  - Easy to reason about: every dependency is constructed exactly once,
 *    in one place. Lifecycle is tied to the [com.wildtrail.app.WildTrailApp]
 *    Application object.
 *  - No Hilt / KSP overhead — appropriate for a teaching project.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val hikeLogRepository: HikeLogRepository
    val socialRepository: SocialRepository
    val achievementRepository: AchievementRepository
    val emergencyContactRepository: EmergencyContactRepository
    val locationTracker: LocationTracker
}

class DefaultAppContainer(context: Context) : AppContainer {

    /** Application-level scope. Survives configuration changes; cancelled only
     *  when the process dies. Repositories use it for long-running flow listeners.
     *
     *  The [CoroutineExceptionHandler] is a safety net: if a Firestore listener
     *  (or any other async work in this scope) throws an uncaught exception,
     *  we log it instead of letting it kill the process. The repositories
     *  already use `.catch { }`, so this should never actually fire — it only
     *  guards against bugs / SDK quirks slipping through. */
    private val appScope = CoroutineScope(
        SupervisorJob()
            + Dispatchers.Default
            + CoroutineExceptionHandler { _, t ->
                Log.e("WildTrailAppScope", "Uncaught coroutine error swallowed", t)
            },
    )

    private val database = WildTrailDatabase.getInstance(context)

    private val authService = FirebaseAuthService(FirebaseAuth.getInstance())
    private val firestore = FirestoreService(FirebaseFirestore.getInstance())

    override val authRepository: AuthRepository = AuthRepository(
        authService = authService,
        firestore = firestore,
        userDao = database.userDao(),
        externalScope = appScope,
    )

    override val userRepository: UserRepository = UserRepository(
        userDao = database.userDao(),
        firestore = firestore,
        externalScope = appScope,
    )

    override val hikeLogRepository: HikeLogRepository = HikeLogRepository(
        hikeLogDao = database.hikeLogDao(),
        firestore = firestore,
        externalScope = appScope,
    )

    override val socialRepository: SocialRepository = SocialRepository(
        reviewDao = database.trailReviewDao(),
        userFollowDao = database.userFollowDao(),
        followedTrailDao = database.followedTrailDao(),
        commentDao = database.hikeCommentDao(),
        firestore = firestore,
        externalScope = appScope,
    )

    override val achievementRepository: AchievementRepository = AchievementRepository(
        achievementDao = database.achievementDao(),
        firestore = firestore,
    )

    override val emergencyContactRepository: EmergencyContactRepository = EmergencyContactRepository(
        dao = database.emergencyContactDao(),
        firestore = firestore,
        externalScope = appScope,
    )

    override val locationTracker: LocationTracker = LocationTracker(context)
}
