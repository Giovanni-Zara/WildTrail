package com.wildtrail.app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.ui.components.HikeCard

@Composable
fun ExploreRoute(
    onHikeClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreContent(
        state = state,
        onQueryChange = viewModel::onQueryChanged,
        onHikeClick = onHikeClick,
        onUserClick = onUserClick,
        onRefresh = { viewModel.refresh() },
        onToggleLike = viewModel::toggleLike,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreContent(
    state: ExploreUiState,
    onQueryChange: (String) -> Unit,
    onHikeClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onRefresh: suspend () -> Unit,
    onToggleLike: (HikeLog) -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            onRefresh()
            kotlinx.coroutines.delay(900L)
            refreshing = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Explore") }) },
    ) { padding: PaddingValues ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { refreshing = true },
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
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search hikes…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                val list = if (state.query.isBlank()) state.featured else state.results
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.query.isBlank()) "Featured this week" else "Results",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (list.isEmpty()) {
                    item {
                        Text(
                            if (state.query.isBlank()) "No featured hikes yet — be the first!"
                            else "No matches for \"${state.query}\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(list, key = { it.hikeId }) { hike ->
                        HikeCard(
                            hike = hike,
                            isLiked = hike.hikeId in state.likedHikeIds,
                            onClick = { onHikeClick(hike.hikeId) },
                            onLikeClick = { onToggleLike(hike) },
                            onCreatorClick = onUserClick,
                            currentUserUid = state.currentUserUid,
                        )
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
