package com.wildtrail.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildtrail.app.ui.WildTrailRoot
import com.wildtrail.app.ui.theme.WildTrailTheme

/**
 * The single Activity in this app. We follow the Single-Activity / Multiple-Composable
 * pattern recommended by Google: the Activity owns the Compose root and the
 * NavHost, but every screen is a Composable function.
 *
 * Lifecycle responsibilities:
 *  - Install the splash screen *before* setContent (required by the
 *    AndroidX SplashScreen support library).
 *  - Enable edge-to-edge so our scaffold can draw under the system bars.
 *  - Hand off all rendering to [WildTrailRoot].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() / setContent.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash screen visible until the auth-state has been
        // resolved, so we don't flash the wrong screen at the user.
        val app = application as WildTrailApp
        val authRepo = app.container.authRepository
        splash.setKeepOnScreenCondition { !authRepo.isInitialised.value }

        setContent {
            WildTrailTheme {
                // The state of "are we logged in?" lives in AuthRepository as a StateFlow.
                // We collect it lifecycle-aware so the UI auto-reacts to login/logout.
                val authState by authRepo.authState.collectAsStateWithLifecycle()
                WildTrailRoot(
                    appContainer = app.container,
                    authState = authState,
                )
            }
        }
    }
}
