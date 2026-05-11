package com.wildtrail.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.ui.components.HikeCard

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
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    onHikeClick: (String) -> Unit,
    onSignOut: () -> Unit,
    onRefresh: () -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }

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
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                onRefresh()
                refreshing = false
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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
                    items(state.recentHikes, key = { "mine-${it.hikeId}" }) { hike ->
                        HikeCard(hike = hike, onClick = { onHikeClick(hike.hikeId) })
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Trending publicly", style = MaterialTheme.typography.titleLarge)
                }
                val mineIds = state.recentHikes.mapTo(HashSet()) { it.hikeId }
                val publicOthers = state.publicFeed.filter { it.hikeId !in mineIds }
                items(publicOthers, key = { "public-${it.hikeId}" }) { hike ->
                    HikeCard(hike = hike, onClick = { onHikeClick(hike.hikeId) })
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun StatsRow(user: User) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        StatBlock(label = "Hikes", value = user.totalHikesCount.toString())
        StatBlock(label = "Distance", value = "%.1f km".format(user.totalDistanceKm))
        StatBlock(label = "XP", value = user.xpPoints.toString())
        StatBlock(label = "Level", value = user.level.toString())
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
