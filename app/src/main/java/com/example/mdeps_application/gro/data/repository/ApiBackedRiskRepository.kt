package com.example.mdeps_application.gro.data.repository

import com.example.mdeps_application.gro.data.OfflineFirstRiskRepository
import com.example.mdeps_application.gro.data.remote.GroApiService
import com.example.mdeps_application.gro.data.remote.toRemoteRiskMatrix
import com.example.mdeps_application.gro.data.remote.toRiskDomain
import com.example.mdeps_application.gro.domain.RemoteRiskMatrix
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.pto.domain.ProjectRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class ApiBackedRiskRepository(
    private val local: OfflineFirstRiskRepository,
    private val api: GroApiService,
    private val projectRepository: ProjectRepository,
) : RiskRepository {

    override suspend fun fetchRemoteHeatmap(): Result<RemoteRiskMatrix?> {
        return try {
            val matrix = api.getRiskMatrix().firstOrNull()?.toRemoteRiskMatrix()
            Result.success(matrix)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Risk matrix load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading risk matrix.", io))
        }
    }

    override suspend fun refreshFromRemote(): Result<Int> {
        return try {
            val projects = projectRepository.observeProjects().first()
            val fallbackProjectId = projects.firstOrNull()?.id ?: 1L
            val remote = api.getRisks()
            remote.forEachIndexed { index, dto ->
                val projectId = projects.getOrNull(index % projects.size.coerceAtLeast(1))?.id
                    ?: fallbackProjectId
                runCatching { local.insertRisk(dto.toRiskDomain(projectId)) }
            }
            Result.success(remote.size)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Risks load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading risks.", io))
        }
    }

    override fun observeAllRisks(): Flow<List<Risk>> = local.observeAllRisks()

    override fun observeRisksByProjectId(projectId: Long): Flow<List<Risk>> =
        local.observeRisksByProjectId(projectId)

    override suspend fun insertRisk(risk: Risk): Long = local.insertRisk(risk)

    override suspend fun updateRisk(risk: Risk) {
        local.updateRisk(risk)
    }

    override suspend fun deleteRiskById(riskId: Long) {
        local.deleteRiskById(riskId)
    }

    override suspend fun deleteRisksForProject(projectId: Long) {
        local.deleteRisksForProject(projectId)
    }
}
