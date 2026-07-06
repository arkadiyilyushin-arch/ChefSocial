package com.chefsocial.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChefSocialApi {
    @GET("api/sync")
    suspend fun pull(): SyncResponseDto

    @POST("api/sync")
    suspend fun push(@Body payload: SyncPayloadDto): SyncResponseDto
}
