package com.example.mdeps_application.gro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.domain.GroSeedContentKeys
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.Risk
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
fun Risk.localizedTitle(): String = resolveGroRiskTitle(title)

@Composable
fun Risk.localizedDescription(): String = resolveGroRiskDescription(description)

@Composable
fun Milestone.localizedTitle(): String = resolveGroMilestoneTitle(title)

@Composable
private fun resolveGroRiskTitle(raw: String): String =
    when (raw) {
        GroSeedContentKeys.RISK_SERVER_OUTAGE, "Server Outage" ->
            stringResource(R.string.gro_seed_risk_server_outage_title)
        else -> raw
    }

@Composable
private fun resolveGroRiskDescription(raw: String): String =
    when (raw) {
        GroSeedContentKeys.RISK_SERVER_OUTAGE,
        "Primary data center failover capacity is below SLA during peak load." ->
            stringResource(R.string.gro_seed_risk_server_outage_desc)
        else -> raw
    }

@Composable
private fun resolveGroMilestoneTitle(raw: String): String =
    when (raw) {
        GroSeedContentKeys.MILESTONE_ARCH_REVIEW, "Architecture review gate" ->
            stringResource(R.string.gro_seed_milestone_arch_review_title)
        GroSeedContentKeys.MILESTONE_STEERING, "Executive steering committee" ->
            stringResource(R.string.gro_seed_milestone_steering_title)
        else -> raw
    }

@Composable
fun RiskStatus.localizedLabel(): String =
    when (this) {
        RiskStatus.IDENTIFIED -> stringResource(R.string.gro_status_identified)
        RiskStatus.MITIGATING -> stringResource(R.string.gro_status_mitigating)
        RiskStatus.RESOLVED -> stringResource(R.string.gro_status_resolved)
    }
