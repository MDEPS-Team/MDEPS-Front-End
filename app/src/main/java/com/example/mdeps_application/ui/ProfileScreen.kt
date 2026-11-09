@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── PANTALLA: PROFILE ─────────────────────────
@Composable
fun ProfileScreen() {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("alex.vance@corporationenterprise.com") }
    var twoFa by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            actions = { ProfileButton {} }
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera
            VxCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar("AV", 96.dp, bg = Vx.BlueSoft, fg = Vx.Blue)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(Vx.GreenSoft).padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = Vx.Green, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("VERIFIED CORPORATE ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Green)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Alejandro Vance Rivas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                    Spacer(Modifier.height(4.dp))
                    Text("Senior Delivery & PMO Director", fontSize = 15.sp, color = Vx.Muted)
                    Spacer(Modifier.height(10.dp))
                    Text("Enterprise Solutions Global • Madrid HQ • Floor 14", fontSize = 12.sp, color = Vx.Muted)
                }
            }

            // Métricas
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Metric("PROJECTS", "14", "On Track", Vx.BlueSoft, Modifier.weight(1f))
                Metric("SLA ADH.", "98.4%", "±0.2% vs target", Vx.GreenSoft, Modifier.weight(1f))
                Metric("TEAM", "32", "Across 4 pods", Vx.BlueSoft, Modifier.weight(1f))
            }

            // Cuenta y seguridad
            VxCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Caption("Account & Security", Modifier.weight(1f), color = Vx.Ink)
                        TextButton(onClick = { showSheet = true }) {
                            Text("Edit", color = Vx.Blue, fontWeight = FontWeight.Bold)
                        }
                    }
                    InfoRow("Corporate Email", email, null)
                    Spacer(Modifier.height(14.dp))
                    InfoRow("Enterprise SSO", "Google Workspace Connected", "Active")
                    Spacer(Modifier.height(14.dp))
                    InfoRow(
                        "Two-Factor Auth",
                        if (twoFa == 0) "Hardware Key / FIDO2 • Enforced" else "Authenticator App • Enforced",
                        "Enforced"
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showSheet) {
        SecuritySheet(
            initialEmail = email, initialTwoFa = twoFa,
            onDismiss = { showSheet = false },
            onConfirm = { e, f -> email = e; twoFa = f; showSheet = false }
        )
    }
}

@Composable
private fun Metric(label: String, value: String, note: String, accent: Color, modifier: Modifier) {
    VxCard(modifier, onClick = {}) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Muted)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            Spacer(Modifier.height(2.dp))
            Text(note, fontSize = 11.sp, color = Vx.Muted, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(accent))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, pill: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Muted)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Vx.Ink)
        }
        if (pill != null) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Vx.BlueSoft).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(pill, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Vx.Blue)
            }
        }
    }
}

// ───────────────────────── MODAL: EDITAR CUENTA Y SEGURIDAD ─────────────────────────
@Composable
private fun SecuritySheet(initialEmail: String, initialTwoFa: Int, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var email by remember { mutableStateOf(initialEmail) }
    var twoFa by remember { mutableStateOf(initialTwoFa) }
    var pass by remember { mutableStateOf("••••••••••••") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().weight(1f, fill = false)) {
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("• CORPORACIÓN ENTERPRISE • SEGURIDAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Blue, letterSpacing = 0.5.sp)
                        Text("Editar Cuenta y Seguridad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Caption("Email Corporativo", Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, null, tint = Vx.Green, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("VERIFICADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Green)
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Vx.Muted) },
                        trailingIcon = { Icon(Icons.Default.Lock, null, tint = Vx.Muted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Caption("ID de Empleado")
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Vx.Bg).padding(12.dp)) {
                            Text("#ENT-88492-SEC", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Caption("Token de Acceso")
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Vx.BlueSoft).padding(12.dp)) {
                            Text("Activo", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Vx.Blue)
                        }
                    }
                }

                Column {
                    Caption("Enterprise SSO Provider")
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Vx.Bg).clickable { }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Vx.Blue)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Google Workspace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                            Text("Enterprise Global", fontSize = 11.sp, color = Vx.Muted)
                        }
                        Pill("Conectado", Vx.BlueSoft, Vx.Blue)
                    }
                }

                Column {
                    Caption("Método 2FA (Doble Factor)")
                    Spacer(Modifier.height(6.dp))
                    TwoFaOption(twoFa == 0, "Llave de Hardware / FIDO2", "YubiKey 5C NFC corporativa vinculada", true) { twoFa = 0 }
                    Spacer(Modifier.height(8.dp))
                    TwoFaOption(twoFa == 1, "Authenticator App (TOTP)", "Generador de códigos temporales", false) { twoFa = 1 }
                }

                Column {
                    Row {
                        Caption("Frase de paso maestra", Modifier.weight(1f))
                        Text("Actualizar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Blue, modifier = Modifier.clickable { })
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Vx.Muted) },
                        trailingIcon = { Text("Modificada hace 42 d", fontSize = 11.sp, color = Vx.Muted, modifier = Modifier.padding(end = 10.dp)) }
                    )
                }
            }

            Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding()) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Cancelar", color = Vx.Ink, fontWeight = FontWeight.Medium) }
                    Button(
                        onClick = { onConfirm(email, twoFa) }, modifier = Modifier.weight(1.4f).height(48.dp),
                        shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Confirmar Cambios", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "✓ Cifrado SOC2 Type II • Protocolo de Seguridad Corporación Enterprise",
                    fontSize = 10.sp, color = Vx.Muted, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TwoFaOption(selected: Boolean, title: String, subtitle: String, recommended: Boolean, onSelect: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (selected) Vx.BlueSoft.copy(alpha = 0.6f) else Vx.Bg)
            .border(1.dp, if (selected) Vx.Blue else Color.Transparent, shape)
            .clickable(onClick = onSelect).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Vx.Blue))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            Text(subtitle, fontSize = 11.sp, color = Vx.Muted)
        }
        if (recommended) Pill("RECOMENDADO", Vx.GreenSoft, Vx.Green, size = 9.sp)
    }
}
