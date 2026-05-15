package com.wildtrail.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wildtrail.app.domain.model.DEFAULT_EMERGENCY_NUMBER
import androidx.compose.foundation.text.KeyboardOptions
import com.wildtrail.app.ui.profile.ProfileViewModel

/**
 * Edit-your-own-profile screen, reached via the gear icon on the Profile
 * tab. We re-use [ProfileViewModel] (targetUid = null → the logged-in
 * user) so we already have a live [com.wildtrail.app.domain.model.User]
 * and a working `updateProfile` action.
 *
 * For now we expose bio, country, and emergency contact — the immutable
 * pieces (username, sex, date of birth) are intentionally read-only here
 * because changing them retroactively would corrupt denormalised data on
 * existing hikes / comments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(
        key = "profile/me",
        factory = ProfileViewModel.factory(targetUid = null),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user = state.user

    var bio by remember(user?.bio) { mutableStateOf(user?.bio.orEmpty()) }
    var country by remember(user?.country) { mutableStateOf(user?.country.orEmpty()) }
    var emergency by remember(user?.emergencyContactNumber) {
        mutableStateOf(user?.emergencyContactNumber.orEmpty())
    }
    var savedFlash by remember { mutableStateOf(false) }

    LaunchedEffect(savedFlash) {
        if (savedFlash) {
            kotlinx.coroutines.delay(1_500L)
            savedFlash = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        if (user == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) { Text("Loading profile…") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            ProfilePictureEditor(
                currentUrl = user.profilePictureUrl,
                onPicked = { uri -> viewModel.changeProfilePicture(uri) },
                isUploading = state.uploadingPicture,
            )

            // --- Read-only header ---------------------------------------
            Text("Account", style = MaterialTheme.typography.titleLarge)
            ReadOnlyRow("Username", user.username)
            ReadOnlyRow(
                "Sex",
                user.sex?.name?.lowercase()?.replace('_', ' ') ?: "—",
            )
            val ageText = user.dateOfBirth?.let { dob ->
                val years = ageInYears(dob)
                if (years > 0) "$years years old" else null
            } ?: "—"
            ReadOnlyRow("Age", ageText)

            // --- Editable fields ----------------------------------------
            Spacer(Modifier.height(8.dp))
            Text("Editable", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = emergency,
                onValueChange = { emergency = it.filter { ch -> ch.isDigit() || ch == '+' } },
                label = { Text("Emergency contact number") },
                placeholder = { Text("Default: $DEFAULT_EMERGENCY_NUMBER") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.updateProfile(
                        bio = bio.trim().takeIf { it.isNotEmpty() },
                        country = country.trim().takeIf { it.isNotEmpty() },
                        emergencyContactNumber = emergency.trim()
                            .takeIf { it.isNotEmpty() } ?: DEFAULT_EMERGENCY_NUMBER,
                    )
                    savedFlash = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Save changes") }

            if (savedFlash) {
                Text(
                    "Saved.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ProfilePictureEditor(
    currentUrl: String?,
    onPicked: (Uri) -> Unit,
    isUploading: Boolean,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? -> if (uri != null) onPicked(uri) }
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(112.dp).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (currentUrl != null) {
                AsyncImage(
                    model = currentUrl,
                    contentDescription = null,
                    modifier = Modifier.size(112.dp).clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp),
                )
            }
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                IconButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Change profile picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

private fun ageInYears(dobMillis: Long): Int {
    val today = java.util.Calendar.getInstance()
    val dob = java.util.Calendar.getInstance().apply { timeInMillis = dobMillis }
    var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)
    if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) age--
    return age.coerceAtLeast(0)
}
