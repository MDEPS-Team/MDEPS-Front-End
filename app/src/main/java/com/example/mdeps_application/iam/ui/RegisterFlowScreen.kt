@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.iam.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
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
import com.example.mdeps_application.iam.application.RegistrationField
import com.example.mdeps_application.iam.application.RegistrationStep
import com.example.mdeps_application.iam.application.RegistrationUiState
import com.example.mdeps_application.iam.application.RegistrationViewModel
import com.example.mdeps_application.iam.domain.ExperienceRange
import com.example.mdeps_application.iam.domain.PasswordPolicy
import com.example.mdeps_application.iam.domain.PmoRole
import com.example.mdeps_application.iam.domain.Salutation
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxButtonStyle
import com.example.mdeps_application.ui.VxChoiceChip
import com.example.mdeps_application.ui.VxFieldLabel
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.VxTextField
import com.example.mdeps_application.ui.AnimatedSuccessMark
import com.example.mdeps_application.ui.pressScale
import com.example.mdeps_application.ui.rememberReducedMotion
import com.example.mdeps_application.ui.staggeredEnter
import kotlinx.coroutines.launch

@Composable
fun RegisterFlowScreen(
    viewModel: RegistrationViewModel,
    onExit: () -> Unit,
    onFinished: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ssoUnavailable = stringResource(R.string.auth_sso_unavailable)

    val goBack: () -> Unit = { if (!viewModel.back()) onExit() }
    BackHandler(enabled = state.step != RegistrationStep.DONE) { goBack() }

    state.submitErrorRes?.let { res ->
        val message = stringResource(res)
        LaunchedEffect(res) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSubmitError()
        }
    }

    val stepLabels = listOf(
        stringResource(R.string.reg_step_personal),
        stringResource(R.string.reg_step_contact),
        stringResource(R.string.reg_step_profession),
        stringResource(R.string.reg_step_role),
        stringResource(R.string.reg_step_done),
    )
    val titleRes = when (state.step) {
        RegistrationStep.PERSONAL -> R.string.reg_title_personal
        RegistrationStep.CONTACT -> R.string.reg_title_contact
        RegistrationStep.PROFESSION -> R.string.reg_title_profession
        RegistrationStep.ROLE -> R.string.reg_title_role
        RegistrationStep.DONE -> R.string.reg_title_done
    }

    Scaffold(
        containerColor = Vx.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            AnimatedContent(
                targetState = titleRes,
                transitionSpec = {
                    fadeIn(tween(VxMotion.FAST_MS, easing = VxMotion.EaseOut)) togetherWith fadeOut(tween(120))
                },
                label = "regTitle",
            ) { res ->
                AuthHeader(
                    title = stringResource(res),
                    onBack = if (state.step == RegistrationStep.DONE) null else goBack,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Vx.Line))
            AuthStepper(
                labels = stepLabels,
                currentIndex = state.step.ordinal,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            )
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { stepTransition() },
                modifier = Modifier.weight(1f),
                label = "regStep",
            ) { step ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                ) {
                    when (step) {
                        RegistrationStep.PERSONAL -> PersonalStep(state, viewModel)
                        RegistrationStep.CONTACT -> ContactStep(state, viewModel) {
                            scope.launch { snackbarHostState.showSnackbar(ssoUnavailable) }
                        }
                        RegistrationStep.PROFESSION -> ProfessionStep(state, viewModel)
                        RegistrationStep.ROLE -> RoleStep(state, viewModel)
                        RegistrationStep.DONE -> DoneStep(state, onFinished)
                    }
                }
            }
        }
    }
}

/** Avanzar desliza hacia la izquierda; retroceder, hacia la derecha. Desplazamiento corto + fade. */
private fun <S : Enum<S>> AnimatedContentTransitionScope<S>.stepTransition(): ContentTransform {
    val forward = targetState.ordinal > initialState.ordinal
    val sign = if (forward) 1 else -1
    return (
        slideInHorizontally(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut)) { sign * it / 5 } +
            fadeIn(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut))
        ) togetherWith (
        slideOutHorizontally(tween(VxMotion.FAST_MS, easing = VxMotion.EaseOut)) { -sign * it / 8 } +
            fadeOut(tween(VxMotion.FAST_MS - 60))
        )
}

@Composable
private fun StepCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun errorText(state: RegistrationUiState, field: RegistrationField): String? =
    state.errors[field]?.let { stringResource(it) }

// ───────────────────────── PASO 1 · DATOS PERSONALES ─────────────────────────

