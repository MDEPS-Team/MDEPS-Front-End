package com.example.mdeps_application.iam.ui

import androidx.biometric.BiometricPrompt
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.application.IamViewModel
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxBrandMark
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxTextField
import com.example.mdeps_application.ui.pressScale
import com.example.mdeps_application.ui.staggeredEnter
import kotlinx.coroutines.launch

private const val BIOMETRIC_LOG_TAG = "IamBiometric"

@Composable
fun LoginScreen(
    viewModel: IamViewModel,
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hostActivity = LocalActivity.current
    val fragmentActivity = hostActivity as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val usernameFieldDescription = stringResource(R.string.iam_a11y_username_field)
    val passwordFieldDescription = stringResource(R.string.iam_a11y_password_field)
    val signInButtonDescription = stringResource(R.string.iam_a11y_sign_in_button)
    val biometricButtonDescription = stringResource(R.string.iam_a11y_biometric_button)
    val biometricSuccessMessage = stringResource(R.string.iam_biometric_success)
    val biometricUnavailableMessage = stringResource(R.string.iam_biometric_unavailable)
    val ssoUnavailableMessage = stringResource(R.string.auth_sso_unavailable)
    val showSsoUnavailable: () -> Unit = {
        coroutineScope.launch { snackbarHostState.showSnackbar(ssoUnavailableMessage) }
    }

    var allowAutoEnter by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated, allowAutoEnter) {
        if (uiState.isAuthenticated && allowAutoEnter) {
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
                    allowAutoEnter = true
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
        containerColor = Vx.Bg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VxBrandMark(size = 60.dp, modifier = Modifier.staggeredEnter(0))
            Text(
                text = stringResource(R.string.iam_login_title),
                modifier = Modifier.padding(top = 12.dp).staggeredEnter(1),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = Vx.Ink,
            )
            Text(
                text = stringResource(R.string.auth_tagline).uppercase(),
                modifier = Modifier.padding(top = 4.dp).staggeredEnter(2),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = Vx.Muted,
            )
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEnter(3)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Vx.Surface)
                    .border(1.dp, Vx.Line, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.auth_welcome_back),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Vx.Ink,
                    )
                    Text(
                        text = stringResource(R.string.auth_welcome_desc),
                        modifier = Modifier.padding(top = 4.dp),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Vx.Muted,
                    )
                }
                VxTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.iam_login_username_label),
                    placeholder = "alex.vance",
                    leadingIcon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.semantics { contentDescription = usernameFieldDescription },
                )
                VxTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.iam_login_password_label),
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    enabled = !uiState.isLoading,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = {
                        allowAutoEnter = true
                        viewModel.signIn(username, password)
                    }),
                    modifier = Modifier.semantics { contentDescription = passwordFieldDescription },
                )
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onForgotPassword)
                        .padding(4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Vx.Ink,
                    textDecoration = TextDecoration.Underline,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VxButton(
                        text = stringResource(R.string.iam_login_sign_in),
                        onClick = {
                            allowAutoEnter = true
                            viewModel.signIn(username, password)
                        },
                        loading = uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = signInButtonDescription },
                    )
                    BiometricButton(
                        enabled = !uiState.isLoading,
                        contentDescription = biometricButtonDescription,
                        onClick = launchBiometricPrompt,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(1.dp).background(Vx.Line))
                    Text(
                        stringResource(R.string.auth_or_continue),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 11.sp,
                        color = Vx.Muted,
                    )
                    Box(Modifier.weight(1f).height(1.dp).background(Vx.Line))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(R.string.auth_sso_microsoft_short, R.string.auth_sso_microsoft, Icons.Outlined.Window),
                        Triple(R.string.auth_sso_google_short, R.string.auth_sso_google, Icons.Outlined.Cloud),
                        Triple(R.string.auth_sso_apple_short, R.string.auth_sso_apple, Icons.Outlined.PhoneIphone),
                    ).forEach { (shortLabel, a11yLabel, icon) ->
                        SsoTile(
                            label = stringResource(shortLabel),
                            contentDescription = stringResource(a11yLabel),
                            icon = icon,
                            enabled = !uiState.isLoading,
                            onClick = showSsoUnavailable,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 18.dp).staggeredEnter(4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.auth_no_account), fontSize = 13.sp, color = Vx.Muted)
                Text(
                    text = stringResource(R.string.auth_create_account),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onCreateAccount)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

@Composable
private fun SsoTile(
    label: String,
    contentDescription: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(14.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Vx.Ink, modifier = Modifier.size(20.dp))
        Text(
            label,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Vx.Ink,
        )
    }
}

@Composable
private fun BiometricButton(enabled: Boolean, contentDescription: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Vx.Chip)
            .border(1.dp, Vx.Line, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = Vx.Ink,
        )
    }
}
