package com.example.exoticpet.api

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
)