@Composable
private fun PersonalStep(state: RegistrationUiState, vm: RegistrationViewModel) {
    val d = state.draft
    StepCard {
        StepIntro(
            eyebrow = stringResource(R.string.reg_eyebrow, 1, 4, stringResource(R.string.reg_topic_identity)),
            title = stringResource(R.string.reg_personal_heading),
            description = stringResource(R.string.reg_personal_desc),
        )
        VxTextField(
            value = d.firstName,
            onValueChange = { v -> vm.update { it.copy(firstName = v) } },
            label = stringResource(R.string.reg_first_name),
            placeholder = "Alejandro",
            error = errorText(state, RegistrationField.FIRST_NAME),
        )
        VxTextField(
            value = d.lastName,
            onValueChange = { v -> vm.update { it.copy(lastName = v) } },
            label = stringResource(R.string.reg_last_name),
            placeholder = "Vance Rivas",
            error = errorText(state, RegistrationField.LAST_NAME),
        )
        Column {
            VxFieldLabel(stringResource(R.string.reg_birth_date))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePartField(d.birthDay, stringResource(R.string.reg_dd), 2, Modifier.weight(1f)) { v ->
                    vm.update { it.copy(birthDay = v) }
                }
                DatePartField(d.birthMonth, stringResource(R.string.reg_mm), 2, Modifier.weight(1f)) { v ->
                    vm.update { it.copy(birthMonth = v) }
                }
                DatePartField(d.birthYear, stringResource(R.string.reg_yyyy), 4, Modifier.weight(1.3f)) { v ->
                    vm.update { it.copy(birthYear = v) }
                }
            }
            errorText(state, RegistrationField.BIRTH_DATE)?.let {
                Text(it, color = Vx.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }
        }
        Column {
            VxFieldLabel(stringResource(R.string.reg_salutation))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Salutation.MR to R.string.reg_salutation_mr,
                    Salutation.MS to R.string.reg_salutation_ms,
                    Salutation.UNSPECIFIED to R.string.reg_salutation_none,
                ).forEach { (value, label) ->
                    VxChoiceChip(
                        text = stringResource(label),
                        selected = d.salutation == value,
                        onClick = { vm.update { it.copy(salutation = value) } },
                    )
                }
            }
        }
        VxButton(
            text = stringResource(R.string.reg_cta_to_contact),
            onClick = vm::next,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DatePartField(
    value: String,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit).take(maxLength)) },
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(placeholder, color = Vx.Muted.copy(alpha = 0.7f)) },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        colors = vxFieldColors(),
    )
}

@Composable
private fun vxFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Vx.Ink,
    unfocusedBorderColor = Vx.Line,
    focusedContainerColor = Vx.Surface,
    unfocusedContainerColor = Vx.Surface,
    cursorColor = Vx.Ink,
    focusedTextColor = Vx.Ink,
    unfocusedTextColor = Vx.Ink,
)

// ───────────────────────── PASO 2 · CONTACTO Y ACCESO ─────────────────────────

