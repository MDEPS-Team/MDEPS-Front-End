package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectQuickActionsSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddTask: () -> Unit,
    onAddRisk: (title: String, description: String) -> Unit,
    onAddMilestone: (title: String) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var riskTitle by rememberSaveable { mutableStateOf("") }
    var riskDescription by rememberSaveable { mutableStateOf("") }
    var milestoneTitle by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.pto_quick_actions_title),
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = {
                    onAddTask()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(text = stringResource(R.string.pto_quick_action_add_task))
            }
            OutlinedTextField(
                value = riskTitle,
                onValueChange = { riskTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text(stringResource(R.string.pto_quick_action_risk_title)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = riskDescription,
                onValueChange = { riskDescription = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text(stringResource(R.string.pto_quick_action_risk_description)) },
            )
            Button(
                onClick = {
                    if (riskTitle.isNotBlank()) {
                        onAddRisk(riskTitle.trim(), riskDescription.trim())
                        riskTitle = ""
                        riskDescription = ""
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = riskTitle.isNotBlank(),
            ) {
                Text(text = stringResource(R.string.pto_quick_action_log_risk))
            }
            OutlinedTextField(
                value = milestoneTitle,
                onValueChange = { milestoneTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text(stringResource(R.string.pto_quick_action_milestone_title)) },
                singleLine = true,
            )
            Button(
                onClick = {
                    if (milestoneTitle.isNotBlank()) {
                        onAddMilestone(milestoneTitle.trim())
                        milestoneTitle = ""
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                enabled = milestoneTitle.isNotBlank(),
            ) {
                Text(text = stringResource(R.string.pto_quick_action_add_milestone))
            }
        }
    }
}
