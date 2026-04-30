package com.wildtrail.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.wildtrail.app.ui.auth.LoginRoute
import com.wildtrail.app.ui.explore.ExploreRoute
import com.wildtrail.app.ui.hike.HikeDetailRoute
import com.wildtrail.app.ui.hike.HikeDetailViewModel
import com.wildtrail.app.ui.home.HomeRoute
import com.wildtrail.app.ui.profile.ProfileRoute
import com.wildtrail.app.ui.tracking.TrackingRoute

/**
 * Defines the navigation graph as a top-level Composable function. Two
 * sub-graphs:
 *
 *  - [Destination.AuthGraph] — only the [Destination.Login] screen lives
 *    here. We swap the active sub-graph when the auth state changes
 *    (see [com.wildtrail.app.ui.WildTrailRoot]).
 *
 *  - [Destination.MainGraph] — Home, Explore, Track, Profile + Hike Detail.
 *
 * Why a graph at all (instead of an `if/else` over Composables)?
 * Navigation Compose gives us back-stack management, deep-linking, type-safe
 * arguments, and saved-state restoration for free. All of that would
 * otherwise have to be reinvented manually.
 */
@Composable
fun WildTrailNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ---------- Auth sub-graph ----------
        navigation(
            route = Destination.AuthGraph.route,
            startDestination = Destination.Login.route,
        ) {
            composable(Destination.Login.route) {
                LoginRoute()
            }
        }

        // ---------- Main sub-graph ----------
        navigation(
            route = Destination.MainGraph.route,
            startDestination = Destination.Home.route,
        ) {
            composable(Destination.Home.route) {
                HomeRoute(
                    onHikeClick = { hikeId ->
                        navController.navigate(Destination.HikeDetail.create(hikeId))
                    },
                )
            }
            composable(Destination.Explore.route) {
                ExploreRoute(
                    onHikeClick = { hikeId ->
                        navController.navigate(Destination.HikeDetail.create(hikeId))
                    },
                )
            }
            composable(Destination.Track.route) {
                TrackingRoute()
            }
            composable(Destination.Profile.route) {
                ProfileRoute(
                    onHikeClick = { hikeId ->
                        navController.navigate(Destination.HikeDetail.create(hikeId))
                    },
                )
            }
            composable(
                route = Destination.HikeDetail.route,
                arguments = listOf(
                    navArgument(Destination.HikeDetail.ARG_HIKE_ID) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val hikeId = backStackEntry.arguments?.getString(Destination.HikeDetail.ARG_HIKE_ID)
                    .orEmpty()
                HikeDetailRoute(
                    hikeId = hikeId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
