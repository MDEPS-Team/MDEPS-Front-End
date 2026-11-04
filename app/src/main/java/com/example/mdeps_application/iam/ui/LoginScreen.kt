package com.example.mdeps_application.iam.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.application.IamViewModel
import kotlinx.coroutines.launch

private const val BIOMETRIC_LOG_TAG = "IamBiometric"

@Composable
fun LoginScreen(
    viewModel: IamViewModel,
    onLoginSuccess: () -> Unit,
) {
    var corporateEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hostActivity = LocalActivity.current
    val fragmentActivity = hostActivity as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val emailIconDescription = stringResource(R.string.iam_a11y_email_icon)
    val passwordIconDescription = stringResource(R.string.iam_a11y_password_icon)
    val emailFieldDescription = stringResource(R.string.iam_a11y_corporate_email_field)
    val passwordFieldDescription = stringResource(R.string.iam_a11y_password_field)
    val signInButtonDescription = stringResource(R.string.iam_a11y_sign_in_button)
    val biometricButtonDescription = stringResource(R.string.iam_a11y_biometric_button)
    val biometricSuccessMessage = stringResource(R.string.iam_biometric_success)
    val biometricUnavailableMessage = stringResource(R.string.iam_biometric_unavailable)

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    val launchBiometricPrompt: () -> Unit = {
        if (fragmentActivity == null) {
            Log.e(
                BIOMETRIC_LOG_TAG,
                "BiometricPrompt requires FragmentActivity; host is ${hostActivity?.javaClass?.name}",
            )
            coroutineScope.launch {
                snackbarHostState.showSnackbar(biometricUnavailableMessage)
            }
        } else {
            Log.d(BIOMETRIC_LOG_TAG, "Launching BiometricPrompt on ${fragmentActivity.javaClass.simpleName}")
            val biometricHelper = BiometricHelper(fragmentActivity)
            biometricHelper.authenticate(
                onSuccess = {
                    Log.i(BIOMETRIC_LOG_TAG, "Biometric authentication succeeded")
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(biometricSuccessMessage)
                    }
                    viewModel.signInWithBiometrics()
                },
                onError = { errorCode, errorMessage ->
                    Log.w(
                        BIOMETRIC_LOG_TAG,
                        "Biometric error code=$errorCode message=$errorMessage",
                    )
                    val userDismissed =
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    if (!userDismissed) {
                        coroutineScope.launch {
                            val message = context.getString(R.string.iam_biometric_error, errorMessage)
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.iam_login_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.iam_login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = corporateEmail,
                    onValueChange = { corporateEmail = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = emailFieldDescription },
                    label = { Text(stringResource(R.string.iam_login_corporate_email_label)) },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = emailIconDescription,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = passwordFieldDescription },
                    label = { Text(stringResource(R.string.iam_login_password_label)) },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = passwordIconDescription,
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.signIn(corporateEmail, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = signInButtonDescription },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.iam_login_sign_in),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = launchBiometricPrompt,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = biometricButtonDescription,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.iam_biometric_sign_in_action),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    IconButton(
                        onClick = launchBiometricPrompt,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.semantics { contentDescription = biometricButtonDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = biometricButtonDescription,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
