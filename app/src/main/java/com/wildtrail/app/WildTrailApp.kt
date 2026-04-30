package com.wildtrail.app

import android.app.Application
import com.wildtrail.app.di.AppContainer
import com.wildtrail.app.di.DefaultAppContainer

/**
 * Custom [Application] subclass that owns the singleton [AppContainer] —
 * our manual dependency-injection (DI) container.
 *
 * Why a custom Application class?
 *  - Android creates exactly one instance of it per process. That makes it the
 *    natural place to hold long-lived singletons such as the Room database,
 *    Firebase services, and repositories — preventing a leak of duplicates
 *    when an Activity is recreated on rotation.
 *  - We inject this container into ViewModels via a [androidx.lifecycle.viewmodel.viewModelFactory]
 *    extension defined per-screen.
 *
 * In a larger app you would replace this with Hilt / Koin / Dagger, but plain
 * manual DI is the lightest weight approach and is perfectly idiomatic for a
 * MAD-style architecture demo.
 */
class WildTrailApp : Application() {

    /** Lazily-created so unit tests that don't need it never pay the cost. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
