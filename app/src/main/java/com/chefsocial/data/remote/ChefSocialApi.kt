package com.chefsocial.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ChefSocialApi {
    @GET("api/sync")
    suspend fun pull(): SyncResponseDto

    @POST("api/sync")
    suspend fun push(@Body payload: SyncPayloadDto): SyncResponseDto

    @Multipart
    @POST("api/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): UploadResponseDto
}
