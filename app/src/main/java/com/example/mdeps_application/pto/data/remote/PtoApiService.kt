package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.pto.data.remote.dto.BoardResourceDto
import com.example.mdeps_application.pto.data.remote.dto.CollaborationTaskDto
import com.example.mdeps_application.pto.data.remote.dto.CreateCollaborationTaskDto
import com.example.mdeps_application.pto.data.remote.dto.CreateProjectResourceDto
import com.example.mdeps_application.pto.data.remote.dto.ExecutiveReportDto
import com.example.mdeps_application.pto.data.remote.dto.ProjectResourceDto
import com.example.mdeps_application.pto.data.remote.dto.UpdateCollaborationTaskDto
import com.example.mdeps_application.pto.data.remote.dto.UpdateProjectResourceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PtoApiService {

    @GET("api/v1/projects")
    suspend fun getProjects(): List<ProjectResourceDto>

    @GET("api/v1/projects/{id}")
    suspend fun getProject(@Path("id") id: Int): ProjectResourceDto

    @POST("api/v1/projects")
    suspend fun createProject(@Body body: CreateProjectResourceDto): ProjectResourceDto

    @PUT("api/v1/projects/{id}")
    suspend fun updateProject(
        @Path("id") id: Int,
        @Body body: UpdateProjectResourceDto,
    ): ProjectResourceDto

    @GET("api/v1/boards")
    suspend fun getBoards(): List<BoardResourceDto>

    @GET("api/v1/tasks")
    suspend fun getTasks(
        @Query("boardId") boardId: Int? = null,
        @Query("scope") scope: String? = null,
    ): List<CollaborationTaskDto>

    @POST("api/v1/tasks")
    suspend fun createTask(@Body body: CreateCollaborationTaskDto): CollaborationTaskDto

    @PUT("api/v1/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body body: UpdateCollaborationTaskDto,
    ): CollaborationTaskDto

    @DELETE("api/v1/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>

    @GET("api/v1/reports")
    suspend fun getExecutiveReports(): List<ExecutiveReportDto>
}
