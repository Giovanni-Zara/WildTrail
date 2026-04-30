package com.wildtrail.app.ui.hike

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.TrailReview

@Composable
fun HikeDetailRoute(
    hikeId: String,
    onBack: () -> Unit,
    viewModel: HikeDetailViewModel = viewModel(factory = HikeDetailViewModel.factory(hikeId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HikeDetailContent(
        state = state,
        onBack = onBack,
        onPostComment = viewModel::postComment,
        onSubmitReview = viewModel::submitReview,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailContent(
    state: HikeDetailUiState,
    onBack: () -> Unit,
    onPostComment: (String) -> Unit,
    onSubmitReview: (Int, Int, Int, Int, Int, Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.hike?.title ?: "Hike") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        if (state.hike == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Loading hike…")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HikeStatsCard(state.hike) }
            item {
                Text(
                    "Reviews (${state.reviews.size})",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (state.reviews.isEmpty()) {
                item { Text("No reviews yet — be the first to share your experience!") }
            } else {
                items(state.reviews, key = { it.reviewId }) { review -> ReviewRow(review) }
            }
            item { ReviewForm(onSubmit = onSubmitReview) }
            item {
                Spacer(Modifier.height(12.dp))
                Text("Comments", style = MaterialTheme.typography.titleLarge)
            }
            items(state.comments, key = { it.commentId }) { c -> CommentRow(c) }
            item { CommentForm(onPost = onPostComment) }
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
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ReviewRow(review: TrailReview) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Difficulty ${review.difficultyLevel}/5 · Mud risk ${review.mudRisk}/5",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Path clarity ${review.pathClarity}/5 · Animal risk ${review.animalEncounterRisk}/5",
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
private fun CommentRow(comment: HikeComment) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewForm(onSubmit: (Int, Int, Int, Int, Int, Boolean) -> Unit) {
    var difficulty by remember { mutableStateOf(3) }
    var mud by remember { mutableStateOf(2) }
    var clarity by remember { mutableStateOf(4) }
    var fatigue by remember { mutableStateOf(3) }
    var animal by remember { mutableStateOf(2) }
    var water by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Leave a quick review", style = MaterialTheme.typography.titleMedium)
            RatingRow("Difficulty", difficulty) { difficulty = it }
            RatingRow("Mud risk", mud) { mud = it }
            RatingRow("Path clarity", clarity) { clarity = it }
            RatingRow("Fatigue", fatigue) { fatigue = it }
            RatingRow("Animal risk", animal) { animal = it }
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Switch(checked = water, onCheckedChange = { water = it })
                Text("  Water sources available", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { onSubmit(fatigue, clarity, difficulty, mud, animal, water) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Submit review") }
        }
    }
}

@Composable
private fun RatingRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label:", modifier = Modifier.weight(1f))
        for (i in 1..5) {
            androidx.compose.material3.FilterChip(
                selected = i == value,
                onClick = { onValueChange(i) },
                label = { Text("$i") },
                modifier = Modifier.padding(horizontal = 2.dp),
            )
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
