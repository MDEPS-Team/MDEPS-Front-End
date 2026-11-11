package com.example.mdeps_application.gro.data.remote

import com.example.mdeps_application.gro.data.remote.dto.RiskMatrixResourceDto
import com.example.mdeps_application.gro.data.remote.dto.RiskResourceDto
import retrofit2.http.GET

interface GroApiService {

    @GET("api/v1/risks")
    suspend fun getRisks(): List<RiskResourceDto>

    @GET("api/v1/risk-matrix")
    suspend fun getRiskMatrix(): List<RiskMatrixResourceDto>
}
