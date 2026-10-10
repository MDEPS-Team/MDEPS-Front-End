@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.mdeps_application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── PANTALLA: CREAR NUEVA INICIATIVA ─────────────────────────
private data class Member(val initials: String, val name: String, val role: String)

@Composable
fun CreateProjectScreen(onClose: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("Zero Trust Data Lake & Audit Vault") }
    var brief by rememberSaveable {
        mutableStateOf("Despliegue unificado de auditoría distribuida con cifrado homomórfico para telemetría transaccional bancaria e interconexión multirregión con soberanía de datos GDPR y DORA.")
    }
    var status by rememberSaveable { mutableStateOf("Planning / Kick-off") }
    var risk by rememberSaveable { mutableStateOf("Moderate") }
    var soc2 by rememberSaveable { mutableStateOf(true) }
    var burndown by rememberSaveable { mutableStateOf(true) }
    var domains by remember { mutableStateOf(setOf("Backend / Core Engine", "API Gateway & Security")) }
    var showDone by remember { mutableStateOf(false) }

    val pool = remember {
        listOf(Member("SK", "S. Kim", "Data"), Member("LP", "L. Pérez", "QA"), Member("DN", "D. Novak", "Infra"))
    }
    val members = remember {
        mutableStateListOf(
            Member("AV", "Alejandro Vance", "Lead"), Member("KL", "K. Leung", "SecOps"),
            Member("TA", "T. Alvarez", "Arch"), Member("MR", "M. Ramos", "Comp")
        )
    }

    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = { Text("Create Project", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") } },
            actions = { ProfileButton {} }
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera
            VxCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Vx.Blue), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("VANTAGE PMO • PROVISIONING", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Blue, letterSpacing = 0.5.sp)
                        IconButton(onClick = onClose, Modifier.size(32.dp)) { Icon(Icons.Default.Close, "Cerrar", Modifier.size(18.dp)) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Crear Nueva Iniciativa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                    Text("Aprovisionamiento de portafolio y gobernanza institucional enterprise", fontSize = 13.sp, color = Vx.Muted, lineHeight = 18.sp)
                }
            }

            // 1
            Section("1. Organization Scope & Entity", Icons.Default.Home, trailing = { Pill("ID: ESG-GLOBAL-01", Vx.Chip, Vx.Muted, size = 10.sp) }) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Vx.Line, RoundedCornerShape(12.dp))
                        .clickable { }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Vx.Blue), contentAlignment = Alignment.Center) {
                        Text("ESG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Enterprise Solutions Global SL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Vx.Ink, maxLines = 1)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, null, tint = Vx.Blue, modifier = Modifier.size(14.dp))
                        }
                        Text("Master Tenant • Tier-1 Core Financial Infras", fontSize = 11.sp, color = Vx.Muted, maxLines = 1)
                    }
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Vx.Muted)
                }
            }

            // 2
            Section("2. Project Identification", Icons.Default.Info) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    prefix = { Text("INIT-2025-04  ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Vx.Blue, fontWeight = FontWeight.Bold) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Caption("Project Executive Brief", Modifier.weight(1f))
                    Text("SLA 99.999%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Green)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = brief, onValueChange = { brief = it }, modifier = Modifier.fillMaxWidth(),
                    minLines = 4, shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
                )
            }

            // 3
            Section("3. Participants & Squad Allocation", Icons.Default.Person, trailing = { Text("4 Leads Assigned", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Blue) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    members.forEach { m ->
                        AssistChip(
                            onClick = { },
                            label = { Text(m.name + "  " + m.role, fontSize = 12.sp) },
                            leadingIcon = { Avatar(m.initials, 24.dp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    AssistChip(
                        onClick = { if (members.size - 4 < pool.size) members.add(pool[members.size - 4]) },
                        label = { Text("Add Member", fontSize = 12.sp, color = Vx.Blue) },
                        leadingIcon = { Icon(Icons.Default.Add, null, tint = Vx.Blue, modifier = Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // 4
            Section("4. Timeline & Sprint Windows", Icons.Default.DateRange, trailing = { Pill("105 Days • 7 Active Sprints", Vx.BlueSoft, Vx.Blue, size = 10.sp) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateBox("START DATE", "15 Nov 2024", Modifier.weight(1f))
                    DateBox("TARGET GATE DATE", "28 Feb 2025", Modifier.weight(1f))
                }
            }

            // 5
            Section("5. Governance Status", Icons.Default.CheckCircle) {
                val opts = listOf("Planning / Kick-off", "Active In-Flight", "Under Review", "Draft")
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Vx.Chip).padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    opts.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            pair.forEach { o ->
                                val sel = o == status
                                Box(
                                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) Vx.Blue else Color.Transparent)
                                        .clickable { status = o }.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(o, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, color = if (sel) Color.White else Vx.Ink)
                                }
                            }
                        }
                    }
                }
            }

            // 6
            Section("6. Risk Domain Classification", Icons.Default.Warning) {
                val all = listOf("Backend / Core Engine", "API Gateway & Security", "Frontend & UI Mobile", "DevOps & Infra", "Compliance Gate")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    all.forEach { d ->
                        val sel = d in domains
                        FilterChip(
                            selected = sel,
                            onClick = { domains = if (sel) domains - d else domains + d },
                            label = { Text(d, fontSize = 12.sp) },
                            leadingIcon = if (sel) ({ Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }) else null,
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Vx.Blue, selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White, containerColor = Vx.Chip
                            ),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Vx.Bg).padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expected Risk Matrix", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Vx.Ink)
                    listOf("Low", "Moderate", "High").forEach { r ->
                        val sel = r == risk
                        Box(
                            Modifier.padding(start = 4.dp).clip(RoundedCornerShape(6.dp))
                                .background(if (sel) Vx.BlueSoft else Color.Transparent)
                                .clickable { risk = r }.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (sel) "• $r" else r, fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                color = if (sel) Vx.Blue else Vx.Muted
                            )
                        }
                    }
                }
            }

            // 7
            Section("7. Governance & SLA Compliance Switches", Icons.Default.Lock) {
                SwitchRow(Icons.Default.CheckCircle, "Enable SOC2 / ISO 27001 automated trace", "Genera evidencias continuas para auditoría regulatoria", soc2) { soc2 = it }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Vx.Line)
                SwitchRow(Icons.Default.Info, "Auto-link to Executive Burndown Telemetry", "Sincronización en tiempo real con Jira Cloud & GitHub", burndown) { burndown = it }
            }
            Spacer(Modifier.height(4.dp))
        }

        // Barra de acciones inferior
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text("Descartar", color = Vx.Ink, fontWeight = FontWeight.Medium) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { showDone = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue)
                ) {
                    Text("Crear Proyecto & Aprovisionar", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDone) {
        AlertDialog(
            onDismissRequest = { showDone = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Vx.Green) },
            title = { Text("Proyecto aprovisionado", fontWeight = FontWeight.Bold) },
            text = { Text("\"$name\" fue creado en estado $status con ${domains.size} dominios de riesgo (matriz: $risk).") },
            confirmButton = { TextButton(onClick = { showDone = false; onClose() }) { Text("Aceptar", color = Vx.Blue) } }
        )
    }
}

@Composable
private fun Section(
    title: String,
    icon: ImageVector,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(icon, null, tint = Vx.Blue, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                Spacer(Modifier.width(8.dp))
                Caption(title, Modifier.weight(1f), color = Vx.Ink)
                if (trailing != null) { Spacer(Modifier.width(8.dp)); trailing() }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DateBox(label: String, value: String, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(10.dp)).background(Vx.Bg).clickable { }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Muted)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
        }
        Icon(Icons.Default.DateRange, null, tint = Vx.Blue, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Vx.Green, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink, maxLines = 1)
            Text(subtitle, fontSize = 11.sp, color = Vx.Muted, maxLines = 2)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Vx.Blue, checkedThumbColor = Color.White)
        )
    }
}
