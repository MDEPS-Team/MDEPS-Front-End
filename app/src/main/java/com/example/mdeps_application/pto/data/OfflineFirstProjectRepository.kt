package com.example.mdeps_application.pto.data

import com.example.mdeps_application.pto.data.local.ProjectDao
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstProjectRepository(
    private val projectDao: ProjectDao,
) : ProjectRepository {

    override fun observeProjects(): Flow<List<Project>> =
        projectDao.observeProjects().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeActiveProjectCount(): Flow<Int> =
        projectDao.observeActiveProjectCount()

    override fun observePendingTaskCount(): Flow<Int> =
        projectDao.observePendingTaskCount()

    override fun observeAllTasks(): Flow<List<Task>> =
        projectDao.observeAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeTasksByProjectId(projectId: Long): Flow<List<Task>> =
        projectDao.observeTasksByProjectId(projectId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertProject(project: Project): Long =
        projectDao.insertProject(project.toEntity())

    override suspend fun updateProject(project: Project) {
        projectDao.updateProject(project.toEntity())
    }

    override suspend fun deleteProject(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    override suspend fun insertTask(task: Task): Long =
        projectDao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) {
        projectDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(taskId: Long) {
        projectDao.deleteTaskById(taskId)
    }
}
