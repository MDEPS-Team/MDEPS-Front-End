package com.example.mdeps_application.pto.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM projects WHERE status != 'COMPLETED'")
    fun observeActiveProjectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    fun observePendingTaskCount(): Flow<Int>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun observeProjectById(projectId: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY id ASC")
    fun observeTasksByProjectId(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun observeTaskById(taskId: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}
