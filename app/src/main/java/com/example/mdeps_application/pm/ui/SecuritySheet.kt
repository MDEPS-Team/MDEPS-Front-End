@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.ui.Vx

@Composable
fun SecuritySheet(
    corporateEmail: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveEmail: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var emailDraft by remember { mutableStateOf(corporateEmail) }
    LaunchedEffect(corporateEmail) {
        emailDraft = corporateEmail
    }
    var showEmployeeId by rememberSaveable { mutableStateOf(false) }
    var showTwoFactorOptions by rememberSaveable { mutableStateOf(false) }

    val closeDescription = stringResource(R.string.iam_a11y_security_close)
    val changeEmailDescription = stringResource(R.string.iam_a11y_security_change_email)
    val viewIdDescription = stringResource(R.string.iam_a11y_security_view_id)
    val changeTwoFaDescription = stringResource(R.string.iam_a11y_security_change_2fa)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.iam_security_sheet_title),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = closeDescription },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeDescription,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = changeEmailDescription }
                    .clickable { showTwoFactorOptions = false; showEmployeeId = false },
                leadingContent = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = changeEmailDescription,
                        modifier = Modifier.size(24.dp),
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.iam_security_change_email), fontWeight = FontWeight.SemiBold)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
            )
            OutlinedTextField(
                value = emailDraft,
                onValueChange = {
                    emailDraft = it
                    onEmailChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                label = { Text(stringResource(R.string.iam_login_corporate_email_label)) },
            )
            Text(
                text = stringResource(R.string.iam_security_email_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Button(
                onClick = onSaveEmail,
                enabled = !isSaving && emailDraft.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue),
            ) {
                Text(
                    text = stringResource(R.string.iam_security_save_email),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider()

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = viewIdDescription }
                    .clickable {
                        showEmployeeId = !showEmployeeId
                        showTwoFactorOptions = false
                    },
                leadingContent = {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = viewIdDescription,
                        modifier = Modifier.size(24.dp),
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.iam_security_view_id), fontWeight = FontWeight.SemiBold)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
            )
            if (showEmployeeId) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.iam_security_employee_id_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.iam_security_employee_id_value),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalDivider()

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = changeTwoFaDescription }
                    .clickable {
                        showTwoFactorOptions = !showTwoFactorOptions
                        showEmployeeId = false
                    },
                leadingContent = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = changeTwoFaDescription,
                        modifier = Modifier.size(24.dp),
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.iam_security_change_2fa), fontWeight = FontWeight.SemiBold)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
            )
            if (showTwoFactorOptions) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.iam_biometric_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
