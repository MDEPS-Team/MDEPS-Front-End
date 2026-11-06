package com.example.mdeps_application.gro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus

private val SeverityLowGreen = Color(0xFF2E7D32)
private val SeverityMediumYellow = Color(0xFFF9A825)
private val SeverityHighOrange = Color(0xFFEF6C00)
private val SeverityCriticalRed = Color(0xFFC62828)

@Composable
fun RiskSeverity.corporateColor(): Color =
    when (this) {
        RiskSeverity.LOW -> SeverityLowGreen
        RiskSeverity.MEDIUM -> SeverityMediumYellow
        RiskSeverity.HIGH -> SeverityHighOrange
        RiskSeverity.CRITICAL -> SeverityCriticalRed
    }

@Composable
fun RiskSeverity.localizedLabel(): String =
    when (this) {
        RiskSeverity.LOW -> stringResource(R.string.gro_severity_low)
        RiskSeverity.MEDIUM -> stringResource(R.string.gro_severity_medium)
        RiskSeverity.HIGH -> stringResource(R.string.gro_severity_high)
        RiskSeverity.CRITICAL -> stringResource(R.string.gro_severity_critical)
    }

@Composable
fun RiskSeverity.badgeContentColor(): Color =
    when (this) {
        RiskSeverity.MEDIUM -> Color(0xFF1B1B1F)
        else -> Color.White
    }

@Composable
fun RiskStatus.localizedLabel(): String =
    when (this) {
        RiskStatus.IDENTIFIED -> stringResource(R.string.gro_status_identified)
        RiskStatus.MITIGATING -> stringResource(R.string.gro_status_mitigating)
        RiskStatus.RESOLVED -> stringResource(R.string.gro_status_resolved)
    }
