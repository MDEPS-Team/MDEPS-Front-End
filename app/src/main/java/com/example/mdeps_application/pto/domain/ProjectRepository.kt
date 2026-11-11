package com.example.mdeps_application.pto.domain

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {

    /** Pulls projects from Railway and upserts them into the local cache (Room). */
    suspend fun refreshFromRemote(): Result<Int>

    /** Fetches a single project from the API and refreshes the local row. */
    suspend fun syncProject(projectId: Long): Result<Project>

    /** Pulls collaboration tasks from Railway and upserts them into Room (Kanban). */
    suspend fun refreshTasksFromRemote(): Result<Int>

    fun observeProjects(): Flow<List<Project>>

    fun observeActiveProjectCount(): Flow<Int>

    fun observePendingTaskCount(): Flow<Int>

    fun observeAllTasks(): Flow<List<Task>>

    fun observeTasksByProjectId(projectId: Long): Flow<List<Task>>

    suspend fun insertProject(project: Project): Long

    suspend fun updateProject(project: Project)

    suspend fun deleteProject(projectId: Long)

    suspend fun insertTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(taskId: Long)
}