@Composable
private fun ContactStep(state: RegistrationUiState, vm: RegistrationViewModel, onSso: () -> Unit) {
    val d = state.draft
    StepCard {
        StepIntro(
            eyebrow = stringResource(R.string.reg_eyebrow, 2, 4, stringResource(R.string.reg_topic_security)),
            title = stringResource(R.string.reg_contact_heading),
            description = null,
        )
        VxButton(
            text = stringResource(R.string.reg_sso_link),
            onClick = onSso,
            style = VxButtonStyle.Secondary,
            leadingIcon = Icons.Outlined.Cloud,
            modifier = Modifier.fillMaxWidth(),
            height = 48.dp,
        )
        OrDivider()
        VxTextField(
            value = d.email,
            onValueChange = { v -> vm.update { it.copy(email = v) } },
            label = stringResource(R.string.reg_email),
            placeholder = "alex.vance@enterprise.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            error = errorText(state, RegistrationField.EMAIL),
        )
        VxTextField(
            value = d.phone,
            onValueChange = { v -> vm.update { it.copy(phone = v.filter { c -> c.isDigit() || c in "+ -" }.take(20)) } },
            label = stringResource(R.string.reg_phone),
            placeholder = "+34 612 890 451",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            error = errorText(state, RegistrationField.PHONE),
        )
        VxTextField(
            value = d.password,
            onValueChange = { v -> vm.update { it.copy(password = v) } },
            label = stringResource(R.string.reg_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            error = errorText(state, RegistrationField.PASSWORD),
        )
        VxTextField(
            value = d.confirmPassword,
            onValueChange = { v -> vm.update { it.copy(confirmPassword = v) } },
            label = stringResource(R.string.reg_confirm_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            error = errorText(state, RegistrationField.CONFIRM_PASSWORD),
        )
        PasswordStrengthMeter(PasswordPolicy.strength(d.password))
        TermsRow(
            checked = d.acceptedTerms,
            error = errorText(state, RegistrationField.TERMS),
            onToggle = { vm.update { it.copy(acceptedTerms = !it.acceptedTerms) } },
        )
        VxButton(
            text = stringResource(R.string.reg_cta_to_profession),
            onClick = vm::next,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(Vx.Line))
        Text(
            stringResource(R.string.auth_or_manual),
            modifier = Modifier.padding(horizontal = 10.dp),
            fontSize = 11.sp,
            color = Vx.Muted,
            fontWeight = FontWeight.Medium,
        )
        Box(Modifier.weight(1f).height(1.dp).background(Vx.Line))
    }
}

@Composable
private fun TermsRow(checked: Boolean, error: String?, onToggle: () -> Unit) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(role = Role.Checkbox, onClick = onToggle)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Vx.Ink, checkmarkColor = Vx.Surface),
            )
            Text(
                stringResource(R.string.reg_terms),
                fontSize = 13.sp,
                color = Vx.Ink,
                lineHeight = 18.sp,
            )
        }
        if (error != null) {
            Text(error, color = Vx.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

// ───────────────────────── PASO 3 · PERFIL PROFESIONAL ─────────────────────────

@Composable
private fun ProfessionStep(state: RegistrationUiState, vm: RegistrationViewModel) {
    val d = state.draft
    StepCard {
        StepIntro(
            eyebrow = stringResource(R.string.reg_eyebrow, 3, 4, stringResource(R.string.reg_topic_career)),
            title = stringResource(R.string.reg_profession_heading),
            description = null,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Vx.Chip)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Vx.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, tint = Vx.Ink)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.reg_roles_banner_eyebrow).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Vx.Muted,
                )
                Text(
                    stringResource(R.string.reg_roles_banner_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
                Text(stringResource(R.string.reg_roles_banner_desc), fontSize = 12.sp, color = Vx.Muted)
            }
        }
        VxDropdown(
            label = stringResource(R.string.reg_position),
            value = d.position,
            options = stringArrayResource(R.array.reg_positions).toList(),
            error = errorText(state, RegistrationField.POSITION),
            onSelect = { v -> vm.update { it.copy(position = v) } },
        )
        VxDropdown(
            label = stringResource(R.string.reg_specialty),
            value = d.specialty,
            options = stringArrayResource(R.array.reg_specialties).toList(),
            error = errorText(state, RegistrationField.SPECIALTY),
            onSelect = { v -> vm.update { it.copy(specialty = v) } },
        )
        Column {
            VxFieldLabel(stringResource(R.string.reg_experience))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ExperienceRange.Y1_3 to "1–3",
                    ExperienceRange.Y4_7 to "4–7",
                    ExperienceRange.Y8_12 to "8–12",
                    ExperienceRange.Y12_PLUS to "+12",
                ).forEach { (value, label) ->
                    VxChoiceChip(
                        text = label,
                        selected = d.experience == value,
                        onClick = { vm.update { it.copy(experience = value) } },
                    )
                }
            }
            errorText(state, RegistrationField.EXPERIENCE)?.let {
                Text(it, color = Vx.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }
        }
        VxButton(
            text = stringResource(R.string.reg_cta_to_role),
            onClick = vm::next,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun VxDropdown(
    label: String,
    value: String,
    options: List<String>,
    error: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        VxFieldLabel(label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = error != null,
                placeholder = { Text(stringResource(R.string.reg_select_placeholder), color = Vx.Muted.copy(alpha = 0.7f)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = vxFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Vx.Surface,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Vx.Ink) },
                        trailingIcon = if (option == value) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = Vx.Ink) }
                        } else {
                            null
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = Vx.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

// ───────────────────────── PASO 4 · SELECCIÓN DE ROL ─────────────────────────

@Composable
private fun RoleStep(state: RegistrationUiState, vm: RegistrationViewModel) {
    val d = state.draft
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        StepIntro(
            eyebrow = stringResource(R.string.reg_eyebrow, 4, 4, stringResource(R.string.reg_topic_access)),
            title = stringResource(R.string.reg_role_heading),
            description = stringResource(R.string.reg_role_desc),
        )
        RoleCard(
            eyebrow = stringResource(R.string.reg_role_leader_eyebrow),
            title = stringResource(R.string.reg_role_leader),
            description = stringResource(R.string.reg_role_leader_desc),
            bullets = listOf(
                stringResource(R.string.reg_role_leader_b1),
                stringResource(R.string.reg_role_leader_b2),
                stringResource(R.string.reg_role_leader_b3),
            ),
            selected = d.role == PmoRole.PROJECT_LEADER,
            onClick = { vm.update { it.copy(role = PmoRole.PROJECT_LEADER) } },
        )
        RoleCard(
            eyebrow = stringResource(R.string.reg_role_owner_eyebrow),
            title = stringResource(R.string.reg_role_owner),
            description = stringResource(R.string.reg_role_owner_desc),
            bullets = listOf(
                stringResource(R.string.reg_role_owner_b1),
                stringResource(R.string.reg_role_owner_b2),
                stringResource(R.string.reg_role_owner_b3),
            ),
            selected = d.role == PmoRole.ENTERPRISE_OWNER,
            onClick = { vm.update { it.copy(role = PmoRole.ENTERPRISE_OWNER) } },
        )
        errorText(state, RegistrationField.ROLE)?.let {
            Text(it, color = Vx.Red, fontSize = 12.sp)
        }
        VxButton(
            text = stringResource(R.string.reg_cta_finish),
            onClick = vm::next,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RoleCard(
    eyebrow: String,
    title: String,
    description: String,
    bullets: List<String>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val borderColor by animateColorAsState(
        if (selected) Vx.Ink else Vx.Line,
        tween(VxMotion.FAST_MS),
        label = "roleBorder",
    )
    val bg by animateColorAsState(
        if (selected) Vx.Surface else Vx.Chip.copy(alpha = 0.6f),
        tween(VxMotion.FAST_MS),
        label = "roleBg",
    )
    val bulletsAlpha by animateFloatAsState(
        if (selected) 1f else 0.55f,
        tween(VxMotion.FAST_MS),
        label = "bulletsAlpha",
    )
    Column(
        Modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.98f)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(BorderStroke(if (selected) 2.dp else 1.dp, borderColor), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null, role = Role.RadioButton, onClick = onClick)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = Vx.Muted)
                Text(
                    title,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
            }
            RadioDot(selected)
        }
        Text(description, modifier = Modifier.padding(top = 8.dp), fontSize = 13.sp, color = Vx.Muted, lineHeight = 18.sp)
        Column(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .graphicsLayer { alpha = bulletsAlpha }
                .clip(RoundedCornerShape(12.dp))
                .background(Vx.Chip)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            bullets.forEach { bullet ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (selected) Vx.Ink else Vx.Muted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(bullet, fontSize = 12.sp, color = Vx.Ink)
                }
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val inner by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "radio",
    )
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) Vx.Ink else Vx.Gray, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = 0.4f + 0.6f * inner
                    scaleY = 0.4f + 0.6f * inner
                    alpha = inner
                }
                .size(12.dp)
                .clip(CircleShape)
                .background(Vx.Ink),
        )
    }
}

