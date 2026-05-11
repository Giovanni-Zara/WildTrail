package com.wildtrail.app.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wildtrail.app.domain.model.Sex
import com.wildtrail.app.ui.theme.WildTrailTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Stateful entry-point: hooks Compose to the [AuthViewModel] and forwards
 * state down + events up to the stateless [LoginContent].
 *
 * This split is the core of the **stateless Composable** pattern:
 *   - Stateful version (this) does Android-specific wiring (ViewModel, state collection).
 *   - Stateless version ([LoginContent]) takes plain Kotlin params -> trivially previewable + testable.
 */
@Composable
fun LoginRoute(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onUsernameChange = viewModel::onUsernameChanged,
        onSexChange = viewModel::onSexChanged,
        onDateOfBirthChange = viewModel::onDateOfBirthChanged,
        onCountryChange = viewModel::onCountryChanged,
        onBioChange = viewModel::onBioChanged,
        onProfilePictureChange = viewModel::onProfilePictureChanged,
        onToggleMode = viewModel::toggleMode,
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSexChange: (Sex) -> Unit,
    onDateOfBirthChange: (Long) -> Unit,
    onCountryChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onProfilePictureChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "WildTrail",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Track. Discover. Share.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(32.dp))

            // --- Sign-up only fields ------------------------------------
            if (state.isSignUp) {
                ProfilePicturePicker(
                    uri = state.profilePictureUri,
                    onChange = onProfilePictureChange,
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    "Sex *",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp),
                )
                SexSelector(
                    selected = state.sex,
                    onChange = onSexChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                DateOfBirthPicker(
                    millis = state.dateOfBirth,
                    onChange = onDateOfBirthChange,
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.country,
                    onValueChange = onCountryChange,
                    label = { Text("Country *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    label = { Text("Short bio (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }

            // --- Common fields ------------------------------------------
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            state.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(if (state.isSignUp) "Create account" else "Log in")
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onToggleMode) {
                Text(
                    if (state.isSignUp) "Already have an account? Log in"
                    else "No account yet? Sign up",
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfilePicturePicker(
    uri: String,
    onChange: (String) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { result: Uri? ->
        if (result != null) onChange(result.toString())
    }
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (uri.isNotBlank()) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        }
        IconButton(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        ) {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = "Pick profile picture (optional)",
                tint = if (uri.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun SexSelector(
    selected: Sex?,
    onChange: (Sex) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Sex.values().forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onChange(option) },
                label = { Text(option.label()) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPicker(
    millis: Long?,
    onChange: (Long) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val df = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(
            if (millis == null) "Date of birth *"
            else "Born ${df.format(java.util.Date(millis))}",
        )
    }
    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(onClick = {
                    state.selectedDateMillis?.let(onChange)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

private fun Sex.label(): String = when (this) {
    Sex.MALE -> "Male"
    Sex.FEMALE -> "Female"
    Sex.OTHER -> "Other"
    Sex.PREFER_NOT_TO_SAY -> "Prefer not to say"
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    WildTrailTheme {
        LoginContent(
            state = AuthUiState(email = "demo@wildtrail.com", password = "secret"),
            onEmailChange = {},
            onPasswordChange = {},
            onUsernameChange = {},
            onSexChange = {},
            onDateOfBirthChange = {},
            onCountryChange = {},
            onBioChange = {},
            onProfilePictureChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}
