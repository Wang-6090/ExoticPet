package com.example.exoticpet.api

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