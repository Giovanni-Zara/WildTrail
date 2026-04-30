package com.wildtrail.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Strongly-typed destinations for the Navigation Compose graph.
 *
 * Defining routes as constants on a sealed class:
 *  - keeps every path in one place (search-friendly),
 *  - guarantees the spelling is identical at every call site,
 *  - makes it trivial to add type-safe arguments via small helper builders.
 *
 * In a larger app you'd use the new "type-safe nav-args" API from
 * Navigation Compose 2.8 — for the assignment, string routes are simpler
 * and equally idiomatic.
 */
sealed class Destination(val route: String) {

    // --- Auth flow (separate top-level graph) ----------------------------
    data object AuthGraph : Destination("auth_graph")
    data object Login : Destination("login")

    // --- Main flow (the bottom-bar destinations) -------------------------
    data object MainGraph : Destination("main_graph")
    data object Home : Destination("home")
    data object Explore : Destination("explore")
    data object Track : Destination("track")
    data object Profile : Destination("profile")

    // --- Detail screens --------------------------------------------------
    data object HikeDetail : Destination("hike_detail/{hikeId}") {
        fun create(hikeId: String) = "hike_detail/$hikeId"
        const val ARG_HIKE_ID = "hikeId"
    }
}

/**
 * Bottom-bar destinations. Each one carries the Material icon + display
 * label so [com.wildtrail.app.ui.navigation.WildTrailBottomBar] can render
 * them with no further configuration.
 */
data class BottomNavItem(
    val destination: Destination,
    val icon: ImageVector,
    val label: String,
)

val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(Destination.Home, Icons.Filled.Home, "Home"),
    BottomNavItem(Destination.Explore, Icons.Filled.Search, "Explore"),
    BottomNavItem(Destination.Track, Icons.Filled.PlayArrow, "Track"),
    BottomNavItem(Destination.Profile, Icons.Filled.Person, "Profile"),
)
