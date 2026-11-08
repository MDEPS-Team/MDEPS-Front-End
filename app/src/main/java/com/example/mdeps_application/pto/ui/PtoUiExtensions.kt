package com.example.mdeps_application.pto.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus

private const val LEGACY_DEMO_PROJECT_PREFIX = "Demo Project "
private const val LEGACY_DEMO_DESCRIPTION = "Auto-generated project for offline Room testing."

@Composable
fun Project.localizedDisplayName(): String {
    if (name.startsWith(LEGACY_DEMO_PROJECT_PREFIX)) {
        val suffix = name.removePrefix(LEGACY_DEMO_PROJECT_PREFIX)
        return stringResource(R.string.pto_demo_project_name, suffix)
    }
    return name
}

@Composable
fun Project.localizedDisplayDescription(): String =
    if (description == LEGACY_DEMO_DESCRIPTION) {
        stringResource(R.string.pto_demo_project_description)
    } else {
        description
    }

@Composable
fun ProjectStatus.localizedLabel(): String =
    when (this) {
        ProjectStatus.PLANNING -> stringResource(R.string.pto_status_planning)
        ProjectStatus.IN_PROGRESS -> stringResource(R.string.pto_status_in_progress)
        ProjectStatus.AT_RISK -> stringResource(R.string.pto_status_at_risk)
        ProjectStatus.COMPLETED -> stringResource(R.string.pto_status_completed)
    }
