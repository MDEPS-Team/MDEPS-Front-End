package com.example.mdeps_application.pto.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.ProjectStatus

@Composable
fun ProjectStatus.localizedLabel(): String =
    when (this) {
        ProjectStatus.PLANNING -> stringResource(R.string.pto_status_planning)
        ProjectStatus.IN_PROGRESS -> stringResource(R.string.pto_status_in_progress)
        ProjectStatus.AT_RISK -> stringResource(R.string.pto_status_at_risk)
        ProjectStatus.COMPLETED -> stringResource(R.string.pto_status_completed)
    }
