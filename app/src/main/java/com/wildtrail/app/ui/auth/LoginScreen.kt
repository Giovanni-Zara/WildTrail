package com.wildtrail.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import com.wildtrail.app.ui.theme.WildTrailTheme

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
        onToggleMode = viewModel::toggleMode,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun LoginContent(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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

            if (state.isSignUp) {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }

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
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp),
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
        }
    }
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
            onToggleMode = {},
            onSubmit = {},
        )
    }
}
