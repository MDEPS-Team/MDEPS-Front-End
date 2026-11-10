@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pm.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mdeps_application.R
import com.example.mdeps_application.pm.application.ProfileViewModel
import kotlinx.coroutines.launch
import com.example.mdeps_application.pm.domain.ProfileDigestFrequency
import com.example.mdeps_application.pm.domain.ProfileMethodology
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.LocalAppSnackbarHostState
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxTopBar

private val ProfileHeroGradient = Brush.verticalGradient(
    listOf(Color(0xFF0B57D0), Color(0xFF063B8E)),
)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onOpenAuditTrail: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user = state.user
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var showSecuritySheet by rememberSaveable { mutableStateOf(false) }

    val fallbackName = stringResource(R.string.home_greeting_fallback_name)
    val fallbackRole = stringResource(R.string.iam_profile_role_fallback)
    val displayName = user?.name?.takeIf { it.isNotBlank() }
        ?: user?.username?.takeIf { it.isNotBlank() }
        ?: fallbackName
    val displayUsername = user?.username?.takeIf { it.isNotBlank() }.orEmpty()
    val displayRole = user?.role?.takeIf { it.isNotBlank() } ?: fallbackRole
    val displayEmail = user?.email?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.iam_profile_email_unavailable)

    val avatarUri = user?.avatarUrl?.let(Uri::parse)
    val pickAvatar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            viewModel.saveProfileAvatar(context.contentResolver, uri)
        }
    }

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Caption(text = stringResource(R.string.iam_login_title), color = Vx.Blue)
                        Text(
                            text = stringResource(R.string.iam_profile_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Vx.Ink,
                        )
                    }
                },
                actions = {
                    if (state.sessionActive) {
                        Pill(
                            text = stringResource(R.string.gro_governance_active),
                            bg = Vx.GreenSoft,
                            fg = Vx.Green,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileHeroCard(
                name = displayName,
                username = displayUsername,
                role = displayRole,
                organization = state.operational.workspace.organization,
                location = state.operational.workspace.location,
                floor = state.operational.workspace.floorLabel,
                avatarUri = avatarUri,
                onChangeAvatar = {
                    pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileKpiCard(
                    label = stringResource(R.string.iam_profile_kpi_projects),
                    value = state.metrics.projectCount.toString(),
                    subtitle = stringResource(R.string.iam_profile_kpi_on_track, state.metrics.projectsOnTrack),
                    icon = { Icon(Icons.Default.Summarize, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                )
                ProfileKpiCard(
                    label = stringResource(R.string.iam_profile_kpi_sla),
                    value = stringResource(R.string.iam_profile_kpi_sla_value, state.metrics.slaAdherencePercent),
                    subtitle = stringResource(R.string.iam_profile_kpi_sla_delta, state.metrics.slaDeltaPercent),
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                )
                ProfileKpiCard(
                    label = stringResource(R.string.iam_profile_kpi_team),
                    value = state.metrics.teamMemberCount.toString(),
                    subtitle = stringResource(R.string.iam_profile_kpi_pods, state.metrics.podCount),
                    icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                )
            }

            AccountSecuritySection(
                username = displayUsername,
                email = state.draftEmail.ifBlank { displayEmail },
                passwordDaysAgo = state.passwordDaysAgo,
                onEdit = { showSecuritySheet = true },
                onPassphraseClick = { showSecuritySheet = true },
            )

            PmoPreferencesSection(
                timezoneKey = state.operational.timezoneKey,
                methodology = state.operational.methodology,
                digest = state.operational.digest,
                darkStudio = state.operational.darkStudioTheme,
                onTimezone = viewModel::setTimezone,
                onMethodology = viewModel::setMethodology,
                onDigest = viewModel::setDigest,
                onDarkStudio = viewModel::setDarkStudio,
            )

            ProfileLanguagePreferenceSection()

            AccessGovernanceCard(
                masterRole = displayRole,
                tenantId = viewModel.vantageTenantId(),
                auditRecords = state.auditRecordCount,
                onOpenAudit = onOpenAuditTrail,
            )

            Button(
                onClick = {
                    viewModel.saveProfile()
                    scope.launch {
                        snackbar?.showSnackbar(context.getString(R.string.iam_profile_saved))
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text(
                        stringResource(R.string.iam_profile_save),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Vx.Red)
                Text(
                    stringResource(R.string.iam_profile_logout),
                    modifier = Modifier.padding(start = 8.dp),
                    color = Vx.Red,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showSecuritySheet) {
        SecuritySheet(
            corporateEmail = state.draftEmail.ifBlank { user?.email.orEmpty() },
            onDismiss = { showSecuritySheet = false },
            onEmailChange = viewModel::setDraftEmail,
        )
    }
}

@Composable
private fun ProfileHeroCard(
    name: String,
    username: String,
    role: String,
    organization: String,
    location: String,
    floor: String,
    avatarUri: Uri?,
    onChangeAvatar: () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(ProfileHeroGradient),
            )
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 0.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Vx.BlueSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = stringResource(R.string.iam_a11y_profile_avatar),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Vx.Blue, modifier = Modifier.size(48.dp))
                    }
                    IconButton(
                        onClick = onChangeAvatar,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Vx.Blue),
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.iam_profile_change_avatar), tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Pill(
                        text = stringResource(R.string.iam_profile_verified_badge).uppercase(),
                        bg = Vx.GreenSoft,
                        fg = Vx.Green,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally), color = Vx.Ink)
                if (username.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.iam_profile_username_handle, username),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Vx.Muted,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp),
                    )
                }
                Text(
                    role,
                    color = Vx.Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetaChip(Icons.Default.Business, organization, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetaChip(Icons.Default.LocationOn, location, Modifier.weight(1f))
                    MetaChip(Icons.Default.Layers, floor, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(16.dp))
        Text(
            text,
            modifier = Modifier.padding(start = 6.dp),
            fontSize = 11.sp,
            color = Vx.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileKpiCard(
    label: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    VxCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption(text = label)
                icon()
            }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(top = 6.dp))
            Text(subtitle, fontSize = 11.sp, color = Vx.Green, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun AccountSecuritySection(
    username: String,
    email: String,
    passwordDaysAgo: Int,
    onEdit: () -> Unit,
    onPassphraseClick: () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.iam_profile_account_security), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Vx.Ink)
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.iam_profile_edit), color = Vx.Blue, fontWeight = FontWeight.SemiBold)
                }
            }
            if (username.isNotBlank()) {
                SecurityRow(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.iam_profile_username_label),
                    subtitle = stringResource(R.string.iam_profile_username_handle, username),
                    trailing = {},
                )
                HorizontalDivider(color = Vx.Line)
            }
            SecurityRow(
                icon = Icons.Default.Email,
                title = stringResource(R.string.iam_profile_corp_email),
                subtitle = email,
                trailing = { Icon(Icons.Default.Lock, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(16.dp)) },
            )
            HorizontalDivider(color = Vx.Line)
            SecurityRow(
                icon = Icons.Default.VerifiedUser,
                title = stringResource(R.string.iam_profile_sso_title),
                subtitle = stringResource(R.string.iam_profile_sso_sub),
                trailing = {
                    Pill(text = stringResource(R.string.iam_profile_status_active), bg = Vx.BlueSoft, fg = Vx.Blue)
                },
            )
            HorizontalDivider(color = Vx.Line)
            SecurityRow(
                icon = Icons.Default.Shield,
                title = stringResource(R.string.iam_profile_2fa_title),
                subtitle = stringResource(R.string.iam_profile_2fa_sub),
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Vx.Green, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.iam_profile_status_enforced),
                            color = Vx.Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
            HorizontalDivider(color = Vx.Line)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPassphraseClick)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Vx.Blue, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(stringResource(R.string.iam_profile_passphrase_title), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        stringResource(R.string.iam_profile_passphrase_sub, passwordDaysAgo),
                        fontSize = 12.sp,
                        color = Vx.Muted,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Vx.Muted)
            }
        }
    }
}

