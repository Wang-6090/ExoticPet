package com.example.exoticpet.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
    suspend fun analyzeWithQwenVL(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: QwenVLRequest
    ): Response<QwenVLResponse>

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
    val token: String? = null,
    val profileCompleted: Boolean = false
)

data class PetProfileDto(
    val id: Int? = null,
    val userId: Int,
    val name: String,
    val species: String,
    val gender: String? = null,
    val birthDate: String? = null,
    val length: Double? = null,
    val weight: Double? = null,
    val specialMark: String? = null,
    val enclosureSize: String? = null,
    val stapleFood: String? = null,
    val healthScore: Int? = 0,
    val lastCheckup: String? = null
)

data class PetProfileRequest(
    val userId: Int,
    val name: String,
    val species: String,
    val gender: String,
    val birthDate: String,
    val length: Double,
    val weight: Double,
    val specialMark: String,
    val enclosureSize: String,
    val stapleFood: String,
    val healthScore: Int = 0,
    val lastCheckup: String = "首次建档"
)

data class RecordDto(
    val id: Int = 0,
    val date: String? = null,
    val time: String? = null,
    val type: String? = null,
    val description: String? = null,
    val suggestion: String? = null,
    val score: Int? = null,
    val status: String? = null,
    val confidence: Double? = null,
    val recordSource: String? = null,
    val analysisType: String? = null
)

data class RecordRequest(
    val userId: Int,
    val date: String,
    val time: String,
    val type: String,
    val description: String,
    val suggestion: String,
    val score: Int? = null,
    val status: String? = null,
    val confidence: Double? = null,
    val recordSource: String? = "MANUAL",
    val analysisType: String? = null
)

data class SimpleApiResponse(
    val success: Boolean,
    val message: String
)