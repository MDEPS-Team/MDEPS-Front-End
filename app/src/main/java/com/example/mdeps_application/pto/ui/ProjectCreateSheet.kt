@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.ProjectStatus

@Composable
fun ProjectCreateSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, status: ProjectStatus) -> Unit,
) {
    if (!visible) return

    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(ProjectStatus.PLANNING) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.pto_create_sheet_title),
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.pto_create_name_label)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.pto_create_description_label)) },
                minLines = 3,
            )
            Text(text = stringResource(R.string.pto_create_status_label), fontWeight = FontWeight.SemiBold)
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    ProjectStatus.PLANNING,
                    ProjectStatus.IN_PROGRESS,
                    ProjectStatus.AT_RISK,
                ).forEach { option ->
                    androidx.compose.material3.FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option.localizedLabel()) },
                    )
                }
            }
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onCreate(name, description, status)
                        onDismiss()
                        name = ""
                        description = ""
                        status = ProjectStatus.PLANNING
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.trim().isNotEmpty(),
            ) {
                Text(stringResource(R.string.pto_create_confirm))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pto_create_cancel))
            }
        }
    }
}
