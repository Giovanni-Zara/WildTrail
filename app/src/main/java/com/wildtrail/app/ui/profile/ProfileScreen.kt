package com.wildtrail.app.ui.profile

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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wildtrail.app.domain.model.AchievementDefinition
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.ui.components.HikeCard
import com.wildtrail.app.util.LevelMath
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProfileRoute(
    targetUid: String? = null,
    onBack: (() -> Unit)? = null,
    onHikeClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(
        key = "profile/${targetUid ?: "me"}",
        factory = ProfileViewModel.factory(targetUid),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onBack = onBack,
        onHikeClick = onHikeClick,
        onSignOut = viewModel::signOut,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onBack: (() -> Unit)?,
    onHikeClick: (String) -> Unit,
    onSignOut: () -> Unit,
    onRefresh: () -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isMe) "Profile" else state.user?.username ?: "Profile") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (state.isMe) {
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sign out")
                        }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { state.user?.let { ProfileHeader(it) } }
                item { state.user?.let { LevelProgressCard(it) } }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Achievements", style = MaterialTheme.typography.titleLarge)
                }
                if (state.earnedAchievements.isEmpty()) {
                    item {
                        Text(
                            "No achievements yet. Hike more to unlock them!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.earnedAchievements, key = { it.achievementId }) { achievement ->
                        AchievementRow(achievement)
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.isMe) "My hikes" else "Hikes",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (state.hikes.isEmpty()) {
                    item {
                        Text(
                            if (state.isMe) "Your hikes will appear here." else "No public hikes yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.hikes, key = { it.hikeId }) { hike ->
                        HikeCard(hike = hike, onClick = { onHikeClick(hike.hikeId) })
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (user.profilePictureUrl != null) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text(user.username, style = MaterialTheme.typography.headlineMedium)
            val ageText = user.dateOfBirth?.let { dob ->
                val years = ageInYears(dob)
                if (years > 0) "$years y/o" else null
            }
            val parts = listOfNotNull(
                user.country,
                ageText,
                user.sex?.name?.lowercase(Locale.getDefault())?.replace('_', ' '),
            )
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
            user.bio?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LevelProgressCard(user: User) {
    val level = LevelMath.levelForXp(user.xpPoints)
    val current = LevelMath.xpInCurrentLevel(user.xpPoints)
    val needed = LevelMath.xpForNextLevel(level)
    val progress = LevelMath.progressInCurrentLevel(user.xpPoints)

    Card {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Level $level",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${user.xpPoints} XP total",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "$current / $needed XP toward Level ${level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatBlock("Hikes", user.totalHikesCount.toString())
                StatBlock("Distance", "%.1f km".format(user.totalDistanceKm))
                StatBlock("Achievements", user.level.toString())
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun AchievementRow(achievement: AchievementDefinition) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(achievement.name, style = MaterialTheme.typography.titleMedium)
                Text(achievement.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun ageInYears(dobMillis: Long): Int {
    val today = Calendar.getInstance()
    val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
    return age.coerceAtLeast(0)
}
