package com.example.mdeps_application.gro.data

import com.example.mdeps_application.gro.data.local.RiskDao
import com.example.mdeps_application.gro.domain.RemoteRiskMatrix
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstRiskRepository(
    private val riskDao: RiskDao,
) : RiskRepository {

    override suspend fun refreshFromRemote(): Result<Int> = Result.success(0)

    override suspend fun fetchRemoteHeatmap(): Result<RemoteRiskMatrix?> = Result.success(null)

    override fun observeAllRisks(): Flow<List<Risk>> =
        riskDao.observeAllRisks().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeRisksByProjectId(projectId: Long): Flow<List<Risk>> =
        riskDao.observeRisksByProjectId(projectId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertRisk(risk: Risk): Long =
        riskDao.insertRisk(risk.toEntity())

    override suspend fun updateRisk(risk: Risk) {
        riskDao.updateRisk(risk.toEntity())
    }

    override suspend fun deleteRiskById(riskId: Long) {
        riskDao.deleteRiskById(riskId)
    }

    override suspend fun deleteRisksForProject(projectId: Long) {
        riskDao.deleteRisksByProjectId(projectId)
    }
}
