package com.example.mdeps_application.pto.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.pto.domain.TaskPriority
import com.example.mdeps_application.pto.domain.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanbanCard(
    val task: Task,
    val projectName: String,
)

data class KanbanUiState(
    val projects: List<Project> = emptyList(),
    val selectedProjectId: Long? = null,
    val query: String = "",
    val columns: Map<TaskStatus, List<KanbanCard>> = TaskStatus.entries.associateWith { emptyList() },
    /** Puntos de las tareas visibles (respeta el filtro de proyecto, no la búsqueda). */
    val totalPoints: Int = 0,
    val donePoints: Int = 0,
    val totalTasks: Int = 0,
    val isLoaded: Boolean = false,
) {
    val velocity: Float get() = if (totalPoints == 0) 0f else donePoints.toFloat() / totalPoints

    fun count(status: TaskStatus): Int = columns[status].orEmpty().size
}

data class TaskDraft(
    val title: String = "",
    val projectId: Long? = null,
    val description: String = "",
    val assignee: String = "",
    val priority: TaskPriority = TaskPriority.P2,
    val dueDate: Long? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val storyPoints: Int = Task.DEFAULT_STORY_POINTS,
) {
    val isValid: Boolean get() = title.isNotBlank() && projectId != null
}

class KanbanViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val selectedProjectId = MutableStateFlow<Long?>(null)
    private val query = MutableStateFlow("")

    /** Todas las tareas sin filtrar (el Roadmap calcula la carga por proyecto a partir de aquí). */
    val allTasks: StateFlow<List<Task>> = projectRepository.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Última tarea creada: la UI muestra el diálogo de éxito mientras no sea null. */
    private val _createdCard = MutableStateFlow<KanbanCard?>(null)
    val createdCard: StateFlow<KanbanCard?> = _createdCard.asStateFlow()

    val uiState: StateFlow<KanbanUiState> = combine(
        projectRepository.observeProjects(),
        projectRepository.observeAllTasks(),
        selectedProjectId,
        query,
    ) { projects, tasks, projectFilter, rawQuery ->
        val names = projects.associate { it.id to it.name }
        // Si el proyecto filtrado se eliminó, se vuelve a "todos".
        val effectiveFilter = projectFilter?.takeIf { it in names }
        val scoped = tasks
            .filter { it.projectId in names }
            .filter { effectiveFilter == null || it.projectId == effectiveFilter }
        val needle = rawQuery.trim()
        val cards = scoped
            .map { KanbanCard(it, names.getValue(it.projectId)) }
            .filter { card ->
                needle.isEmpty() ||
                    card.task.title.contains(needle, ignoreCase = true) ||
                    card.task.assignee.orEmpty().contains(needle, ignoreCase = true) ||
                    card.projectName.contains(needle, ignoreCase = true)
            }
        KanbanUiState(
            projects = projects,
            selectedProjectId = effectiveFilter,
            query = rawQuery,
            columns = TaskStatus.entries.associateWith { status ->
                cards.filter { it.task.effectiveStatus == status }.sortedWith(cardOrder)
            },
            totalPoints = scoped.sumOf { it.storyPoints },
            donePoints = scoped.filter { it.isCompleted }.sumOf { it.storyPoints },
            totalTasks = scoped.size,
            isLoaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KanbanUiState())

    fun selectProject(projectId: Long?) {
        selectedProjectId.value = projectId
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun moveTask(task: Task, target: TaskStatus) {
        if (task.effectiveStatus == target) return
        viewModelScope.launch { projectRepository.updateTask(task.movedTo(target)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { projectRepository.deleteTask(task.id) }
    }

    fun createTask(draft: TaskDraft) {
        val projectId = draft.projectId ?: return
        if (!draft.isValid) return
        viewModelScope.launch {
            val task = Task(
                id = 0L,
                projectId = projectId,
                title = draft.title.trim(),
                isCompleted = draft.status == TaskStatus.DONE,
                evidencePhotoUri = null,
                status = draft.status,
                priority = draft.priority,
                dueDate = draft.dueDate,
                assignee = draft.assignee.trim().ifEmpty { null },
                storyPoints = draft.storyPoints,
                description = draft.description.trim().ifEmpty { null },
            )
            val id = projectRepository.insertTask(task)
            val projectName = uiState.value.projects.firstOrNull { it.id == projectId }?.name.orEmpty()
            _createdCard.value = KanbanCard(task.copy(id = id), projectName)
        }
    }

    fun dismissCreated() {
        _createdCard.update { null }
    }

    companion object {
        /** Prioridad primero (P0 arriba), luego la fecha más próxima. */
        private val cardOrder = compareBy<KanbanCard>(
            { it.task.priority.ordinal },
            { it.task.dueDate ?: Long.MAX_VALUE },
            { it.task.id },
        )

        fun factory(projectRepository: ProjectRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    KanbanViewModel(projectRepository) as T
            }
    }
}
