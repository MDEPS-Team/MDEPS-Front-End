package com.example.mdeps_application.iam.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.application.PasswordRecoveryViewModel
import com.example.mdeps_application.iam.application.RecoveryStep
import com.example.mdeps_application.iam.domain.PasswordPolicy
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxButtonStyle
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.VxTextField
import com.example.mdeps_application.ui.AnimatedSuccessMark
import com.example.mdeps_application.ui.rememberReducedMotion
import com.example.mdeps_application.ui.staggeredEnter
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel,
    entering: Boolean,
    onExit: () -> Unit,
    onEnterApp: (email: String, password: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ssoUnavailable = stringResource(R.string.auth_sso_unavailable)
    val smsUnavailable = stringResource(R.string.rec_sms_unavailable)

    val goBack: () -> Unit = { if (!viewModel.back()) onExit() }
    BackHandler(enabled = state.step != RecoveryStep.DONE) { goBack() }

    val titleRes = when (state.step) {
        RecoveryStep.EMAIL -> R.string.rec_title_email
        RecoveryStep.CODE -> R.string.rec_title_code
        RecoveryStep.NEW_PASSWORD -> R.string.rec_title_password
        RecoveryStep.DONE -> R.string.rec_title_done
    }
    val labels = listOf(
        stringResource(R.string.rec_step_email),
        stringResource(R.string.rec_step_code),
        stringResource(R.string.rec_step_password),
    )

    Scaffold(containerColor = Vx.Bg, snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            AnimatedContent(
                targetState = titleRes,
                transitionSpec = { fadeIn(tween(VxMotion.FAST_MS)) togetherWith fadeOut(tween(120)) },
                label = "recTitle",
            ) { res ->
                AuthHeader(
                    title = stringResource(res),
                    onBack = if (state.step == RecoveryStep.DONE) onExit else goBack,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Vx.Line))
            AuthStepper(
                labels = labels,
                // En la pantalla de éxito las 3 etapas aparecen completadas.
                currentIndex = state.step.ordinal,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            )
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val sign = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (
                        slideInHorizontally(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut)) { sign * it / 5 } +
                            fadeIn(tween(VxMotion.STANDARD_MS))
                        ) togetherWith (
                        slideOutHorizontally(tween(VxMotion.FAST_MS)) { -sign * it / 8 } + fadeOut(tween(140))
                        )
                },
                modifier = Modifier.weight(1f),
                label = "recStep",
            ) { step ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val error = state.errorRes?.let { stringResource(it) }
                    when (step) {
                        RecoveryStep.EMAIL -> RecoveryCard {
                            CenteredIconBadge(Icons.Default.LockReset)
                            CenteredIntro(
                                stringResource(R.string.rec_email_heading),
                                stringResource(R.string.rec_email_desc),
                            )
                            VxButton(
                                text = stringResource(R.string.rec_sso),
                                onClick = { scope.launch { snackbarHostState.showSnackbar(ssoUnavailable) } },
                                style = VxButtonStyle.Secondary,
                                leadingIcon = Icons.Outlined.Cloud,
                                height = 48.dp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            VxTextField(
                                value = state.email,
                                onValueChange = viewModel::onEmailChange,
                                label = stringResource(R.string.rec_email_label),
                                placeholder = "alex.vance@enterprise.com",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Send,
                                error = error,
                            )
                            VxButton(
                                text = stringResource(R.string.rec_send_code),
                                onClick = viewModel::sendCode,
                                loading = state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextLink(stringResource(R.string.rec_remembered), onExit)
                        }

                        RecoveryStep.CODE -> {
                            RecoveryCard {
                                Text(
                                    stringResource(R.string.rec_code_eyebrow).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = Vx.Muted,
                                )
                                Text(
                                    stringResource(R.string.rec_code_heading),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Vx.Ink,
                                )
                                Text(
                                    stringResource(R.string.rec_code_desc, state.email.trim()),
                                    fontSize = 14.sp,
                                    color = Vx.Muted,
                                    lineHeight = 20.sp,
                                )
                                OtpInput(
                                    value = state.code,
                                    length = viewModel.codeLength,
                                    isError = error != null,
                                    onValueChange = viewModel::onCodeChange,
                                )
                                if (error != null) Text(error, color = Vx.Red, fontSize = 12.sp)
                                ResendRow(
                                    secondsLeft = state.resendSecondsLeft,
                                    onResend = viewModel::resendCode,
                                )
                                VxButton(
                                    text = stringResource(R.string.rec_verify),
                                    onClick = viewModel::verifyCode,
                                    loading = state.isLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                TextLink(stringResource(R.string.rec_sms)) {
                                    scope.launch { snackbarHostState.showSnackbar(smsUnavailable) }
                                }
                            }
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Vx.Chip)
                                    .border(1.dp, Vx.Line, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                            ) {
                                Text(
                                    stringResource(R.string.rec_support_title),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Vx.Ink,
                                )
                                Text(
                                    stringResource(R.string.rec_support_desc),
                                    fontSize = 12.sp,
                                    color = Vx.Muted,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }

                        RecoveryStep.NEW_PASSWORD -> RecoveryCard {
                            IconBadge(Icons.Outlined.Shield)
                            Text(
                                stringResource(R.string.rec_password_heading),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Vx.Ink,
                            )
                            Text(stringResource(R.string.rec_password_desc), fontSize = 13.sp, color = Vx.Muted)
                            VxTextField(
                                value = state.newPassword,
                                onValueChange = viewModel::onNewPasswordChange,
                                label = stringResource(R.string.rec_new_password),
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                            )
                            PasswordChecklist(state.newPassword)
                            VxTextField(
                                value = state.confirmPassword,
                                onValueChange = viewModel::onConfirmPasswordChange,
                                label = stringResource(R.string.rec_confirm_password),
                                leadingIcon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                imeAction = ImeAction.Done,
                                error = error,
                            )
                            var signOutOthers by rememberSaveable { mutableStateOf(true) }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(role = Role.Checkbox) { signOutOthers = !signOutOthers },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = signOutOthers,
                                    onCheckedChange = { signOutOthers = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Vx.Ink, checkmarkColor = Vx.Surface),
                                )
                                Column {
                                    Text(stringResource(R.string.rec_sign_out_all), fontSize = 13.sp, color = Vx.Ink)
                                    Text(stringResource(R.string.rec_sign_out_all_desc), fontSize = 12.sp, color = Vx.Muted)
                                }
                            }
                            VxButton(
                                text = stringResource(R.string.rec_update_password),
                                onClick = viewModel::savePassword,
                                loading = state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        RecoveryStep.DONE -> RecoveryCard {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                AnimatedSuccessMark(size = 72.dp)
                            }
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .staggeredEnter(0, 380)
                                        .clip(RoundedCornerShape(50))
                                        .background(Vx.Ink)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.rec_done_badge).uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Vx.Surface,
                                    )
                                }
                            }
                            Column(Modifier.staggeredEnter(1, 380)) {
                                CenteredIntro(
                                    stringResource(R.string.rec_done_heading),
                                    stringResource(R.string.rec_done_desc),
                                )
                            }
                            SecurityLog(Modifier.staggeredEnter(2, 380))
                            VxButton(
                                text = stringResource(R.string.rec_enter),
                                onClick = { onEnterApp(state.email.trim(), state.newPassword) },
                                loading = entering,
                                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                modifier = Modifier.fillMaxWidth().staggeredEnter(3, 380),
                            )
                            VxButton(
                                text = stringResource(R.string.rec_back_to_login),
                                onClick = onExit,
                                style = VxButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth().staggeredEnter(4, 380),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Vx.Chip),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Vx.Ink, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun CenteredIconBadge(icon: ImageVector) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { IconBadge(icon) }
}

@Composable
private fun CenteredIntro(title: String, desc: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Vx.Ink, textAlign = TextAlign.Center)
        Text(
            desc,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 13.sp,
            color = Vx.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun TextLink(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            fontSize = 13.sp,
            color = Vx.Muted,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Seis casillas sobre un único BasicTextField oculto: pegar el código completo funciona. */
@Composable
private fun OtpInput(value: String, length: Int, isError: Boolean, onValueChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    val reduced = rememberReducedMotion()
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError && !reduced) {
            for (offset in listOf(10f, -8f, 6f, -4f, 0f)) shake.animateTo(offset, tween(50))
        }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .focusRequester(focus)
            .graphicsLayer { translationX = shake.value.dp.toPx() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        singleLine = true,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(length) { i ->
                    val char = value.getOrNull(i)?.toString().orEmpty()
                    val isCurrent = i == value.length
                    val border by animateColorAsState(
                        when {
                            isError -> Vx.Red
                            isCurrent -> Vx.Ink
                            char.isNotEmpty() -> Vx.Gray
                            else -> Vx.Line
                        },
                        tween(VxMotion.FAST_MS),
                        label = "otpBorder$i",
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(0.82f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (char.isEmpty()) Vx.Chip.copy(alpha = 0.5f) else Vx.Surface)
                            .border(if (isCurrent) 2.dp else 1.dp, border, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = char,
                            transitionSpec = {
                                (fadeIn(tween(VxMotion.FAST_MS)) + androidx.compose.animation.scaleIn(
                                    tween(VxMotion.FAST_MS, easing = VxMotion.EaseOut),
                                    initialScale = 0.7f,
                                )) togetherWith fadeOut(tween(80))
                            },
                            label = "otpChar$i",
                        ) { c ->
                            Text(
                                text = c.ifEmpty { "•" },
                                fontSize = if (c.isEmpty()) 14.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (c.isEmpty()) Vx.Gray else Vx.Ink,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ResendRow(secondsLeft: Int, onResend: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Vx.Chip)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (secondsLeft > 0) {
                stringResource(R.string.rec_resend_in, secondsLeft / 60, secondsLeft % 60)
            } else {
                stringResource(R.string.rec_resend_ready)
            },
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = Vx.Ink,
        )
        Text(
            stringResource(R.string.rec_resend_now),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(enabled = secondsLeft == 0, onClick = onResend)
                .padding(4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (secondsLeft == 0) Vx.Ink else Vx.Muted,
        )
    }
}

@Composable
private fun PasswordChecklist(password: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Vx.Chip)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PasswordStrengthMeter(PasswordPolicy.strength(password))
        Spacer(Modifier.height(2.dp))
        ChecklistItem(stringResource(R.string.rec_rule_length), password.length >= PasswordPolicy.MIN_LENGTH)
        ChecklistItem(
            stringResource(R.string.rec_rule_case),
            password.any { it.isUpperCase() } && password.any { it.isLowerCase() } && password.any { it.isDigit() },
        )
        ChecklistItem(stringResource(R.string.rec_rule_symbol), password.any { !it.isLetterOrDigit() })
    }
}

@Composable
private fun ChecklistItem(text: String, met: Boolean) {
    val tint by animateColorAsState(if (met) Vx.Green else Vx.Muted, tween(VxMotion.FAST_MS), label = "rule")
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedContent(
            targetState = met,
            transitionSpec = {
                (fadeIn(tween(VxMotion.FAST_MS)) + androidx.compose.animation.scaleIn(initialScale = 0.6f)) togetherWith
                    fadeOut(tween(80))
            },
            label = "ruleIcon",
        ) { ok ->
            Icon(
                if (ok) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (met) Vx.Ink else Vx.Muted)
    }
}

@Composable
private fun SecurityLog(modifier: Modifier = Modifier) {
    val device = remember {
        val maker = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) Build.MODEL else "$maker ${Build.MODEL}"
    }
    val time = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Vx.Chip)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(R.string.rec_log_title).uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = Vx.Muted,
        )
        LogRow(Icons.Default.PhoneAndroid, device)
        LogRow(Icons.Default.Lock, stringResource(R.string.rec_log_time, time))
        LogRow(Icons.Outlined.Shield, stringResource(R.string.rec_log_protocol))
    }
}

@Composable
private fun LogRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Vx.Ink)
    }
}
