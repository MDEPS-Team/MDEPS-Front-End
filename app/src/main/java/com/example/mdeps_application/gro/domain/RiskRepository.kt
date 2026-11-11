package com.example.mdeps_application.gro.domain

import kotlinx.coroutines.flow.Flow

interface RiskRepository {

    suspend fun refreshFromRemote(): Result<Int>

    suspend fun fetchRemoteHeatmap(): Result<RemoteRiskMatrix?>

    fun observeAllRisks(): Flow<List<Risk>>

    fun observeRisksByProjectId(projectId: Long): Flow<List<Risk>>

    suspend fun insertRisk(risk: Risk): Long

    suspend fun updateRisk(risk: Risk)

    suspend fun deleteRiskById(riskId: Long)

    suspend fun deleteRisksForProject(projectId: Long)
}
