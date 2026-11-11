@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.pm.application.SupportCenterViewModel
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.LocalAppSnackbarHostState
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard

@Composable
fun SupportCenterSheet(
    visible: Boolean,
    viewModel: SupportCenterViewModel,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalAppSnackbarHostState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.syncError) {
        val message = state.syncError ?: return@LaunchedEffect
        snackbar?.showSnackbar(message)
        viewModel.clearSyncError()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        val scroll = rememberScrollState()
        Column(
            Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Caption(text = stringResource(R.string.support_center_eyebrow), color = Vx.Blue)
            Text(stringResource(R.string.support_center_title), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.support_center_subtitle), color = Vx.Muted)
            if (state.fromServer) {
                Caption(text = stringResource(R.string.iam_profile_kpis_from_server), color = Vx.Green)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.createDemoTicket() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.support_center_new_ticket))
                }
                OutlinedButton(
                    onClick = { viewModel.createDemoChat() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.support_center_new_chat))
                }
            }
            Button(onClick = { viewModel.refresh() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.support_center_refresh))
            }
            if (state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            Text(stringResource(R.string.support_center_tickets), fontWeight = FontWeight.SemiBold)
            if (state.tickets.isEmpty()) {
                Text(stringResource(R.string.support_center_empty_tickets), color = Vx.Muted)
            } else {
                state.tickets.forEach { ticket ->
                    SupportTicketCard(ticket.title, ticket.description, ticket.status, ticket.priority)
                }
            }
            Text(
                stringResource(R.string.support_center_chats),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (state.chats.isEmpty()) {
                Text(stringResource(R.string.support_center_empty_chats), color = Vx.Muted)
            } else {
                state.chats.forEach { chat ->
                    ChatSummaryCard(
                        title = chat.title,
                        subtitle = chat.subtitle,
                        selected = state.selectedChatId == chat.id,
                        onClick = { viewModel.openChat(chat.id) },
                    )
                }
            }
            if (state.selectedChatId != null) {
                Text(
                    stringResource(R.string.support_center_messages),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (state.messagesLoading) {
                    CircularProgressIndicator(Modifier.padding(8.dp))
                } else if (state.chatMessages.isEmpty()) {
                    Text(stringResource(R.string.support_center_messages_empty), color = Vx.Muted)
                } else {
                    state.chatMessages.forEach { message ->
                        Text(
                            "${message.senderLabel}: ${message.content}",
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
                var draft by remember(state.selectedChatId) { mutableStateOf("") }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.support_center_message_hint)) },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        viewModel.sendChatMessage(draft)
                        draft = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.support_center_send_message))
                }
                TextButton(onClick = { viewModel.closeChat() }) {
                    Text(stringResource(R.string.support_center_close_chat))
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.support_center_close))
            }
        }
    }
}

@Composable
private fun SupportTicketCard(title: String, description: String, status: String, priority: String) {
    VxCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Vx.Blue)
            Column(Modifier.padding(start = 10.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                if (description.isNotBlank()) {
                    Text(description, color = Vx.Muted, maxLines = 3)
                }
                Text("$status · $priority", color = Vx.Muted)
            }
        }
    }
}

@Composable
private fun ChatSummaryCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    VxCard(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = if (selected) Vx.BlueSoft else Vx.Surface,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = Vx.Teal)
            Column(Modifier.padding(start = 10.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Vx.Muted, maxLines = 2)
            }
        }
    }
}
