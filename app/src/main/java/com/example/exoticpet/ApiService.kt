package com.example.exoticpet.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 通义千问VL多模态分析接口
    @POST("api/v1/services/aigc/multimodal-generation/generation")
    suspend fun analyzeWithQwenVL(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: QwenVLRequest
    ): Response<QwenVLResponse>
}

// ============ 通义千问VL官方格式 ============
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

// ✅ 修正：响应体中的 content 可能是数组
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

// ✅ 修正：content 可能是字符串或数组
data class QwenVLResultMessage(
    val role: String,
    val content: Any  // 改为 Any 类型，可以接收字符串或数组
)

// 统一的分析结果数据类
data class AnalysisResult(
    val status: String,
    val score: Int,
    val analysis: String,
    val suggestions: String,
    val warnings: String,
    val confidence: Float,
    val timestamp: Long
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