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
import com.wildtrail.app.ui.home.HomeRoute
import com.wildtrail.app.ui.profile.ProfileRoute
import com.wildtrail.app.ui.tracking.TrackingRoute

/**
 * Navigation graph: an Auth sub-graph and a Main sub-graph. The MainGraph
 * also exposes a `profile/{uid}` route used by clickable usernames in
 * reviews, comments, and hike-card creator rows.
 */
@Composable
fun WildTrailNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    val openHike: (String) -> Unit = { hikeId ->
        navController.navigate(Destination.HikeDetail.create(hikeId))
    }
    val openUser: (String) -> Unit = { uid ->
        navController.navigate(Destination.UserProfile.create(uid))
    }

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
                HomeRoute(onHikeClick = openHike, onUserClick = openUser)
            }
            composable(Destination.Explore.route) {
                ExploreRoute(onHikeClick = openHike, onUserClick = openUser)
            }
            composable(Destination.Track.route) {
                TrackingRoute()
            }
            composable(Destination.Profile.route) {
                ProfileRoute(
                    targetUid = null,
                    onBack = null,
                    onHikeClick = openHike,
                    onUserClick = openUser,
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
                    onUserClick = openUser,
                )
            }
            composable(
                route = Destination.UserProfile.route,
                arguments = listOf(
                    navArgument(Destination.UserProfile.ARG_UID) { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getString(Destination.UserProfile.ARG_UID)
                    .orEmpty()
                ProfileRoute(
                    targetUid = uid,
                    onBack = { navController.popBackStack() },
                    onHikeClick = openHike,
                    onUserClick = openUser,
                )
            }
        }
    }
}