@Composable
private fun SecurityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Vx.Blue, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = Vx.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        trailing()
    }
}

@Composable
private fun PmoPreferencesSection(
    timezoneKey: String,
    methodology: ProfileMethodology,
    digest: ProfileDigestFrequency,
    darkStudio: Boolean,
    onTimezone: (String) -> Unit,
    onMethodology: (ProfileMethodology) -> Unit,
    onDigest: (ProfileDigestFrequency) -> Unit,
    onDarkStudio: (Boolean) -> Unit,
) {
    val timezoneOptions = listOf(
        "utc_plus_1" to stringResource(R.string.iam_profile_tz_madrid),
        "utc_0" to stringResource(R.string.iam_profile_tz_london),
        "utc_minus_5" to stringResource(R.string.iam_profile_tz_new_york),
    )

    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.iam_profile_pmo_prefs), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Vx.Ink)

            Caption(text = stringResource(R.string.iam_profile_timezone_label))
            timezoneOptions.forEach { (key, label) ->
                val selected = key == timezoneKey
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Vx.BlueSoft else Vx.Chip)
                        .clickable { onTimezone(key) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Vx.Blue, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        label,
                        modifier = Modifier.padding(start = if (selected) 8.dp else 0.dp),
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = Vx.Ink,
                    )
                }
            }

            Caption(text = stringResource(R.string.iam_profile_methodology_label))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = methodology == ProfileMethodology.HYBRID,
                    onClick = { onMethodology(ProfileMethodology.HYBRID) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) {
                    Text(stringResource(R.string.iam_profile_methodology_hybrid), fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = methodology == ProfileMethodology.WATERFALL,
                    onClick = { onMethodology(ProfileMethodology.WATERFALL) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) {
                    Text(stringResource(R.string.iam_profile_methodology_waterfall), fontSize = 11.sp)
                }
            }

            Caption(text = stringResource(R.string.iam_profile_digest_label))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = digest == ProfileDigestFrequency.DAILY,
                    onClick = { onDigest(ProfileDigestFrequency.DAILY) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) {
                    Text(stringResource(R.string.iam_profile_digest_daily), fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = digest == ProfileDigestFrequency.WEEKLY,
                    onClick = { onDigest(ProfileDigestFrequency.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) {
                    Text(stringResource(R.string.iam_profile_digest_weekly), fontSize = 11.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = Vx.Blue)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(stringResource(R.string.iam_profile_dark_studio), fontWeight = FontWeight.SemiBold, color = Vx.Ink)
                    Text(stringResource(R.string.iam_profile_dark_studio_sub), fontSize = 12.sp, color = Vx.Muted)
                }
                Switch(
                    checked = darkStudio,
                    onCheckedChange = onDarkStudio,
                    colors = SwitchDefaults.colors(checkedTrackColor = Vx.Blue),
                )
            }
        }
    }
}

@Composable
private fun AccessGovernanceCard(
    masterRole: String,
    tenantId: String,
    auditRecords: Int,
    onOpenAudit: () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth(), containerColor = Vx.BlueSoft) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = Vx.Blue)
                Text(
                    stringResource(R.string.iam_profile_access_title),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Pill(text = stringResource(R.string.iam_profile_tier_badge), bg = Vx.Surface, fg = Vx.Blue)
            }
            Text(
                masterRole,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp),
                color = Vx.Ink,
            )
            Text(stringResource(R.string.iam_profile_master_role_desc), fontSize = 12.sp, color = Vx.Muted)
            Text(
                stringResource(R.string.iam_profile_suite_version),
                fontSize = 11.sp,
                color = Vx.Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(tenantId, fontWeight = FontWeight.Bold, color = Vx.Blue, fontSize = 13.sp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Vx.Surface)
                    .clickable(onClick = onOpenAudit)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.iam_profile_audit_link), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Text(
                    stringResource(R.string.iam_profile_audit_records, auditRecords),
                    color = Vx.Muted,
                    fontSize = 12.sp,
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Vx.Muted)
            }
        }
    }
}
