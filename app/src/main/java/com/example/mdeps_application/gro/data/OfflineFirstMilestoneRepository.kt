package com.example.mdeps_application.gro.data

import com.example.mdeps_application.gro.data.local.MilestoneDao
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstMilestoneRepository(
    private val milestoneDao: MilestoneDao,
) : MilestoneRepository {

    override fun observeAllMilestones(): Flow<List<Milestone>> =
        milestoneDao.observeAllMilestones().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMilestonesByProjectId(projectId: Long): Flow<List<Milestone>> =
        milestoneDao.observeMilestonesByProjectId(projectId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertMilestone(milestone: Milestone): Long =
        milestoneDao.insertMilestone(milestone.toEntity())

    override suspend fun updateMilestone(milestone: Milestone) {
        milestoneDao.updateMilestone(milestone.toEntity())
    }

    override suspend fun deleteMilestoneById(milestoneId: Long) {
        milestoneDao.deleteMilestoneById(milestoneId)
    }

    override suspend fun deleteMilestonesForProject(projectId: Long) {
        milestoneDao.deleteMilestonesByProjectId(projectId)
    }
}
