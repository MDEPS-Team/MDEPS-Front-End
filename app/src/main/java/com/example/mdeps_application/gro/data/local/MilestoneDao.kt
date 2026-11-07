package com.example.mdeps_application.gro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {

    @Query("SELECT * FROM milestones ORDER BY target_date ASC")
    fun observeAllMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones ORDER BY target_date ASC")
    suspend fun getAllMilestonesOnce(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE project_id = :projectId ORDER BY target_date ASC")
    fun observeMilestonesByProjectId(projectId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE id = :milestoneId LIMIT 1")
    fun observeMilestoneById(milestoneId: Long): Flow<MilestoneEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :milestoneId")
    suspend fun deleteMilestoneById(milestoneId: Long)
}
