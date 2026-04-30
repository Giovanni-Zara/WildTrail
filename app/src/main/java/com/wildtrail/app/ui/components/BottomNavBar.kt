package com.wildtrail.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wildtrail.app.ui.navigation.bottomNavItems

/**
 * Stateless bottom navigation bar wired to the [NavController].
 *
 * Behaviour:
 *  - Highlights the destination matching the current back-stack entry.
 *  - Pops up to the start destination + saves state on tab switch — the
 *    standard "tab navigation" pattern recommended by the Compose docs:
 *    https://developer.android.com/jetpack/compose/navigation#bottom-nav
 */
@Composable
fun WildTrailBottomBar(navController: NavController) {
    NavigationBar {
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentEntry?.destination?.route

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.destination.route,
                onClick = {
                    if (currentRoute != item.destination.route) {
                        navController.navigate(item.destination.route) {
                            // Pop back to the start destination so we don't
                            // build up a long back-stack as users tap tabs.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
