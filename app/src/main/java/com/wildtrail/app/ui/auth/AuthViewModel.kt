package com.wildtrail.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the auth screens.
 *
 *   - We use one immutable data class with all the fields the screen needs.
 *     This is the **single state object** approach Google now recommends:
 *     it's atomic (no two fields ever drift) and easy to render with a
 *     stateless Composable.
 *
 *   - All fields are nullable / defaults so the initial state is just
 *     `AuthUiState()`.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * The auth ViewModel — owns [AuthUiState] and exposes it as [StateFlow].
 *
 *   - `viewModelScope` ties any coroutines to the ViewModel lifecycle, so
 *     they're cancelled on screen rotation only if the ViewModel is being
 *     destroyed (which is what we want).
 *   - We expose **events** through suspending functions called from the UI;
 *     this matches the UDF (Unidirectional Data Flow) pattern: UI → event
 *     → ViewModel → state → UI.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onUsernameChanged(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun toggleMode() = _uiState.update { it.copy(isSignUp = !it.isSignUp, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Enter an email and at least 6 chars of password") }
            return
        }
        if (state.isSignUp && state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username is required") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isSignUp) {
                authRepository.signUp(state.email.trim(), state.password, state.username.trim())
            } else {
                authRepository.signIn(state.email.trim(), state.password)
            }
            result
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Authentication failed")
                    }
                }
        }
    }

    companion object {
        /**
         * Factory using the new viewmodel-savedstate `viewModelFactory` DSL.
         * It pulls dependencies out of the [WildTrailApp] container so the
         * ViewModel never has to know about Android's Application class.
         */
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                AuthViewModel(app.container.authRepository)
            }
        }
    }
}
