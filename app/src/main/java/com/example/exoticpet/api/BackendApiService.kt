package com.example.exoticpet.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface BackendApiService {

    @Multipart
    @POST("ai/analyze")
    suspend fun analyzePetImage(
        @Part image: MultipartBody.Part,
        @Part("type") type: String,
        @Part("petInfo") petInfo: String
    ): Response<AnalysisResult>

    @GET("ai/history")
    suspend fun getAnalysisHistory(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<List<AnalysisHistory>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("pets/me")
    suspend fun getMyPet(
        @Query("userId") userId: Int
    ): Response<PetProfileDto>

    @PUT("pets/me")
    suspend fun saveMyPet(
        @Body request: PetProfileRequest
    ): Response<SimpleApiResponse>

    @GET("records")
    suspend fun getRecords(
        @Query("userId") userId: Int,
        @Query("type") type: String? = null
    ): Response<List<RecordDto>>

    @POST("records/manual")
    suspend fun addManualRecord(
        @Body request: RecordRequest
    ): Response<SimpleApiResponse>

    @POST("records/analysis")
    suspend fun saveAnalysisRecord(
        @Body request: RecordRequest
    ): Response<SimpleApiResponse>
}