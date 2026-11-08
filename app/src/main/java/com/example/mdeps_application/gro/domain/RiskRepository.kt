package com.example.mdeps_application.gro.domain

import kotlinx.coroutines.flow.Flow

interface RiskRepository {

    fun observeAllRisks(): Flow<List<Risk>>

    fun observeRisksByProjectId(projectId: Long): Flow<List<Risk>>

    suspend fun insertRisk(risk: Risk): Long

    suspend fun updateRisk(risk: Risk)

    suspend fun deleteRisksForProject(projectId: Long)
}
