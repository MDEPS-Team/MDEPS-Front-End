package com.example.mdeps_application.pto.data.repository

import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.pto.data.OfflineFirstProjectRepository
import com.example.mdeps_application.pto.data.ProjectBoardRegistry
import com.example.mdeps_application.pto.data.remote.PtoApiService
import com.example.mdeps_application.pto.data.remote.toCreateDto
import com.example.mdeps_application.pto.data.remote.toDomain
import com.example.mdeps_application.pto.data.remote.toUpdateDto
import com.example.mdeps_application.pto.data.remote.toTaskCreateDto
import com.example.mdeps_application.pto.data.remote.toTaskDomain
import com.example.mdeps_application.pto.data.remote.toTaskUpdateDto
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.Task
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class ApiBackedProjectRepository(
    private val local: OfflineFirstProjectRepository,
    private val api: PtoApiService,
    private val authRepository: AuthRepository,
    private val boardRegistry: ProjectBoardRegistry = ProjectBoardRegistry(),
) : ProjectRepository {

    override fun observeProjects(): Flow<List<Project>> = local.observeProjects()

    override fun observeActiveProjectCount(): Flow<Int> = local.observeActiveProjectCount()

    override fun observePendingTaskCount(): Flow<Int> = local.observePendingTaskCount()

    override fun observeAllTasks(): Flow<List<Task>> = local.observeAllTasks()

    override fun observeTasksByProjectId(projectId: Long): Flow<List<Task>> =
        local.observeTasksByProjectId(projectId)

    override suspend fun refreshFromRemote(): Result<Int> {
        return try {
            val remote = api.getProjects()
            remote.forEach { dto ->
                runCatching { local.insertProject(dto.toDomain()) }
            }
            refreshTasksFromRemote()
            Result.success(remote.size)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Projects load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading projects.", io))
        }
    }

    override suspend fun syncProject(projectId: Long): Result<Project> {
        return try {
            val remote = api.getProject(projectId.toInt())
            val domain = remote.toDomain()
            local.insertProject(domain)
            Result.success(domain)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Project load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading project.", io))
        }
    }

    override suspend fun insertProject(project: Project): Long {
        val userId = authRepository.currentUserId()?.toIntOrNull()
        if (userId == null) {
            return local.insertProject(project)
        }
        return try {
            val created = api.createProject(project.toCreateDto(userId))
            local.insertProject(created.toDomain())
        } catch (http: HttpException) {
            throw IllegalStateException("Create project failed (HTTP ${http.code()}).", http)
        } catch (io: IOException) {
            throw IOException("Network error while creating project.", io)
        }
    }

    override suspend fun updateProject(project: Project) {
        val userId = authRepository.currentUserId()?.toIntOrNull()
        if (project.id > 0) {
            runCatching {
                api.updateProject(
                    project.id.toInt(),
                    project.toUpdateDto(userId),
                )
            }
        }
        local.updateProject(project)
    }

    override suspend fun deleteProject(projectId: Long) {
        local.deleteProject(projectId)
    }

    override suspend fun refreshTasksFromRemote(): Result<Int> {
        return try {
            val projects = local.observeProjects().first()
            if (projects.isEmpty()) return Result.success(0)
            val boards = runCatching { api.getBoards() }.getOrDefault(emptyList())
            boardRegistry.hydrate(boards, projects)
            val aggregated = linkedMapOf<Int, com.example.mdeps_application.pto.data.remote.dto.CollaborationTaskDto>() // taskId -> dto
            runCatching {
                api.getTasks(scope = "dashboard").forEach { task ->
                    task.id?.let { aggregated[it] = task }
                }
            }
            boards.forEach { board ->
                val boardId = board.id ?: return@forEach
                runCatching {
                    api.getTasks(boardId = boardId).forEach { task ->
                        task.id?.let { aggregated[it] = task }
                    }
                }
            }
            if (aggregated.isEmpty()) {
                projects.forEach { project ->
                    val boardId = boardRegistry.resolveBoardId(project, boards)
                    runCatching {
                        api.getTasks(boardId = boardId).forEach { task ->
                            task.id?.let { aggregated[it] = task }
                        }
                    }
                }
            }
            aggregated.values.forEach { dto ->
                runCatching { local.insertTask(dto.toTaskDomain(projects)) }
            }
            Result.success(aggregated.size)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Tasks load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading tasks.", io))
        }
    }

    override suspend fun insertTask(task: Task): Long {
        val projects = local.observeProjects().first()
        val project = projects.firstOrNull { it.id == task.projectId }
        return try {
            if (project != null) {
                val boards = runCatching { api.getBoards() }.getOrDefault(emptyList())
                val boardId = boardRegistry.resolveBoardId(project, boards)
                val created = api.createTask(task.toTaskCreateDto(boardId, project.name))
                return local.insertTask(created.toTaskDomain(projects))
            } else {
                local.insertTask(task)
            }
        } catch (http: HttpException) {
            throw IllegalStateException("Create task failed (HTTP ${http.code()}).", http)
        } catch (io: IOException) {
            throw IOException("Network error while creating task.", io)
        }
    }

    override suspend fun updateTask(task: Task) {
        val projects = local.observeProjects().first()
        val project = projects.firstOrNull { it.id == task.projectId }
        if (task.id > 0) {
            runCatching {
                val boards = runCatching { api.getBoards() }.getOrDefault(emptyList())
                val boardId = project?.let { boardRegistry.resolveBoardId(it, boards) }
                api.updateTask(
                    task.id.toInt(),
                    task.toTaskUpdateDto(boardId, project?.name),
                )
            }
        }
        local.updateTask(task)
    }

    override suspend fun deleteTask(taskId: Long) {
        if (taskId > 0) {
            runCatching { api.deleteTask(taskId.toInt()) }
        }
        local.deleteTask(taskId)
    }
}
