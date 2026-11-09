package com.example.mdeps_application.iam.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.EmailPolicy
import com.example.mdeps_application.iam.domain.PasswordPolicy
import com.example.mdeps_application.iam.domain.RegistrationDraft
import com.example.mdeps_application.iam.domain.Token
import com.example.mdeps_application.iam.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class RegistrationStep { PERSONAL, CONTACT, PROFESSION, ROLE, DONE }

/** Campos validables; el valor es el recurso de texto del error. */
enum class RegistrationField {
    FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL, PHONE, PASSWORD, CONFIRM_PASSWORD, TERMS,
    POSITION, SPECIALTY, EXPERIENCE, ROLE,
}

data class RegistrationUiState(
    val step: RegistrationStep = RegistrationStep.PERSONAL,
    val draft: RegistrationDraft = RegistrationDraft(),
    val errors: Map<RegistrationField, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submitErrorRes: Int? = null,
    val registeredUser: User? = null,
)

class RegistrationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun update(transform: (RegistrationDraft) -> RegistrationDraft) {
        _uiState.update { state ->
            val newDraft = transform(state.draft)
            // Limpia errores de los campos que el usuario está corrigiendo.
            val stillInvalid = validate(state.step, newDraft).keys
            state.copy(
                draft = newDraft,
                errors = state.errors.filterKeys { it in stillInvalid },
            )
        }
    }

    /** Intenta avanzar; si hay errores los expone y permanece en la etapa. */
    fun next() {
        val state = _uiState.value
        val errors = validate(state.step, state.draft)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        when (state.step) {
            RegistrationStep.PERSONAL -> goTo(RegistrationStep.CONTACT)
            RegistrationStep.CONTACT -> goTo(RegistrationStep.PROFESSION)
            RegistrationStep.PROFESSION -> goTo(RegistrationStep.ROLE)
            RegistrationStep.ROLE -> submit()
            RegistrationStep.DONE -> Unit
        }
    }

    /** Devuelve false si ya estaba en la primera etapa (la UI debe salir del flujo). */
    fun back(): Boolean {
        val previous = when (_uiState.value.step) {
            RegistrationStep.PERSONAL, RegistrationStep.DONE -> return false
            RegistrationStep.CONTACT -> RegistrationStep.PERSONAL
            RegistrationStep.PROFESSION -> RegistrationStep.CONTACT
            RegistrationStep.ROLE -> RegistrationStep.PROFESSION
        }
        goTo(previous)
        return true
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitErrorRes = null) }
    }

    private fun goTo(step: RegistrationStep) {
        _uiState.update { it.copy(step = step, errors = emptyMap()) }
    }

    private fun submit() {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitErrorRes = null) }
            authRepository.register(_uiState.value.draft).fold(
                onSuccess = { user ->
                    authRepository.saveUserProfile(user)
                    authRepository.saveToken(
                        Token(accessToken = "mock-access-${user.id}", refreshToken = "mock-refresh-${user.id}"),
                    )
                    _uiState.update {
                        it.copy(isSubmitting = false, registeredUser = user, step = RegistrationStep.DONE)
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitErrorRes = R.string.reg_error_submit)
                    }
                },
            )
        }
    }

    private fun validate(step: RegistrationStep, d: RegistrationDraft): Map<RegistrationField, Int> {
        val errors = mutableMapOf<RegistrationField, Int>()
        when (step) {
            RegistrationStep.PERSONAL -> {
                if (d.firstName.isBlank()) errors[RegistrationField.FIRST_NAME] = R.string.reg_error_required
                if (d.lastName.isBlank()) errors[RegistrationField.LAST_NAME] = R.string.reg_error_required
                if (!isValidBirthDate(d)) errors[RegistrationField.BIRTH_DATE] = R.string.reg_error_birth_date
            }
            RegistrationStep.CONTACT -> {
                if (!EmailPolicy.isValid(d.email)) errors[RegistrationField.EMAIL] = R.string.reg_error_email
                if (d.phone.count { it.isDigit() } < 7) errors[RegistrationField.PHONE] = R.string.reg_error_phone
                if (!PasswordPolicy.isAcceptable(d.password)) {
                    errors[RegistrationField.PASSWORD] = R.string.reg_error_password_weak
                }
                if (d.confirmPassword != d.password || d.confirmPassword.isEmpty()) {
                    errors[RegistrationField.CONFIRM_PASSWORD] = R.string.reg_error_password_mismatch
                }
                if (!d.acceptedTerms) errors[RegistrationField.TERMS] = R.string.reg_error_terms
            }
            RegistrationStep.PROFESSION -> {
                if (d.position.isBlank()) errors[RegistrationField.POSITION] = R.string.reg_error_required
                if (d.specialty.isBlank()) errors[RegistrationField.SPECIALTY] = R.string.reg_error_required
                if (d.experience == null) errors[RegistrationField.EXPERIENCE] = R.string.reg_error_required
            }
            RegistrationStep.ROLE -> {
                if (d.role == null) errors[RegistrationField.ROLE] = R.string.reg_error_role
            }
            RegistrationStep.DONE -> Unit
        }
        return errors
    }

    private fun isValidBirthDate(d: RegistrationDraft): Boolean {
        val day = d.birthDay.toIntOrNull() ?: return false
        val month = d.birthMonth.toIntOrNull() ?: return false
        val year = d.birthYear.toIntOrNull() ?: return false
        val date = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return false
        val today = LocalDate.now()
        // Mayor de 16 años y fecha razonable.
        return date.isBefore(today.minusYears(16)) && year >= 1900
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RegistrationViewModel(authRepository) as T
            }
    }
}
