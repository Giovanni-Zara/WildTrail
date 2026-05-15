package com.wildtrail.app.ui.tracking

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.wildtrail.app.domain.model.SurfaceType
import com.wildtrail.app.ui.components.RatingRow

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackingRoute(
    viewModel: TrackingViewModel = viewModel(factory = TrackingViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    )

    LaunchedEffect(permissions.allPermissionsGranted) {
        viewModel.onPermissionResult(permissions.allPermissionsGranted)
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    if (state.status == TrackingStatus.STOPPED && !state.saved) {
        LaunchedEffect(Unit) { showSaveDialog = true }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Track a hike") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.hasPermission) {
                PermissionBanner(onRequest = { permissions.launchMultiplePermissionRequest() })
            }
            // Live map preview: visible only when we have permission so the
            // map doesn't request location services before the user opts in.
            if (state.hasPermission) {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                ) {
                    com.wildtrail.app.ui.components.RouteMap(
                        points = state.routePoints,
                        modifier = Modifier.fillMaxSize(),
                        follow = state.status == TrackingStatus.RECORDING ||
                            state.status == TrackingStatus.PAUSED,
                        showCurrentMarker = true,
                    )
                }
            }
            StatsCard(state)
            Spacer(Modifier.height(8.dp))
            ControlButtons(
                status = state.status,
                hasPermission = state.hasPermission,
                onStart = viewModel::start,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onStop = viewModel::stop,
            )
            state.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showSaveDialog) {
        SaveHikeDialog(
            distanceKm = state.distanceKm,
            onConfirm = { req ->
                viewModel.saveHike(
                    title = req.title,
                    description = req.description,
                    surfaceType = req.surfaceType,
                    isPrivate = req.isPrivate,
                    difficultyLevel = req.difficultyLevel,
                    mudRisk = req.mudRisk,
                    pathClarity = req.pathClarity,
                    fatigueLevel = req.fatigueLevel,
                    animalEncounterRisk = req.animalEncounterRisk,
                    waterAvailability = req.waterAvailability,
                )
                showSaveDialog = false
            },
            onDismiss = {
                showSaveDialog = false
                viewModel.resetAfterSave()
            },
        )
    }

    if (state.saved) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(800)
            viewModel.resetAfterSave()
        }
    }
}

@Composable
private fun PermissionBanner(onRequest: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Location permission is required to record hikes.",
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRequest) { Text("Grant permission") }
        }
    }
}

@Composable
private fun StatsCard(state: TrackingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Distance", "%.2f km".format(state.distanceKm))
                Stat("Time", formatDuration(state.durationSec))
                Stat("Speed", "%.1f km/h".format(state.avgSpeedKmh))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Elev. gain", "${state.elevationGainM} m")
                Stat("Points", state.routePoints.size.toString())
                Stat("Status", state.status.name)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ControlButtons(
    status: TrackingStatus,
    hasPermission: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    when (status) {
        TrackingStatus.IDLE -> Button(
            onClick = onStart,
            enabled = hasPermission,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Start hike") }

        TrackingStatus.RECORDING -> Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("Pause")
            }
            Button(onClick = onStop, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("Finish")
            }
        }

        TrackingStatus.PAUSED -> Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(onClick = onResume, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("Resume")
            }
            OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("Finish")
            }
        }

        TrackingStatus.STOPPED -> Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Hike finished — fill in the details to save",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Aggregated request for [TrackingViewModel.saveHike] — keeps the dialog
 * callback signature short.
 */
private data class SaveHikeRequest(
    val title: String,
    val description: String?,
    val surfaceType: SurfaceType,
    val isPrivate: Boolean,
    val difficultyLevel: Int,
    val mudRisk: Int,
    val pathClarity: Int,
    val fatigueLevel: Int,
    val animalEncounterRisk: Int,
    val waterAvailability: Boolean,
)

@Composable
private fun SaveHikeDialog(
    distanceKm: Float,
    onConfirm: (SaveHikeRequest) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var surface by remember { mutableStateOf(SurfaceType.MOUNTAIN) }

    var difficulty by remember { mutableStateOf(3) }
    var mud by remember { mutableStateOf(3) }
    var clarity by remember { mutableStateOf(3) }
    var fatigue by remember { mutableStateOf(3) }
    var animal by remember { mutableStateOf(3) }
    var water by remember { mutableStateOf(false) }

    // Every field except the description is mandatory. The rating chips,
    // surface type and switches always carry a value, so the only field
    // that can actually be left empty is the title — guard the Save button
    // on it and surface an inline error.
    val isFormValid = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = isFormValid,
                onClick = {
                    onConfirm(
                        SaveHikeRequest(
                            title = title.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            surfaceType = surface,
                            isPrivate = isPrivate,
                            difficultyLevel = difficulty,
                            mudRisk = mud,
                            pathClarity = clarity,
                            fatigueLevel = fatigue,
                            animalEncounterRisk = animal,
                            waterAvailability = water,
                        ),
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Discard") } },
        title = { Text("Save hike (${"%.2f".format(distanceKm)} km)") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    isError = title.isBlank(),
                    supportingText = {
                        if (title.isBlank()) Text("Title is required")
                    },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                )

                Text("Surface type *", style = MaterialTheme.typography.labelLarge)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SurfaceType.values().forEach { st ->
                        FilterChip(
                            selected = st == surface,
                            onClick = { surface = st },
                            label = { Text(st.label(), maxLines = 1) },
                        )
                    }
                }

                Text("Trail characteristics", style = MaterialTheme.typography.titleMedium)
                Text(
                    "These appear on the hike card and help others know what to expect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                RatingRow("Difficulty", difficulty, { difficulty = it })
                RatingRow("Mud risk", mud, { mud = it })
                RatingRow("Path clarity", clarity, { clarity = it })
                RatingRow("Fatigue", fatigue, { fatigue = it })
                RatingRow("Animal encounter risk", animal, { animal = it })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = water, onCheckedChange = { water = it })
                    Text(
                        "  Water sources available",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                    Text(
                        if (isPrivate) "Private (only you)" else "Public",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
    )
}

private fun SurfaceType.label() = when (this) {
    SurfaceType.MOUNTAIN -> "Mountain"
    SurfaceType.FOREST -> "Forest"
    SurfaceType.COASTAL -> "Coastal"
    SurfaceType.URBAN -> "Urban"
    SurfaceType.DESERT -> "Desert"
    SurfaceType.OTHER -> "Other"
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%d:%02d:%02d".format(h, m, s)
}
