@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.ProjectPdfExporter
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException

@Composable
fun ProjectDetailScreen(
    project: Project,
    tasks: List<Task>,
    onNavigateBack: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskCompletedChange: (Task, Boolean) -> Unit,
    onEvidencePhotoCaptured: (Task, String) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var activeTaskForCamera by remember { mutableStateOf<Task?>(null) }
    var showCameraScreen by remember { mutableStateOf(false) }

    val navigateBackDescription = stringResource(R.string.pto_a11y_navigate_back)
    val addTaskDescription = stringResource(R.string.pto_a11y_add_task)
    val permissionDeniedMessage = stringResource(R.string.pto_camera_permission_denied)
    val exportPdfDescription = stringResource(R.string.pto_a11y_export_pdf)
    val exportPdfFailedMessage = stringResource(R.string.pto_export_pdf_failed)
    val exportPdfNoAppMessage = stringResource(R.string.pto_export_pdf_no_app)
    val pdfExporter = remember(context) { ProjectPdfExporter(context.applicationContext) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            showCameraScreen = true
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMessage)
            }
            activeTaskForCamera = null
        }
    }

    fun openCameraForTask(task: Task) {
        activeTaskForCamera = task
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showCameraScreen = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.pto_project_tasks_title),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.semantics { contentDescription = navigateBackDescription },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = navigateBackDescription,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    runCatching {
                                        val pdfUri = pdfExporter.exportProjectReport(project, tasks)
                                        PtoPdfShare.launchPdfShareOrView(context, pdfUri)
                                    }.onFailure { error ->
                                        val message = when (error) {
                                            is ActivityNotFoundException -> exportPdfNoAppMessage
                                            else -> exportPdfFailedMessage
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            },
                            modifier = Modifier.semantics { contentDescription = exportPdfDescription },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = exportPdfDescription,
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTaskClick,
                    modifier = Modifier.semantics { contentDescription = addTaskDescription },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = addTaskDescription,
                    )
                }
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ProjectSummaryCard(project = project)
                }
                item {
                    Text(
                        text = stringResource(R.string.pto_tasks_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.pto_empty_tasks),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onCompletedChange = { isCompleted ->
                                onTaskCompletedChange(task, isCompleted)
                            },
                            onTakeEvidencePhotoClick = { openCameraForTask(task) },
                        )
                    }
                }
            }
        }

        if (showCameraScreen && activeTaskForCamera != null) {
            CameraScreen(
                onPhotoCaptured = { uriString ->
                    onEvidencePhotoCaptured(activeTaskForCamera!!, uriString)
                    showCameraScreen = false
                    activeTaskForCamera = null
                },
                onClose = {
                    showCameraScreen = false
                    activeTaskForCamera = null
                },
            )
        }
    }
}

@Composable
private fun ProjectSummaryCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.pto_project_description_label),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.pto_project_status_label) + ": " + project.status.localizedLabel(),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCompletedChange: (Boolean) -> Unit,
    onTakeEvidencePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasEvidence = !task.evidencePhotoUri.isNullOrBlank()
    val checkboxDescription = stringResource(R.string.pto_a11y_task_checkbox)
    val cameraDescription = if (hasEvidence) {
        stringResource(R.string.pto_a11y_replace_evidence_photo)
    } else {
        stringResource(R.string.pto_a11y_take_evidence_photo)
    }
    val thumbnailDescription = stringResource(R.string.pto_a11y_evidence_thumbnail)
    val completionLabel = if (task.isCompleted) {
        stringResource(R.string.pto_task_completed)
    } else {
        stringResource(R.string.pto_task_pending)
    }
    val evidenceLabel = if (hasEvidence) {
        stringResource(R.string.pto_evidence_attached)
    } else {
        stringResource(R.string.pto_evidence_missing)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCompletedChange,
                modifier = Modifier.semantics { contentDescription = checkboxDescription },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = completionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (hasEvidence) {
                        AsyncImage(
                            model = task.evidencePhotoUri,
                            contentDescription = thumbnailDescription,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Text(
                        text = evidenceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(
                onClick = onTakeEvidencePhotoClick,
                modifier = Modifier.semantics { contentDescription = cameraDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = cameraDescription,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectDetailScreenPreview() {
    val project = Project(
        id = 1L,
        name = "ERP Rollout",
        description = "Deploy ERP modules across business units.",
        status = ProjectStatus.IN_PROGRESS,
    )
    val tasks = listOf(
        Task(1L, 1L, "Configure master data", true, null),
        Task(2L, 1L, "Validate warehouse module", false, "file:///sample/evidence.jpg"),
    )
    MDEPS_APPLICATIONTheme {
        ProjectDetailScreen(
            project = project,
            tasks = tasks,
            onNavigateBack = {},
            onAddTaskClick = {},
            onTaskCompletedChange = { _, _ -> },
            onEvidencePhotoCaptured = { _, _ -> },
        )
    }
}
