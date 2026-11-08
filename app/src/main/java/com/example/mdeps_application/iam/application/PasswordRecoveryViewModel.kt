package com.example.mdeps_application.iam.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.data.DataStoreAuthRepository
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.EmailPolicy
import com.example.mdeps_application.iam.domain.PasswordPolicy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecoveryStep { EMAIL, CODE, NEW_PASSWORD, DONE }

data class PasswordRecoveryUiState(
    val step: RecoveryStep = RecoveryStep.EMAIL,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorRes: Int? = null,
    val resendSecondsLeft: Int = 0,
)

class PasswordRecoveryViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    val codeLength: Int = DataStoreAuthRepository.RESET_CODE_LENGTH

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorRes = null) }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(codeLength)
        _uiState.update { it.copy(code = digits, errorRes = null) }
        if (digits.length == codeLength) verifyCode()
    }

    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value, errorRes = null) }

    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, errorRes = null) }

    fun sendCode() {
        val email = _uiState.value.email.trim()
        if (!EmailPolicy.isValid(email)) {
            _uiState.update { it.copy(errorRes = R.string.reg_error_email) }
            return
        }
        launchRequest {
            authRepository.requestPasswordReset(email).getOrThrow()
            _uiState.update { it.copy(step = RecoveryStep.CODE, code = "") }
            startCountdown()
        }
    }

    fun resendCode() {
        if (_uiState.value.resendSecondsLeft > 0) return
        launchRequest {
            authRepository.requestPasswordReset(_uiState.value.email.trim()).getOrThrow()
            _uiState.update { it.copy(code = "") }
            startCountdown()
        }
    }

    fun verifyCode() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.code.length != codeLength) {
            _uiState.update { it.copy(errorRes = R.string.rec_error_code) }
            return
        }
        launchRequest(errorRes = R.string.rec_error_code) {
            authRepository.verifyResetCode(state.email.trim(), state.code).getOrThrow()
            _uiState.update { it.copy(step = RecoveryStep.NEW_PASSWORD) }
        }
    }

    fun savePassword() {
        val state = _uiState.value
        when {
            !PasswordPolicy.isAcceptable(state.newPassword) ->
                _uiState.update { it.copy(errorRes = R.string.reg_error_password_weak) }
            state.newPassword != state.confirmPassword ->
                _uiState.update { it.copy(errorRes = R.string.reg_error_password_mismatch) }
            else -> launchRequest {
                authRepository.resetPassword(state.email.trim(), state.code, state.newPassword).getOrThrow()
                _uiState.update { it.copy(step = RecoveryStep.DONE) }
            }
        }
    }

    /** Devuelve false cuando hay que salir del flujo. */
    fun back(): Boolean {
        val previous = when (_uiState.value.step) {
            RecoveryStep.EMAIL, RecoveryStep.DONE -> return false
            RecoveryStep.CODE -> RecoveryStep.EMAIL
            RecoveryStep.NEW_PASSWORD -> RecoveryStep.CODE
        }
        _uiState.update { it.copy(step = previous, errorRes = null) }
        return true
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (s in RESEND_SECONDS downTo 0) {
                _uiState.update { it.copy(resendSecondsLeft = s) }
                if (s > 0) delay(1_000)
            }
        }
    }

    private fun launchRequest(errorRes: Int = R.string.rec_error_generic, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorRes = null) }
            runCatching { block() }
                .onFailure { _uiState.update { it.copy(errorRes = errorRes) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    companion object {
        private const val RESEND_SECONDS = 30

        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PasswordRecoveryViewModel(authRepository) as T
            }
    }
}
