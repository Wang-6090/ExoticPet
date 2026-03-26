package com.example.exoticpet.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.exoticpet.api.AnalysisResult
import com.example.exoticpet.api.QwenVLContent
import com.example.exoticpet.api.QwenVLInput
import com.example.exoticpet.api.QwenVLMessage
import com.example.exoticpet.api.QwenVLRequest
import com.example.exoticpet.api.QwenVLResponse
import com.example.exoticpet.api.RetrofitClient
import com.example.exoticpet.models.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import com.example.exoticpet.config.AiConfig

class AnalysisRepository {

    private val api = RetrofitClient.instance
    private val TAG = "AnalysisRepository"

    suspend fun analyzeImage(
        bitmap: Bitmap,
        type: String,
        pet: Pet,
        customPrompt: String? = null
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val base64Image = bitmapToBase64(bitmap)
            val imageDataUrl = "data:image/jpeg;base64,$base64Image"

            Log.d(TAG, "图片转换完成")

            val prompt = customPrompt ?: buildPromptByType(type, pet)

            val analysisText = callQwenVLAPI(imageDataUrl, prompt)
            Log.d(TAG, "API返回: $analysisText")

            val result = parseAnalysisResult(analysisText)

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "分析失败", e)
            Result.failure(e)
        }
    }

    private suspend fun callQwenVLAPI(
        imageDataUrl: String,
        prompt: String
    ): String {
        val request = QwenVLRequest(
            model = AiConfig.MODEL,
            input = QwenVLInput(
                messages = listOf(
                    QwenVLMessage(
                        role = "user",
                        content = listOf(
                            QwenVLContent(image = imageDataUrl),
                            QwenVLContent(text = prompt)
                        )
                    )
                )
            )
        )

        val response = api.analyzeWithQwenVL(
            authorization = "Bearer ${AiConfig.API_KEY}",
            request = request
        )

        if (response.isSuccessful) {
            val body = response.body()

            if (body?.code != null && body.code != "200" && body.code != "success") {
                throw Exception("API错误: ${body.code} - ${body.message}")
            }

            val content = extractContentFromResponse(body)
            if (content.isBlank()) {
                throw Exception("API返回内容为空")
            }
            return content
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("API调用失败: ${response.code()} - $errorBody")
        }
    }

    private fun extractContentFromResponse(response: QwenVLResponse?): String {
        val message = response?.output?.choices?.firstOrNull()?.message ?: return ""

        return when (val content = message.content) {
            is String -> content
            is List<*> -> {
                val sb = StringBuilder()
                content.forEach { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val text = item["text"] as? String
                            if (!text.isNullOrEmpty()) sb.append(text)
                        }
                        is String -> sb.append(item)
                    }
                }
                sb.toString()
            }
            else -> {
                try {
                    val json = JSONObject(content.toString())
                    json.optString("text", content.toString())
                } catch (e: Exception) {
                    content.toString()
                }
            }
        }
    }

    private fun buildPromptByType(type: String, pet: Pet): String {
        val petInfo =
            "登记宠物资料（仅供参考，可能与图片不一致）：名字=${pet.name}，登记种类=${pet.species}，出生日期=${pet.birthDate}，体重=${pet.weight}g，体长=${pet.length}cm"

        return when (type) {
            "health" -> """
            你是一位专业的异宠兽医。
            请先根据图片本身识别动物种类和健康状态，再参考登记资料辅助判断。
            
            $petInfo
            
            重要要求：
            1. 不要因为登记资料里写了“${pet.species}”，就默认图片中的动物一定是${pet.species}。
            2. 必须优先依据图片内容识别动物。
            3. 如果图片中的动物与登记资料不一致，请明确指出，并以图片中的动物为准。
            4. 如果图片中没有清晰可见的动物，请返回“无法判断”。
            5. 只返回 JSON，不要返回 markdown，不要返回项目符号，不要解释过程。
            
            请严格按以下 JSON 返回：
            {
              "detected_species": "图片中识别到的动物种类，无法确定就写无法确定",
              "pet_info_match": true,
              "status": "健康/需要注意/紧急/无法判断",
              "score": 0,
              "analysis": "根据图片做出的详细分析",
              "suggestions": "饲养建议",
              "warnings": "注意事项",
              "confidence": 0.0
            }
        """.trimIndent()

            "behavior" -> """
            你是一位专业的异宠行为学家。
            请先根据图片本身识别动物种类和行为状态，再参考登记资料辅助判断。
            
            $petInfo
            
            重要要求：
            1. 不要因为登记资料里写了“${pet.species}”，就默认图片中的动物一定是${pet.species}。
            2. 必须优先依据图片内容识别动物和行为状态。
            3. 如果图片中的动物与登记资料不一致，请明确指出，并以图片中的动物为准。
            4. 如果图片中没有清晰可见的动物，请返回“无法判断”。
            5. 只返回 JSON，不要返回 markdown，不要返回项目符号，不要解释过程。
            
            请严格按以下 JSON 返回：
            {
              "detected_species": "图片中识别到的动物种类，无法确定就写无法确定",
              "pet_info_match": true,
              "status": "正常/活跃/异常/无法判断",
              "score": 0,
              "analysis": "行为分析",
              "suggestions": "行为引导建议",
              "warnings": "注意事项",
              "confidence": 0.0
            }
        """.trimIndent()

            else -> """
            你是一位专业的异宠营养师。
            请先根据图片本身识别动物种类和体态，再参考登记资料辅助判断。
            
            $petInfo
            
            重要要求：
            1. 不要因为登记资料里写了“${pet.species}”，就默认图片中的动物一定是${pet.species}。
            2. 必须优先依据图片内容识别动物和体态。
            3. 如果图片中的动物与登记资料不一致，请明确指出，并以图片中的动物为准。
            4. 如果图片中没有清晰可见的动物，请返回“无法判断”。
            5. 只返回 JSON，不要返回 markdown，不要返回项目符号，不要解释过程。
            
            请严格按以下 JSON 返回：
            {
              "detected_species": "图片中识别到的动物种类，无法确定就写无法确定",
              "pet_info_match": true,
              "status": "正常/需改善/异常/无法判断",
              "score": 0,
              "analysis": "饮食或体态分析",
              "suggestions": "饮食建议",
              "warnings": "注意事项",
              "confidence": 0.0
            }
        """.trimIndent()
        }
    }

    private fun parseAnalysisResult(aiResponse: String): AnalysisResult {
        return try {
            var cleanedResponse = aiResponse.trim()

            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.removePrefix("```").removeSuffix("```").trim()
            }

            val jsonStart = cleanedResponse.indexOf("{")
            val jsonEnd = cleanedResponse.lastIndexOf("}")
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanedResponse = cleanedResponse.substring(jsonStart, jsonEnd + 1)
            }

            val json = JSONObject(cleanedResponse)

            AnalysisResult(
                status = json.optString("status", "分析完成"),
                score = json.optInt("score", 80),
                analysis = json.optString("analysis", aiResponse.take(200)),
                suggestions = json.optString("suggestions", "请咨询专业兽医获取更详细建议"),
                warnings = json.optString("warnings", "如有异常请及时就医"),
                confidence = json.optDouble("confidence", 0.85).toFloat(),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            AnalysisResult(
                status = "分析完成",
                score = 75,
                analysis = if (aiResponse.isNotEmpty()) aiResponse.take(300) else "AI分析完成，请查看详细结果",
                suggestions = "AI分析结果仅供参考，如有疑问请咨询专业兽医",
                warnings = "本分析结果不构成医疗诊断",
                confidence = 0.7f,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}