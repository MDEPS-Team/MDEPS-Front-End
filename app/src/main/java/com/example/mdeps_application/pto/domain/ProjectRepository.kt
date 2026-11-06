package com.example.mdeps_application.pto.domain

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {

    fun observeProjects(): Flow<List<Project>>

    fun observeTasksByProjectId(projectId: Long): Flow<List<Task>>

    suspend fun insertProject(project: Project): Long

    suspend fun updateProject(project: Project)

    suspend fun deleteProject(projectId: Long)

    suspend fun insertTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(taskId: Long)
}
