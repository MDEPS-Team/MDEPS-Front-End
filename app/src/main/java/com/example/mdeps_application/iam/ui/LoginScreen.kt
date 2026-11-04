package com.example.mdeps_application.iam.ui

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.example.mdeps_application.R
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var corporateEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
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

    val launchBiometricPrompt: () -> Unit = {
        val activity = context as? FragmentActivity
        if (activity == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(biometricUnavailableMessage)
            }
        } else {
        val biometricHelper = BiometricHelper(activity)
        biometricHelper.authenticate(
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(biometricSuccessMessage)
                }
            },
            onError = { errorCode, errorMessage ->
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
                    onClick = { /* wired from application layer */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = signInButtonDescription },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.iam_login_sign_in),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
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

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MDEPS_APPLICATIONTheme {
        LoginScreen()
    }
}
