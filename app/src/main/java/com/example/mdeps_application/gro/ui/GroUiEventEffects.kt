package com.example.mdeps_application.gro.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GroUiEvent
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.ui.LocalAppSnackbarHostState

@Composable
fun CollectGroUiEvents(viewModel: GroViewModel) {
    val snackbarHostState = LocalAppSnackbarHostState.current ?: return
    val context = LocalContext.current
    val defaultMessage = stringResource(R.string.gro_action_completed_success)
    val undoLabel = stringResource(R.string.gro_snackbar_undo)

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is GroUiEvent.ActionCompleted -> {
                    val message = when {
                        event.messageRes != null && event.messageArg != null ->
                            context.getString(event.messageRes, event.messageArg)
                        event.messageRes != null ->
                            context.getString(event.messageRes)
                        else -> defaultMessage
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = if (event.canUndo) undoLabel else null,
                        // Con acción, Material usa Indefinite por defecto y el aviso tapaba la UI.
                        duration = SnackbarDuration.Long,
                    )
                    if (event.canUndo && result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastAction()
                    }
                }
            }
        }
    }
}
