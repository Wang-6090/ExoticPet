package com.example.exoticpet.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 上传图片进行分析
    @Multipart
    @POST("ai/analyze")
    suspend fun analyzePetImage(
        @Part image: MultipartBody.Part,
        @Part("type") type: String,  // health, behavior, diet
        @Part("petInfo") petInfo: String
    ): Response<AnalysisResult>

    // 获取分析历史
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

// API响应数据类
data class AnalysisResult(
    val status: String,        // 健康状态
    val score: Int,            // 评分
    val analysis: String,      // 详细分析
    val suggestions: String,   // 建议
    val warnings: String,      // 注意事项
    val confidence: Float,     // 置信度
    val timestamp: Long        // 时间戳
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