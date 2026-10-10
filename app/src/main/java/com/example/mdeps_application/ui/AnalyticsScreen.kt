@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── PANTALLA: ANALYTICS ─────────────────────────
@Composable
fun AnalyticsScreen(onProfile: () -> Unit) {
    var period by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(Vx.Bg)) {
        VxTopBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Vx.Blue),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("VANTAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Muted, letterSpacing = 1.sp)
                        Text("Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                    }
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Box {
                        Icon(Icons.Default.Notifications, "Notificaciones", tint = Vx.Ink)
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Vx.Blue).align(Alignment.TopEnd))
                    }
                }
                ProfileButton(onProfile)
            }
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("INTELLIGENCE SUITE", color = Vx.Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Text("Analítica Avanzada", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                }
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Exportar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            PeriodSelector(selected = period, onSelect = { period = it })
            EfficiencyCard()
            KpiGrid()
            BudgetCard()
            PortfolioHealthCard()
            MlCard()
            CapacityCard()
            AuditRow()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PeriodSelector(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf("Este Mes", "Q3 2024", "YTD", "Custom")
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Vx.Chip).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (sel) Color.White else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label, fontSize = 13.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    color = if (sel) Vx.Ink else Vx.Muted
                )
            }
        }
    }
}

@Composable
private fun EfficiencyCard() {
    VxCard(Modifier.fillMaxWidth(), onClick = {}) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Vx.Blue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("ÍNDICE DE EFICIENCIA OPERATIVA", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Muted)
                Pill("+4.6%", Vx.GreenSoft, Vx.Green)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("92.4", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                Spacer(Modifier.width(6.dp))
                Text("/ 100", fontSize = 14.sp, color = Vx.Muted, modifier = Modifier.padding(bottom = 8.dp))
            }
            LineChart(Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 6.dp))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Semana 1", "Semana 2", "Semana 3", "Actual (S4)").forEach {
                    Text(it, fontSize = 11.sp, color = Vx.Muted)
                }
            }
        }
    }
}

@Composable
private fun LineChart(modifier: Modifier) {
    val pts = listOf(0.18f, 0.24f, 0.22f, 0.30f, 0.38f, 0.36f, 0.50f, 0.58f, 0.82f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val line = Path()
        val fill = Path()
        pts.forEachIndexed { i, v ->
            val x = w * i / (pts.size - 1)
            val y = h * (1 - v)
            if (i == 0) { line.moveTo(x, y); fill.moveTo(x, h); fill.lineTo(x, y) }
            else { line.lineTo(x, y); fill.lineTo(x, y) }
        }
        fill.lineTo(w, h); fill.close()
        drawPath(fill, Brush.verticalGradient(listOf(Vx.Blue.copy(alpha = 0.22f), Color.Transparent)))
        drawPath(line, Vx.Blue, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(Color.White, 7.dp.toPx(), Offset(w, h * (1 - pts.last())))
        drawCircle(Vx.Blue, 5.dp.toPx(), Offset(w, h * (1 - pts.last())))
    }
}

@Composable
private fun KpiGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiTile("SPI (SCHEDULE)", "1.08", null, "Óptimo", Vx.GreenSoft, Vx.Green, "Meta: >1.00", Vx.Green, Modifier.weight(1f))
            KpiTile("CPI (COST)", "0.98", null, "Bajo Control", Vx.BlueSoft, Vx.Blue, "Meta: 1.00", Vx.Gray, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiTile("VELOCIDAD", "38", "pts/sprint", "+14%", Vx.GreenSoft, Vx.Green, "vs avg histórico", Vx.Blue, Modifier.weight(1f))
            KpiTile("TIEMPO CICLO", "4.2", "días", "-18%", Vx.GreenSoft, Vx.Green, "aceleración", Vx.Blue, Modifier.weight(1f))
        }
    }
}

