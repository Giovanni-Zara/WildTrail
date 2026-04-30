package com.wildtrail.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.di.AppContainer
import com.wildtrail.app.ui.components.WildTrailBottomBar
import com.wildtrail.app.ui.navigation.Destination
import com.wildtrail.app.ui.navigation.WildTrailNavGraph
import com.wildtrail.app.ui.navigation.bottomNavItems

/**
 * Root composable for the entire app.
 *
 *  - Owns the [androidx.navigation.NavHostController].
 *  - Watches the auth state and switches between the auth sub-graph and the
 *    main sub-graph, popping the back-stack so the user can't navigate
 *    back into the wrong half.
 *  - Hosts the Material 3 [Scaffold]: the standard "slot" for a top bar /
 *    bottom bar / floating action button. Compose calls these "slots" in
 *    its API; the assignment specifies them as the structural primitives.
 */
@Composable
fun WildTrailRoot(
    appContainer: AppContainer,
    authState: AuthState,
) {
    val navController = rememberNavController()

    // Decide which sub-graph is the start destination based on the current
    // auth state. Using `LaunchedEffect(authState)` we also force a
    // navigation when the user logs in / out at runtime.
    val startDestination = when (authState) {
        is AuthState.SignedIn -> Destination.MainGraph.route
        else -> Destination.AuthGraph.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                navController.navigate(Destination.MainGraph.route) {
                    popUpTo(Destination.AuthGraph.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            AuthState.SignedOut -> {
                navController.navigate(Destination.AuthGraph.route) {
                    popUpTo(Destination.MainGraph.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            AuthState.Loading -> Unit
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.destination.route }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) WildTrailBottomBar(navController)
            },
        ) { padding: PaddingValues ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                WildTrailNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }
}
