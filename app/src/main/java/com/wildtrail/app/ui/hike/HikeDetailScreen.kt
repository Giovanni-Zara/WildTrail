package com.wildtrail.app.ui.hike

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.TrailReview
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.ui.components.EditableStarRow
import com.wildtrail.app.ui.components.RatingRow
import com.wildtrail.app.ui.components.StarRow
import com.wildtrail.app.util.formatHikeDate
import kotlinx.coroutines.launch

@Composable
fun HikeDetailRoute(
    hikeId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: HikeDetailViewModel = viewModel(factory = HikeDetailViewModel.factory(hikeId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    HikeDetailContent(
        state = state,
        onBack = onBack,
        onUserClick = onUserClick,
        onPostComment = viewModel::postComment,
        onSubmitReview = viewModel::submitReview,
        onToggleLike = viewModel::toggleLike,
        onRefresh = { scope.launch { viewModel.refresh() } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailContent(
    state: HikeDetailUiState,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostComment: (String) -> Unit,
    onSubmitReview: (Int, Int, Int, Int, Int, Int, Boolean) -> Unit,
    onToggleLike: () -> Unit,
    onRefresh: () -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }

    // Drive the indicator from a coroutine so it animates for a real moment.
    LaunchedEffect(refreshing) {
        if (refreshing) {
            onRefresh()
            kotlinx.coroutines.delay(900L)
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.hike?.title ?: "Hike") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.currentUserUid != null) {
                        LikeButton(
                            liked = state.isLikedByMe,
                            count = state.likeCount,
                            onClick = onToggleLike,
                        )
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        when {
            state.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading hike…")
                }
            }
            state.hike == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "This hike could not be found.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            else -> HikeDetailBody(
                state = state,
                padding = padding,
                refreshing = refreshing,
                onPullRefresh = { refreshing = true },
                onUserClick = onUserClick,
                onPostComment = onPostComment,
                onSubmitReview = onSubmitReview,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HikeDetailBody(
    state: HikeDetailUiState,
    padding: PaddingValues,
    refreshing: Boolean,
    onPullRefresh: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostComment: (String) -> Unit,
    onSubmitReview: (Int, Int, Int, Int, Int, Int, Boolean) -> Unit,
) {
    val hike = state.hike!!
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onPullRefresh,
        modifier = Modifier.fillMaxSize().padding(padding),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                CreatorBlock(
                    hike = hike,
                    creator = state.creator,
                    isMyHike = state.isMyHike,
                    onClick = { onUserClick(hike.creatorFirebaseUid) },
                )
            }
            if (hike.routeCoordinates.size >= 2) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    ) {
                        com.wildtrail.app.ui.components.RouteMap(
                            points = hike.routeCoordinates,
                            modifier = Modifier.fillMaxSize(),
                            follow = false,
                            showCurrentMarker = false,
                        )
                    }
                }
                item { com.wildtrail.app.ui.components.ElevationChart(hike.routeCoordinates) }
            }
            item { HikeStatsCard(hike) }
            item { CharacteristicsCard(hike) }
            item { ReviewStatsCard(hike) }

            item {
                Text(
                    "Reviews (${state.reviews.size})",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (state.reviews.isEmpty()) {
                item { Text("No reviews yet — be the first to share your experience!") }
            } else {
                items(state.reviews, key = { it.reviewId }) { review ->
                    ReviewRow(
                        review = review,
                        author = state.authors[review.reviewerUid],
                        onAuthorClick = { onUserClick(review.reviewerUid) },
                    )
                }
            }
            when {
                state.isMyHike -> item {
                    Text(
                        "You can't review your own hike — the trail characteristics " +
                            "you filled in at save time are shown above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.currentUserUid == null -> Unit
                state.myReviewExists -> item {
                    // The user has already submitted a review for this hike;
                    // their row already appears in the reviews list above, so
                    // we just confirm and *do not* re-show the form.
                    Text(
                        "✓ You've already reviewed this hike — your review is shown above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                else -> item { ReviewForm(onSubmit = onSubmitReview) }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text("Comments", style = MaterialTheme.typography.titleLarge)
            }
            items(state.comments, key = { it.commentId }) { c ->
                CommentRow(
                    comment = c,
                    author = state.authors[c.authorUid],
                    onAuthorClick = { onUserClick(c.authorUid) },
                )
            }
            item { CommentForm(onPost = onPostComment) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LikeButton(liked: Boolean, count: Int, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (liked) "Unlike" else "Like",
                tint = if (liked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("$count", modifier = Modifier.padding(end = 8.dp))
    }
}

/**
 * Uses the hike's *denormalised* creatorUsername / creatorProfilePictureUrl
 * (always available) instead of waiting for the full creator user doc to
 * sync. If we DO have the full doc, we enrich with the level line.
 */
@Composable
private fun CreatorBlock(
    hike: HikeLog,
    creator: User?,
    isMyHike: Boolean,
    onClick: () -> Unit,
) {
    // Resolution order:
    //   1. Current user → "You"
    //   2. denormalised creatorUsername on the hike (filled at save time)
    //   3. observed creator user document (set by HikeDetailViewModel)
    //   4. a friendly placeholder
    val displayName = when {
        isMyHike -> "You"
        hike.creatorUsername.isNotBlank() -> hike.creatorUsername
        creator != null && creator.username.isNotBlank() -> creator.username
        else -> "Hiker"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarFromUrl(
                url = hike.creatorProfilePictureUrl ?: creator?.profilePictureUrl,
                size = 40.dp,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    "Posted by $displayName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (creator != null) {
                    Text(
                        "Level ${creator.level} · ${creator.xpPoints} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (!isMyHike) {
                    Text(
                        "Tap to view profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarFromUrl(url: String?, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(size),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun HikeStatsCard(hike: HikeLog) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Recorded on ${formatHikeDate(hike.endedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            hike.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Distance", "%.1f km".format(hike.lengthKm))
                Stat("Elev. gain", "${hike.elevationGainMeters} m")
                Stat("Avg speed", "%.1f km/h".format(hike.avgSpeedKmh))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Calories", "${hike.caloriesBurned} kcal")
                Stat("XP", "${hike.xpEarned}")
                Stat("Surface", hike.surfaceType.name)
            }
        }
    }
}

@Composable
private fun CharacteristicsCard(hike: HikeLog) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Trail characteristics (from creator)",
                style = MaterialTheme.typography.titleMedium,
            )
            CharacteristicLine("Difficulty", hike.difficultyLevel)
            CharacteristicLine("Mud risk", hike.mudRisk)
            CharacteristicLine("Path clarity", hike.pathClarity)
            CharacteristicLine("Fatigue", hike.fatigueLevel)
            CharacteristicLine("Animal risk", hike.animalEncounterRisk)
            Text(
                if (hike.waterAvailability) "💧 Water available on trail" else "🚱 No water sources",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CharacteristicLine(label: String, value: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        StarRow(rating = value.toFloat())
        Text("  $value/5", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReviewStatsCard(hike: HikeLog) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Community rating", style = MaterialTheme.typography.titleMedium)
            if (hike.reviewCount == 0) {
                Text(
                    "No reviews yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRow(rating = hike.averageRating)
                    Text(
                        "  %.1f / 5  (%d review%s)".format(
                            hike.averageRating,
                            hike.reviewCount,
                            if (hike.reviewCount == 1) "" else "s",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ReviewRow(
    review: TrailReview,
    author: User?,
    onAuthorClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ----- Header: avatar + username + date + big stars ---------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AvatarFromUrl(url = author?.profilePictureUrl, size = 44.dp)
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                        .clickable(onClick = onAuthorClick),
                ) {
                    Text(
                        author?.username ?: "Unknown user",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        com.wildtrail.app.util.formatHikeDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StarRow(rating = review.overallRating.toFloat(), size = 22.dp)
                Spacer(Modifier.size(8.dp))
                Text(
                    "${review.overallRating}/5",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ----- Pill grid of trail conditions ------------------------
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ReviewPill("Difficulty", "${review.difficultyLevel}/5")
                ReviewPill("Mud", "${review.mudRisk}/5")
                ReviewPill("Path", "${review.pathClarity}/5")
                ReviewPill("Fatigue", "${review.fatigueLevel}/5")
                ReviewPill("Animal risk", "${review.animalEncounterRisk}/5")
                ReviewPill(
                    label = if (review.waterAvailability) "💧 Water" else "🚱 No water",
                    value = null,
                )
            }
        }
    }
}

@Composable
private fun ReviewPill(label: String, value: String?) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (value != null) {
                Spacer(Modifier.size(6.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: HikeComment,
    author: User?,
    onAuthorClick: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onAuthorClick),
            ) {
                AvatarFromUrl(url = author?.profilePictureUrl, size = 32.dp)
                Text(
                    author?.username ?: "Unknown user",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewForm(onSubmit: (Int, Int, Int, Int, Int, Int, Boolean) -> Unit) {
    var overall by remember { mutableIntStateOf(3) }
    var difficulty by remember { mutableIntStateOf(3) }
    var mud by remember { mutableIntStateOf(3) }
    var clarity by remember { mutableIntStateOf(3) }
    var fatigue by remember { mutableIntStateOf(3) }
    var animal by remember { mutableIntStateOf(3) }
    var water by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Leave a review", style = MaterialTheme.typography.titleMedium)
            Text("Overall rating", style = MaterialTheme.typography.labelLarge)
            EditableStarRow(rating = overall, onChange = { overall = it })
            RatingRow("Difficulty", difficulty, { difficulty = it })
            RatingRow("Mud risk", mud, { mud = it })
            RatingRow("Path clarity", clarity, { clarity = it })
            RatingRow("Fatigue", fatigue, { fatigue = it })
            RatingRow("Animal risk", animal, { animal = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = water, onCheckedChange = { water = it })
                Text("  Water sources available", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { onSubmit(overall, fatigue, clarity, difficulty, mud, animal, water) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Submit review") }
        }
    }
}

@Composable
private fun CommentForm(onPost: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add a comment…") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onPost(text)
                        text = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Post") }
        }
    }
}
