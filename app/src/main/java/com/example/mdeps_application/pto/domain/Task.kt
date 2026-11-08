package com.example.mdeps_application.pto.domain

/** Columnas del tablero Kanban. DONE equivale a [Task.isCompleted] = true. */
enum class TaskStatus { TODO, IN_PROGRESS, REVIEW, DONE;

    val next: TaskStatus?
        get() = entries.getOrNull(ordinal + 1)
}

/** P0 = crítica … P3 = baja. */
enum class TaskPriority { P0, P1, P2, P3 }

data class Task(
    val id: Long,
    val projectId: Long,
    val title: String,
    val isCompleted: Boolean,
    val evidencePhotoUri: String?,
    val status: TaskStatus = if (isCompleted) TaskStatus.DONE else TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.P2,
    val dueDate: Long? = null,
    val assignee: String? = null,
    val storyPoints: Int = DEFAULT_STORY_POINTS,
    val description: String? = null,
) {
    /**
     * Estado coherente con [isCompleted]: el resto de la app (detalle de proyecto, analítica)
     * solo conoce el booleano, así que este es el valor que se persiste.
     */
    val effectiveStatus: TaskStatus
        get() = when {
            isCompleted -> TaskStatus.DONE
            status == TaskStatus.DONE -> TaskStatus.TODO
            else -> status
        }

    fun movedTo(target: TaskStatus): Task = copy(status = target, isCompleted = target == TaskStatus.DONE)

    companion object {
        const val DEFAULT_STORY_POINTS = 3
    }
}
