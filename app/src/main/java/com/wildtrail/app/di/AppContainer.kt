package com.wildtrail.app.di

import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wildtrail.app.BuildConfig
import com.wildtrail.app.data.local.WildTrailDatabase
import com.wildtrail.app.data.remote.FirebaseAuthService
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.StorageService
import com.wildtrail.app.data.remote.HikeApiService
import com.wildtrail.app.data.remote.WeatherApiService
import com.wildtrail.app.data.repository.AchievementRepository
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.EmergencyContactRepository
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.PredictRepository
import com.wildtrail.app.data.repository.SensorRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.data.repository.WeatherRepository
import com.wildtrail.app.util.HikeMediaStore
import com.wildtrail.app.util.LocalImageStore
import com.wildtrail.app.util.LocationTracker
import com.wildtrail.app.util.PhotoDescriber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    val weatherRepository: WeatherRepository
    val predictRepository: PredictRepository
    val sensorRepository: SensorRepository
    val locationTracker: LocationTracker
    val hikeMediaStore: HikeMediaStore
    val photoDescriber: PhotoDescriber
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
    private val storage = StorageService()
    private val localImageStore = LocalImageStore(context.applicationContext)
    private val weatherHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()
    // Both the weather proxy and the ML predict endpoint live on the same
    // PythonAnywhere server, so we share one Retrofit instance.
    private val backendRetrofit = Retrofit.Builder()
        .baseUrl(normalizeBaseUrl(BuildConfig.WEATHER_BACKEND_BASE_URL))
        .client(weatherHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApiService: WeatherApiService =
        backendRetrofit.create(WeatherApiService::class.java)

    private val hikeApiService: HikeApiService =
        backendRetrofit.create(HikeApiService::class.java)

    override val authRepository: AuthRepository = AuthRepository(
        authService = authService,
        firestore = firestore,
        storage = storage,
        imageStore = localImageStore,
        userDao = database.userDao(),
        externalScope = appScope,
    )

    // Declared before userRepository because UserRepository depends on it
    // (it fans a profile-picture change out onto the user's hike cards), and
    // `override val`s initialise in declaration order.
    override val hikeLogRepository: HikeLogRepository = HikeLogRepository(
        hikeLogDao = database.hikeLogDao(),
        likeDao = database.likeDao(),
        reviewDao = database.trailReviewDao(),
        userDao = database.userDao(),
        firestore = firestore,
        externalScope = appScope,
    )

    override val userRepository: UserRepository = UserRepository(
        userDao = database.userDao(),
        firestore = firestore,
        storage = storage,
        imageStore = localImageStore,
        hikeLogRepository = hikeLogRepository,
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

    override val weatherRepository: WeatherRepository = WeatherRepository(
        weatherApiService = weatherApiService,
        weatherDao = database.weatherDao(),
    )

    override val predictRepository: PredictRepository = PredictRepository(
        hikeApiService = hikeApiService,
    )

    override val sensorRepository: SensorRepository = SensorRepository(
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager,
    )

    override val locationTracker: LocationTracker = LocationTracker(context)

    override val hikeMediaStore: HikeMediaStore = HikeMediaStore(context.applicationContext)

    override val photoDescriber: PhotoDescriber = PhotoDescriber()

    private fun normalizeBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return "https://example.com/"
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
