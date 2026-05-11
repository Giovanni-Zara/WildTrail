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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun HikeDetailRoute(
    hikeId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: HikeDetailViewModel = viewModel(factory = HikeDetailViewModel.factory(hikeId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HikeDetailContent(
        state = state,
        onBack = onBack,
        onUserClick = onUserClick,
        onPostComment = viewModel::postComment,
        onSubmitReview = viewModel::submitReview,
        onToggleLike = viewModel::toggleLike,
        onRefresh = viewModel::refresh,
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
                    LikeButton(
                        liked = state.isLikedByMe,
                        count = state.likeCount,
                        onClick = onToggleLike,
                    )
                },
            )
        },
    ) { padding: PaddingValues ->
        if (state.hike == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { Text("Loading hike…") }
            return@Scaffold
        }

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
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    state.creator?.let { creator ->
                        CreatorBlock(
                            user = creator,
                            onClick = { onUserClick(creator.firebaseUid) },
                        )
                    }
                }
                item { HikeStatsCard(state.hike) }
                item { CharacteristicsCard(state.hike) }
                item { ReviewStatsCard(state.hike) }

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
                if (!state.isMyHike && state.currentUserUid != null) {
                    item { ReviewForm(onSubmit = onSubmitReview) }
                } else if (state.isMyHike) {
                    item {
                        Text(
                            "You can't review your own hike — the trail characteristics " +
                                "you filled in at save time are shown above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

@Composable
private fun CreatorBlock(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = user)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    "Posted by ${user.username}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Level ${user.level} · ${user.xpPoints} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(user: User, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (user.profilePictureUrl != null) {
            AsyncImage(
                model = user.profilePictureUrl,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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

@Composable
private fun ReviewRow(
    review: TrailReview,
    author: User?,
    onAuthorClick: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (author != null) UserAvatar(user = author, size = 32.dp)
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                        .clickable(onClick = onAuthorClick),
                ) {
                    Text(
                        author?.username ?: "Unknown user",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                StarRow(rating = review.overallRating.toFloat())
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Difficulty ${review.difficultyLevel}/5 · Mud ${review.mudRisk}/5 · " +
                    "Path ${review.pathClarity}/5",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Fatigue ${review.fatigueLevel}/5 · Animal risk ${review.animalEncounterRisk}/5",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (review.waterAvailability) "Water available" else "No water on the trail",
                style = MaterialTheme.typography.bodySmall,
            )
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
                if (author != null) UserAvatar(user = author, size = 32.dp)
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
