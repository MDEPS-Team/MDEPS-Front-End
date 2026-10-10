@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── MOCK DATA ─────────────────────────
data class Project(val name: String, val status: String, val progress: Float, val owner: String)

val mockProjects = listOf(
    Project("Implementación ERP", "En progreso", 0.62f, "A. Vance"),
    Project("Campaña Marketing", "En riesgo", 0.38f, "M. Ramos"),
    Project("Migración Cloud", "En progreso", 0.81f, "K. Leung"),
    Project("Zero Trust Data Lake", "Planning", 0.12f, "T. Alvarez")
)

// ───────────────────────── LOGIN ─────────────────────────
@Composable
fun LoginScreen(onSignIn: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("alex.vance@corporationenterprise.com") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(Vx.Bg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Vantage PMO", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Vx.Blue)
        Spacer(Modifier.height(6.dp))
        Text("Project Management Office", fontSize = 15.sp, color = Vx.Muted)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") }, singleLine = true, shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue)
        ) { Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

// ───────────────────────── HOME ─────────────────────────
@Composable
fun HomeScreen(onProfile: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = { Text("My Portfolio", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            actions = { ProfileButton(onProfile) }
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeKpi("Active Projects", "4", Modifier.weight(1f))
                    HomeKpi("Tasks", "12", Modifier.weight(1f))
                    HomeKpi("ROI", "15%", Modifier.weight(1f))
                }
            }
            items(mockProjects) { ProjectCard(it) }
        }
    }
}

@Composable
private fun HomeKpi(label: String, value: String, modifier: Modifier) {
    Card(
        modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Vx.BlueSoft)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Vx.Blue)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Vx.Blue)
        }
    }
}

@Composable
fun ProjectCard(p: Project) {
    val risk = p.status == "En riesgo"
    VxCard(Modifier.fillMaxWidth(), onClick = {}) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                Pill(p.status, if (risk) Vx.RedSoft else Vx.BlueSoft, if (risk) Vx.Red else Vx.Blue)
            }
            Spacer(Modifier.height(4.dp))
            Text("Owner • ${p.owner}", fontSize = 12.sp, color = Vx.Muted)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                VxProgress(p.progress, if (risk) Vx.Red else Vx.Blue, Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Text("${(p.progress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            }
        }
    }
}

// ───────────────────────── PROJECTS ─────────────────────────
@Composable
fun ProjectsScreen(onCreate: () -> Unit, onProfile: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = { Text("Projects", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            actions = { ProfileButton(onProfile) }
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Button(
                    onClick = onCreate, modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Iniciativa", fontWeight = FontWeight.Bold)
                }
            }
            items(mockProjects) { ProjectCard(it) }
        }
    }
}

// ───────────────────────── GOVERNANCE ─────────────────────────
@Composable
fun GovernanceScreen(onProfile: () -> Unit) {
    val controls = remember3()
    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = { Text("Governance", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            actions = { ProfileButton(onProfile) }
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            controls.forEach { (title, desc, initial) ->
                var on by rememberSaveable(title) { mutableStateOf(initial) }
                VxCard(Modifier.fillMaxWidth(), onClick = { on = !on }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = if (on) Vx.Green else Vx.Gray)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                            Text(desc, fontSize = 12.sp, color = Vx.Muted)
                        }
                        Switch(
                            checked = on, onCheckedChange = { on = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Vx.Blue, checkedThumbColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}

private fun remember3() = listOf(
    Triple("SOC2 / ISO 27001", "Evidencias continuas para auditoría", true),
    Triple("Gate Reviews", "Aprobación obligatoria por fase", true),
    Triple("Budget Guardrails", "Alertas al superar 90% del cap", false)
)
