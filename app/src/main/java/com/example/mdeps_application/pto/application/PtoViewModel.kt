package com.example.mdeps_application.pto.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
)

class PtoViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _projectDetailState = MutableStateFlow(ProjectDetailUiState())
    val projectDetailState: StateFlow<ProjectDetailUiState> = _projectDetailState.asStateFlow()

    private var projectDetailsJob: Job? = null

    init {
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
    }

    fun loadProjectDetails(projectId: Long) {
        projectDetailsJob?.cancel()
        projectDetailsJob = viewModelScope.launch {
            combine(
                projectRepository.observeProjects().map { projects ->
                    projects.firstOrNull { it.id == projectId }
                },
                projectRepository.observeTasksByProjectId(projectId),
            ) { project, tasks ->
                ProjectDetailUiState(project = project, tasks = tasks)
            }.collect { detailState ->
                _projectDetailState.value = detailState
            }
        }
    }

    fun clearProjectDetails() {
        projectDetailsJob?.cancel()
        _projectDetailState.value = ProjectDetailUiState()
    }

    fun addDummyProject() {
        viewModelScope.launch {
            val suffix = System.currentTimeMillis() % 1000
            projectRepository.insertProject(
                Project(
                    id = 0L,
                    name = "Demo Project $suffix",
                    description = "Auto-generated project for offline Room testing.",
                    status = ProjectStatus.IN_PROGRESS,
                ),
            )
        }
    }

    fun addTask(projectId: Long, title: String) {
        viewModelScope.launch {
            projectRepository.insertTask(
                Task(
                    id = 0L,
                    projectId = projectId,
                    title = title,
                    isCompleted = false,
                    evidencePhotoUri = null,
                ),
            )
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            projectRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTaskEvidence(taskId: Long, photoUri: String) {
        viewModelScope.launch {
            val currentTask = _projectDetailState.value.tasks.firstOrNull { it.id == taskId } ?: return@launch
            projectRepository.updateTask(currentTask.copy(evidencePhotoUri = photoUri))
        }
    }

    companion object {
        fun factory(projectRepository: ProjectRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PtoViewModel(projectRepository) as T
                }
            }
    }
}
