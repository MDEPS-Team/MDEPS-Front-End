package com.example.mdeps_application.gro.domain

import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {

    fun observeAllMilestones(): Flow<List<Milestone>>

    fun observeMilestonesByProjectId(projectId: Long): Flow<List<Milestone>>

    suspend fun insertMilestone(milestone: Milestone): Long

    suspend fun updateMilestone(milestone: Milestone)

    suspend fun deleteMilestoneById(milestoneId: Long)

    suspend fun deleteMilestonesForProject(projectId: Long)
}
