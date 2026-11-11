package com.example.mdeps_application.pm.data.remote

import com.example.mdeps_application.pm.data.remote.dto.ProfileResourceDto
import com.example.mdeps_application.pm.data.remote.dto.ProfileStatsDto
import com.example.mdeps_application.pm.data.remote.dto.PatchUserResourceDto
import com.example.mdeps_application.pm.data.remote.dto.UpdateProfileResourceDto
import com.example.mdeps_application.pm.data.remote.dto.UserSettingsDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PmApiService {

    @GET("api/v1/users/{id}")
    suspend fun getProfileByUserId(@Path("id") userId: Int): ProfileResourceDto

    @PATCH("api/v1/users/{id}")
    suspend fun patchUserById(
        @Path("id") userId: Int,
        @Body body: PatchUserResourceDto,
    ): ProfileResourceDto

    @GET("api/v1/profiles")
    suspend fun getProfileByEmail(@Query("email") email: String): ProfileResourceDto

    @GET("api/v1/profiles/{id}")
    suspend fun getProfileById(@Path("id") id: Int): ProfileResourceDto

    @PATCH("api/v1/profiles/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body body: UpdateProfileResourceDto,
    ): ProfileResourceDto

    @GET("api/v1/stats")
    suspend fun getStats(@Query("userId") userId: Int): List<ProfileStatsDto>

    @GET("api/v1/user-settings")
    suspend fun getUserSettings(): Response<UserSettingsDto>

    @PUT("api/v1/user-settings")
    suspend fun putUserSettings(@Body body: UserSettingsDto): Response<UserSettingsDto>
}
