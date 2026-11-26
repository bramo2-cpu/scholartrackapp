package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val authenticated: Boolean = false
)

class AuthViewModel(private val repo: ScholarshipRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(authenticated = repo.currentUserId.value != null))
    val state: StateFlow<AuthUiState> = _state

    fun toggleMode() = _state.update {
        it.copy(isRegisterMode = !it.isRegisterMode, error = null)
    }

    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v, error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }

    fun submit() {
        val s = _state.value
        if (s.isRegisterMode && s.fullName.isBlank()) {
            _state.update { it.copy(error = "Full name is required") }
            return
        }
        if (s.email.isBlank() || !s.email.contains("@")) {
            _state.update { it.copy(error = "Enter a valid email address") }
            return
        }
        if (s.password.length < 4) {
            _state.update { it.copy(error = "Password must be at least 4 characters") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (s.isRegisterMode) {
                repo.register(s.fullName, s.email, s.password)
            } else {
                repo.login(s.email, s.password)
            }
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, authenticated = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Authentication failed") } }
            )
        }
    }

    fun logout() {
        repo.logout()
        _state.value = AuthUiState()
    }
}
