package com.wildtrail.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.ui.components.HikeCard

/**
 * Stateful entry-point: collects [HomeUiState] and forwards it down.
 */
@Composable
fun HomeRoute(
    onHikeClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onHikeClick = onHikeClick,
        onSignOut = viewModel::signOut,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    onHikeClick: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.currentUser != null) "Hi, ${state.currentUser.username}!"
                        else "Welcome",
                    )
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sign out")
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                if (state.currentUser != null) {
                    StatsRow(state.currentUser)
                }
                Spacer(Modifier.height(8.dp))
                Text("Recently from you", style = MaterialTheme.typography.titleLarge)
            }
            if (state.recentHikes.isEmpty()) {
                item {
                    Text(
                        "Your hikes will appear here once you record one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // Namespace keys per section: a public hike of the current
                // user can appear in BOTH recentHikes and publicFeed and
                // LazyColumn forbids duplicate keys across the whole list.
                items(state.recentHikes, key = { "mine-${it.hikeId}" }) { hike ->
                    HikeCard(hike = hike, onClick = { onHikeClick(hike.hikeId) })
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Trending publicly", style = MaterialTheme.typography.titleLarge)
            }
            // Hide hikes that are already shown in "your recent hikes" so the
            // user doesn't see duplicates AND we keep keys collision-free.
            val mineIds = state.recentHikes.mapTo(HashSet()) { it.hikeId }
            val publicOthers = state.publicFeed.filter { it.hikeId !in mineIds }
            items(publicOthers, key = { "public-${it.hikeId}" }) { hike ->
                HikeCard(hike = hike, onClick = { onHikeClick(hike.hikeId) })
            }
        }
    }
}

@Composable
private fun StatsRow(user: User) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(vertical = 8.dp),
    ) {
        StatBlock(label = "Hikes", value = user.totalHikesCount.toString())
        StatBlock(label = "Distance", value = "%.1f km".format(user.totalDistanceKm))
        StatBlock(label = "XP", value = user.xpPoints.toString())
        StatBlock(label = "Level", value = user.level.toString())
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
