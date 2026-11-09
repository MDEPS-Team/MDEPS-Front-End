@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxTopBar
import java.text.DateFormat
import java.util.Calendar

@Composable
fun CreateInitiativeScreen(
    viewModel: PtoViewModel,
    onClose: () -> Unit,
    onCreated: (Long?) -> Unit,
) {
    val context = LocalContext.current
    val dayMillis = 86_400_000L
    val defaultStart = System.currentTimeMillis()
    var name by rememberSaveable { mutableStateOf("") }
    var brief by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(ProjectStatus.PLANNING) }
    var enableGovernance by rememberSaveable { mutableStateOf(true) }
    var enableEvidence by rememberSaveable { mutableStateOf(true) }
    var riskBand by rememberSaveable { mutableStateOf(CreateRiskBand.MODERATE) }
    var startDateMillis by rememberSaveable { mutableStateOf(defaultStart) }
    var gateDateMillis by rememberSaveable { mutableStateOf(defaultStart + 105L * dayMillis) }
    var selectedDomains by rememberSaveable { mutableStateOf(setOf(CreateDomain.API)) }
    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = { Text(stringResource(R.string.pto_create_nav_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.pto_create_cancel))
                    }
                },
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.pto_create_discard))
                }
                Button(
                    onClick = {
                        val finalStatus = if (riskBand == CreateRiskBand.HIGH) {
                            ProjectStatus.AT_RISK
                        } else {
                            status
                        }
                        viewModel.createProject(
                            name = name,
                            description = buildInitiativeBrief(
                                brief = brief,
                                riskBand = riskBand,
                                governance = enableGovernance,
                                evidence = enableEvidence,
                                domains = selectedDomains,
                                startDateMillis = startDateMillis,
                                gateDateMillis = gateDateMillis,
                            ),
                            status = finalStatus,
                            gateDateMillis = gateDateMillis,
                            seedHighSeverityRisk = riskBand == CreateRiskBand.HIGH,
                            onCreated = { id -> onCreated(id) },
                        )
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    enabled = name.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue),
                ) {
                    Text(stringResource(R.string.pto_create_provision), fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                VxCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Caption(text = stringResource(R.string.pto_create_eyebrow), color = Vx.Blue)
                        Text(
                            text = stringResource(R.string.pto_create_headline),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Vx.Ink,
                        )
                        Text(
                            text = stringResource(R.string.pto_create_subhead),
                            fontSize = 13.sp,
                            color = Vx.Muted,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
            item {
                FormSection(stringResource(R.string.pto_create_section_identity), Icons.Default.Info) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.pto_create_name_label)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = brief,
                        onValueChange = { brief = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        label = { Text(stringResource(R.string.pto_create_description_label)) },
                        minLines = 3,
                    )
                }
            }
            item {
                FormSection(stringResource(R.string.pto_create_section_timeline), Icons.Default.CalendarToday) {
                    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
                    DateField(
                        label = stringResource(R.string.pto_create_start_date),
                        value = dateFormat.format(startDateMillis),
                        onPick = {
                            showDatePicker(context, startDateMillis) { startDateMillis = it }
                        },
                    )
                    DateField(
                        label = stringResource(R.string.pto_create_gate_date),
                        value = dateFormat.format(gateDateMillis),
                        onPick = {
                            showDatePicker(context, gateDateMillis) { gateDateMillis = it }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item {
                FormSection(stringResource(R.string.pto_create_section_governance), Icons.Default.Lock) {
                    statusOptions().forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = { Text(option.localizedLabel()) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                    GovernanceSwitchRow(
                        title = stringResource(R.string.pto_create_soc2_switch),
                        subtitle = stringResource(R.string.pto_create_soc2_switch_desc),
                        checked = enableGovernance,
                        onCheckedChange = { enableGovernance = it },
                    )
                    GovernanceSwitchRow(
                        title = stringResource(R.string.pto_create_evidence_switch),
                        subtitle = stringResource(R.string.pto_create_evidence_switch_desc),
                        checked = enableEvidence,
                        onCheckedChange = { enableEvidence = it },
                    )
                }
            }
            item {
                FormSection(stringResource(R.string.pto_create_section_risk), Icons.Default.Warning) {
                    CreateDomain.entries.forEach { domain ->
                        FilterChip(
                            selected = selectedDomains.contains(domain),
                            onClick = {
                                selectedDomains = if (selectedDomains.contains(domain)) {
                                    selectedDomains - domain
                                } else {
                                    selectedDomains + domain
                                }
                            },
                            label = { Text(stringResource(domain.labelRes)) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                    CreateRiskBand.entries.forEach { band ->
                        FilterChip(
                            selected = riskBand == band,
                            onClick = { riskBand = band },
                            label = { Text(stringResource(band.labelRes)) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                    if (riskBand == CreateRiskBand.HIGH) {
                        Pill(
                            text = stringResource(R.string.pto_create_risk_hint),
                            bg = Vx.RedSoft,
                            fg = Vx.Red,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

private enum class CreateRiskBand(val labelRes: Int) {
    LOW(R.string.pto_create_risk_low),
    MODERATE(R.string.pto_create_risk_moderate),
    HIGH(R.string.pto_create_risk_high),
}

private enum class CreateDomain(val labelRes: Int) {
    BACKEND(R.string.pto_create_domain_backend),
    API(R.string.pto_create_domain_api),
    MOBILE(R.string.pto_create_domain_mobile),
    DATA(R.string.pto_create_domain_data),
}

private fun statusOptions() = listOf(
    ProjectStatus.PLANNING,
    ProjectStatus.IN_PROGRESS,
    ProjectStatus.AT_RISK,
)

private fun buildInitiativeBrief(
    brief: String,
    riskBand: CreateRiskBand,
    governance: Boolean,
    evidence: Boolean,
    domains: Set<CreateDomain>,
    startDateMillis: Long,
    gateDateMillis: Long,
): String {
    val flags = buildList {
        add("risk=${riskBand.name.lowercase()}")
        if (domains.isNotEmpty()) add("domains=${domains.joinToString(",") { it.name.lowercase() }}")
        add("start=$startDateMillis")
        add("gate=$gateDateMillis")
        if (governance) add("soc2=on")
        if (evidence) add("evidence=on")
    }.joinToString(";")
    return listOf(brief.trim(), "[$flags]").filter { it.isNotBlank() }.joinToString("\n")
}

private fun showDatePicker(
    context: android.content.Context,
    initialMillis: Long,
    onDate: (Long) -> Unit,
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val picked = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDate(picked.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    ).show()
}

@Composable
private fun DateField(
    label: String,
    value: String,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPick),
        label = { Text(label) },
    )
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Vx.Blue)
                Caption(text = title, modifier = Modifier.padding(start = 8.dp), color = Vx.Ink)
            }
            Column(Modifier.padding(top = 12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun GovernanceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Vx.Ink)
            Text(text = subtitle, fontSize = 11.sp, color = Vx.Muted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
