package com.example.mdeps_application.gro.domain

import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {

    fun observeMilestonesByProjectId(projectId: Long): Flow<List<Milestone>>

    suspend fun insertMilestone(milestone: Milestone): Long

    suspend fun updateMilestone(milestone: Milestone)
}
