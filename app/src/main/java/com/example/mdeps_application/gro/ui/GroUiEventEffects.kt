package com.example.mdeps_application.gro.ui

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GroUiEvent
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.ui.LocalAppSnackbarHostState

@Composable
fun CollectGroUiEvents(viewModel: GroViewModel) {
    val snackbarHostState = LocalAppSnackbarHostState.current ?: return
    val successMessage = stringResource(R.string.gro_action_completed_success)
    val undoLabel = stringResource(R.string.gro_snackbar_undo)

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is GroUiEvent.ActionCompleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = successMessage,
                        actionLabel = if (event.canUndo) undoLabel else null,
                    )
                    if (event.canUndo && result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastAction()
                    }
                }
            }
        }
    }
}