@Composable
private fun KpiTile(
    label: String, value: String, unit: String?, pill: String, pillBg: Color, pillFg: Color,
    note: String, dot: Color, modifier: Modifier
) {
    VxCard(modifier, onClick = {}) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Muted)
                Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                if (unit != null) {
                    Spacer(Modifier.width(4.dp))
                    Text(unit, fontSize = 12.sp, color = Vx.Muted, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(pill, pillBg, pillFg)
                Spacer(Modifier.width(6.dp))
                Text(note, fontSize = 11.sp, color = Vx.Muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun BudgetCard() {
    val items = listOf(
        Triple("Desarrollo TI", "45%  \$216,000", 0.45f to Vx.Blue),
        Triple("Consultoría & Gobernanza", "30%  \$144,000", 0.30f to Color(0xFF3B7DDD)),
        Triple("Infraestructura Cloud", "15%  \$72,000", 0.15f to Vx.Gray),
        Triple("Contingencia Disponible", "10%  \$48,000", 0.10f to Vx.Teal)
    )
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Distribución Presupuestaria", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                    Text("Cap Total: \$480,000 USD", fontSize = 12.sp, color = Vx.Muted)
                }
                Pill("89% Comprometido", Vx.BlueSoft, Vx.Blue)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                items.forEach { Box(Modifier.weight(it.third.first).fillMaxSize().background(it.third.second)) }
            }
            Spacer(Modifier.height(14.dp))
            items.forEach { (name, amount, style) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(style.second))
                    Spacer(Modifier.width(10.dp))
                    Text(name, Modifier.weight(1f), fontSize = 13.sp, color = Vx.Ink)
                    Text(amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                }
            }
        }
    }
}

@Composable
private fun PortfolioHealthCard() {
    val segs = listOf(24f to Vx.Green, 9f to Vx.Gray, 5f to Vx.Red)
    VxCard(Modifier.fillMaxWidth(), onClick = {}) {
        Column(Modifier.padding(16.dp)) {
            Text("Salud de Portafolio (38 Iniciativas)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(128.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 18.dp.toPx()
                        var start = -90f
                        segs.forEach { (v, c) ->
                            val sweep = 360f * v / 38f
                            drawArc(
                                color = c, startAngle = start + 1.5f, sweepAngle = sweep - 3f, useCenter = false,
                                topLeft = Offset(stroke / 2, stroke / 2),
                                size = Size(size.width - stroke, size.height - stroke),
                                style = Stroke(stroke)
                            )
                            start += sweep
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("62%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                        Text("ÓPTIMO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Vx.Green)
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendRow("En Ruta", "24 proy", Vx.Green)
                    LegendRow("Riesgo Moderado", "9 proy", Vx.Gray)
                    LegendRow("Crítico", "5 proy", Vx.Red)
                }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, Modifier.weight(1f), fontSize = 13.sp, color = Vx.Ink)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
    }
}

@Composable
private fun MlCard() {
    VxCard(Modifier.fillMaxWidth(), containerColor = Color(0xFFEAF1FD)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Vx.Blue), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("VANTAGE ML INTELLIGENCE", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Blue)
                Pill("Predictivo", Color.White, Vx.Blue)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                androidx.compose.ui.text.buildAnnotatedString {
                    pushStyle(androidx.compose.ui.text.SpanStyle(color = Vx.Red, fontWeight = FontWeight.Bold))
                    append("Riesgo 68%: ")
                    pop()
                    append("El proyecto 'Infraestructura Zero Trust' proyecta retraso crítico por cuello de botella en validaciones de red. Se recomienda redistribuir 2 arquitectos sénior desde Sprint 4.")
                },
                fontSize = 13.sp, lineHeight = 19.sp, color = Vx.Ink
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue)
            ) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aplicar Rebalanceo Automático", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CapacityCard() {
    val rows = listOf(
        Triple("Backend & Core APIs", "98% (Sobrecarga)", 0.98f) to Vx.Red,
        Triple("Frontend & UI Mobile", "92%", 0.92f) to Vx.Blue,
        Triple("QA & Cumplimiento", "85%", 0.85f) to Vx.Blue,
        Triple("DevOps & Cloud Ops", "74% (Óptimo)", 0.74f) to Vx.Green
    )
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Carga de Capacidad por Área", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            Text("Disponibilidad en sprint activo", fontSize = 12.sp, color = Vx.Muted)
            Spacer(Modifier.height(10.dp))
            rows.forEach { (t, color) ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row {
                        Text(t.first, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Vx.Ink)
                        Text(t.second, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(6.dp))
                    VxProgress(t.third, color, height = 6.dp)
                }
            }
        }
    }
}

@Composable
private fun AuditRow() {
    VxCard(Modifier.fillMaxWidth(), onClick = {}) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(Vx.BlueSoft), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, null, tint = Vx.Blue, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Auditoría Trimestral Q3 Aprobada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                Text("99.2% de adherencia a SLAs corporativos", fontSize = 12.sp, color = Vx.Muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Vx.Muted)
        }
    }
}