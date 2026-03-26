package com.example.exoticpet.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DashScopeApiService {

    @POST("api/v1/services/aigc/multimodal-generation/generation")
    suspend fun analyzeWithQwenVL(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: QwenVLRequest
    ): Response<QwenVLResponse>
}