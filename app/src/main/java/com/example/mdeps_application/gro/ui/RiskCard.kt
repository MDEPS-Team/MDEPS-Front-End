package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

@Composable
fun RiskCard(
    risk: Risk,
    modifier: Modifier = Modifier,
    cardContentDescription: String? = null,
) {
    val resolvedCardDescription = cardContentDescription
        ?: stringResource(R.string.gro_a11y_risk_card)
    val severityLabel = risk.severity.localizedLabel()
    val severityBadgeDescription = stringResource(
        R.string.gro_a11y_severity_badge,
        severityLabel,
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = resolvedCardDescription },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = risk.localizedTitle(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                SeverityBadge(
                    severity = risk.severity,
                    contentDescription = severityBadgeDescription,
                )
            }
            Text(
                text = risk.localizedDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SeverityBadge(
    severity: RiskSeverity,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = severity.localizedLabel(),
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .background(
                color = severity.corporateColor(),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = severity.badgeContentColor(),
    )
}

@Preview(showBackground = true)
@Composable
private fun RiskCardPreview() {
    MDEPS_APPLICATIONTheme {
        RiskCard(
            risk = Risk(
                id = 1L,
                projectId = 10L,
                title = "Vendor delivery delay",
                description = "Critical path dependency on third-party integration may slip by two sprints.",
                severity = RiskSeverity.HIGH,
                status = RiskStatus.IDENTIFIED,
            ),
        )
    }
}
