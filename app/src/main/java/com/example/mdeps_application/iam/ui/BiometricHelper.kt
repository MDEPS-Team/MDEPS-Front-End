package com.example.mdeps_application.iam.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.mdeps_application.R

class BiometricHelper(
    private val activity: FragmentActivity,
) {

    fun canAuthenticate(): Boolean {
        val result = BiometricManager.from(activity).canAuthenticate(AUTHENTICATORS)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: CharSequence) -> Unit,
    ) {
        if (!canAuthenticate()) {
            onError(
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
                activity.getString(R.string.iam_biometric_unavailable),
            )
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.iam_biometric_title))
            .setSubtitle(activity.getString(R.string.iam_biometric_subtitle))
            .setNegativeButtonText(activity.getString(R.string.iam_biometric_cancel))
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }

    private companion object {
        const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
}