// ───────────────────────── PASO 5 · ÉXITO ─────────────────────────

@Composable
private fun DoneStep(state: RegistrationUiState, onFinished: () -> Unit) {
    val d = state.draft
    val user = state.registeredUser
    val roleLabel = stringResource(
        if (d.role == PmoRole.ENTERPRISE_OWNER) R.string.reg_role_owner else R.string.reg_role_leader,
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Vx.Surface)
                .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedSuccessMark(size = 76.dp)
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .staggeredEnter(0, baseDelayMs = 380)
                    .clip(RoundedCornerShape(50))
                    .background(Vx.Ink)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    stringResource(R.string.reg_done_badge).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Vx.Surface,
                )
            }
            Text(
                stringResource(R.string.reg_done_title),
                modifier = Modifier.padding(top = 12.dp).staggeredEnter(1, baseDelayMs = 380),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.reg_done_desc),
                modifier = Modifier.padding(top = 6.dp).staggeredEnter(2, baseDelayMs = 380),
                fontSize = 14.sp,
                color = Vx.Muted,
                textAlign = TextAlign.Center,
            )
            Row(
                Modifier
                    .padding(top = 16.dp)
                    .staggeredEnter(3, baseDelayMs = 380)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Vx.Chip)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.reg_done_workspace),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
                LivePill(stringResource(R.string.reg_done_active))
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Vx.Chip)
                .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.reg_done_summary).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = Vx.Muted,
            )
            listOf(
                stringResource(R.string.reg_done_user) to (user?.name ?: d.fullName),
                stringResource(R.string.reg_done_email) to (user?.email ?: d.email),
                stringResource(R.string.reg_done_role) to roleLabel,
                stringResource(R.string.reg_done_space) to stringResource(R.string.reg_done_space_value),
            ).forEachIndexed { index, (label, value) ->
                SummaryRow(label, value, Modifier.staggeredEnter(index, baseDelayMs = 600))
            }
        }
        VxButton(
            text = stringResource(R.string.reg_cta_enter),
            onClick = onFinished,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth().staggeredEnter(0, baseDelayMs = 820),
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Vx.Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.sp, color = Vx.Muted, letterSpacing = 0.4.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Vx.Ink, maxLines = 1)
        }
        Icon(Icons.Default.Check, contentDescription = null, tint = Vx.Green, modifier = Modifier.size(16.dp))
    }
}

/** Pill con un punto "en vivo" que respira suavemente. */
@Composable
fun LivePill(text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Vx.Ink)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4ADE80)))
        Spacer(Modifier.width(6.dp))
        Text(text.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Surface)
    }
}
