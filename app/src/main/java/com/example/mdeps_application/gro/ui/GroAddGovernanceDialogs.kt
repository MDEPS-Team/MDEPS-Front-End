@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R

@Composable
fun AddRiskDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit,
) {
    if (!visible) return
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gro_add_risk_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.gro_add_risk_hint),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.gro_add_risk_field_title)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.gro_add_risk_field_desc)) },
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, description)
                    title = ""
                    description = ""
                    onDismiss()
                },
                enabled = title.trim().isNotEmpty(),
            ) {
                Text(stringResource(R.string.gro_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.gro_add_cancel))
            }
        },
    )
}

@Composable
fun AddMilestoneDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String) -> Unit,
) {
    if (!visible) return
    var title by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gro_add_milestone_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.gro_add_milestone_hint),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.gro_add_milestone_field_title)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title)
                    title = ""
                    onDismiss()
                },
                enabled = title.trim().isNotEmpty(),
            ) {
                Text(stringResource(R.string.gro_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.gro_add_cancel))
            }
        },
    )
}
