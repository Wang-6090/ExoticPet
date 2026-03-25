package com.example.exoticpet.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // 通义千问VL多模态分析接口
    @POST("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
    suspend fun analyzeWithQwenVL(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: QwenVLRequest
    ): Response<QwenVLResponse>

    // 后端图片分析接口（保留，不删功能）
    @Multipart
    @POST("ai/analyze")
    suspend fun analyzePetImage(
        @Part image: MultipartBody.Part,
        @Part("type") type: String,
        @Part("petInfo") petInfo: String
    ): Response<AnalysisResult>

    // 分析历史
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
}

// ============ 通义千问VL请求/响应模型 ============

data class QwenVLRequest(
    val model: String = "qwen-vl-plus",
    val input: QwenVLInput
)

data class QwenVLInput(
    val messages: List<QwenVLMessage>
)

data class QwenVLMessage(
    val role: String = "user",
    val content: List<QwenVLContent>
)

data class QwenVLContent(
    val text: String? = null,
    val image: String? = null
)

data class QwenVLResponse(
    val output: QwenVLOutput?,
    val code: String?,
    val message: String?
)

data class QwenVLOutput(
    val choices: List<QwenVLChoice>
)

data class QwenVLChoice(
    val message: QwenVLResultMessage
)

data class QwenVLResultMessage(
    val role: String,
    val content: Any
)

// ============ 业务数据模型 ============

data class AnalysisResult(
    val status: String,
    val score: Int,
    val analysis: String,
    val suggestions: String,
    val warnings: String,
    val confidence: Float,
    val timestamp: Long
)

data class AnalysisHistory(
    val id: String,
    val imageUrl: String,
    val result: AnalysisResult,
    val createdAt: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null,
    val username: String? = null,
    val token: String? = null
)