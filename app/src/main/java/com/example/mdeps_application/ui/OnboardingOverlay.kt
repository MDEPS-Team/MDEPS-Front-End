package com.example.mdeps_application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R

@Composable
fun OnboardingDialog(
    step: Int,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit,
) {
    val title = when (step) {
        0 -> stringResource(R.string.onboarding_step_one_title)
        1 -> stringResource(R.string.onboarding_step_two_title)
        else -> stringResource(R.string.onboarding_step_three_title)
    }
    val body = when (step) {
        0 -> stringResource(R.string.onboarding_step_one_body)
        1 -> stringResource(R.string.onboarding_step_two_body)
        else -> stringResource(R.string.onboarding_step_three_body)
    }

    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = body) },
        confirmButton = {
            Button(
                onClick = {
                    if (step >= 2) onFinish() else onNext()
                },
            ) {
                Text(
                    text = if (step >= 2) {
                        stringResource(R.string.onboarding_finish)
                    } else {
                        stringResource(R.string.onboarding_next)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text(text = stringResource(R.string.onboarding_skip))
            }
        },
    )
}
