package com.example.mdeps_application.iam.ui

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.mdeps_application.R

class BiometricHelper(
    private val activity: FragmentActivity,
) {

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: CharSequence) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }
        }

        runCatching {
            BiometricPrompt(activity, executor, callback).authenticate(buildPromptInfo())
        }.onFailure { error ->
            Log.e(BIOMETRIC_LOG_TAG, "Failed to show BiometricPrompt", error)
            onError(BiometricPrompt.ERROR_HW_UNAVAILABLE, error.message ?: "BiometricPrompt failed")
        }
    }

    private fun buildPromptInfo(): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.iam_biometric_title))
            .setSubtitle(activity.getString(R.string.iam_biometric_subtitle))
            .setAllowedAuthenticators(AUTHENTICATORS)

        // Negative button is not allowed when DEVICE_CREDENTIAL is included.
        if ((AUTHENTICATORS and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
            builder.setNegativeButtonText(activity.getString(R.string.iam_biometric_cancel))
        }

        return builder.build()
    }

    private companion object {
        private const val BIOMETRIC_LOG_TAG = "IamBiometric"

        const val AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
