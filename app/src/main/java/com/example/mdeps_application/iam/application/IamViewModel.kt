package com.example.mdeps_application.iam.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.Credentials
import com.example.mdeps_application.iam.domain.Token
import com.example.mdeps_application.iam.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IamUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)

class IamViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IamUiState())
    val uiState: StateFlow<IamUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(Credentials(email = email.trim(), password = password))
            result.fold(
                onSuccess = { user ->
                    persistSession(user)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = user,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = error.message ?: "Sign in failed.",
                        )
                    }
                },
            )
        }
    }

    fun signInWithBiometrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val storedToken = authRepository.getToken().first()
                if (storedToken != null) {
                    // REST hook: authApiService.refreshToken(TokenRefreshRequestDto(...))
                    require(storedToken.accessToken.isNotBlank()) { "Stored session is invalid." }
                }
                authRepository.login(
                    Credentials(
                        email = BIOMETRIC_BOOTSTRAP_EMAIL,
                        password = BIOMETRIC_BOOTSTRAP_PASSWORD,
                    ),
                ).getOrThrow()
            }.fold(
                onSuccess = { user ->
                    persistSession(user)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = user,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = error.message ?: "Biometric sign in failed.",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun persistSession(user: User) {
        authRepository.saveToken(
            Token(
                accessToken = "mock-access-${user.id}",
                refreshToken = "mock-refresh-${user.id}",
            ),
        )
    }

    companion object {
        private const val BIOMETRIC_BOOTSTRAP_EMAIL = "biometric@vantage.internal"
        private const val BIOMETRIC_BOOTSTRAP_PASSWORD = "biometric-gate"

        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return IamViewModel(authRepository) as T
                }
            }
    }
}
