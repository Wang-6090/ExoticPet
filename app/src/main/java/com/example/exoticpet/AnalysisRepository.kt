package com.example.exoticpet.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.exoticpet.api.*
import com.example.exoticpet.models.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import org.json.JSONArray
import org.json.JSONObject

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

            val result = parseAnalysisResult(analysisText, type)

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
            model = "qwen-vl-plus",
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

        Log.d(TAG, "开始调用API...")

        val response = api.analyzeWithQwenVL(
            authorization = "Bearer ${RetrofitClient.DASHSCOPE_API_KEY}",
            request = request
        )

        Log.d(TAG, "API响应码: ${response.code()}")

        return if (response.isSuccessful) {
            val body = response.body()
            Log.d(TAG, "响应体: $body")

            if (body?.code != null && body.code != "200" && body.code != "success") {
                throw Exception("API错误: ${body.code} - ${body.message}")
            }

            // ✅ 关键修复：解析 content（可能是字符串或数组）
            val content = extractContentFromResponse(body)
            if (content.isNullOrEmpty()) {
                throw Exception("API返回内容为空")
            }
            content
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "HTTP错误: ${response.code()}, 错误体: $errorBody")
            throw Exception("API调用失败: ${response.code()} - $errorBody")
        }
    }

    /**
     * ✅ 新增：从响应中提取 content 内容
     * 处理 content 可能是字符串或数组的情况
     */
    private fun extractContentFromResponse(response: QwenVLResponse?): String {
        val message = response?.output?.choices?.firstOrNull()?.message ?: return ""

        return when (val content = message.content) {
            is String -> {
                // 如果是字符串，直接返回
                content
            }
            is List<*> -> {
                // 如果是数组，提取所有 text 字段拼接
                val stringBuilder = StringBuilder()
                content.forEach { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val text = item["text"] as? String
                            if (!text.isNullOrEmpty()) {
                                stringBuilder.append(text)
                            }
                        }
                        is String -> {
                            stringBuilder.append(item)
                        }
                    }
                }
                stringBuilder.toString()
            }
            else -> {
                // 其他情况，尝试转成 JSON 再解析
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
        val petInfo = "宠物信息：${pet.name}，种类：${pet.species}，年龄：${pet.birthDate}，体重：${pet.weight}g，体长：${pet.length}cm"

        return when (type) {
            "health" -> """
                你是一位专业的异宠兽医。请分析这张图片中的宠物健康状况。
                $petInfo
                
                请仔细分析图片中的宠物，包括：
                1. 整体外观状态
                2. 皮肤/鳞片/毛发状况
                3. 体型和姿态
                4. 眼睛、四肢等细节
                
                请严格按以下JSON格式回答，只返回JSON，不要有其他内容：
                {
                    "status": "健康/需要注意/紧急",
                    "score": 0-100的评分,
                    "analysis": "详细分析结果（100字内）",
                    "suggestions": "饲养建议（80字内）",
                    "warnings": "注意事项（50字内）",
                    "confidence": 置信度0-1
                }
            """.trimIndent()

            "behavior" -> """
                你是一位专业的异宠行为学家。请分析这张图片中宠物的行为状态。
                $petInfo
                
                请分析图片中宠物的行为表现，包括：
                1. 活动状态（活跃/安静/紧张）
                2. 姿态和动作
                3. 对环境反应
                
                请严格按以下JSON格式回答，只返回JSON，不要有其他内容：
                {
                    "status": "正常/活跃/异常",
                    "score": 0-100的行为评分,
                    "analysis": "行为分析（100字内）",
                    "suggestions": "行为引导建议（80字内）",
                    "warnings": "注意事项（50字内）",
                    "confidence": 置信度0-1
                }
            """.trimIndent()

            else -> """
                你是一位专业的异宠营养师。请分析这张图片中宠物的饮食状况。
                $petInfo
                
                请分析图片中与饮食相关的内容：
                1. 体型判断（偏瘦/正常/偏胖）
                2. 进食环境
                3. 可能的营养状况
                
                请严格按以下JSON格式回答，只返回JSON，不要有其他内容：
                {
                    "status": "正常/需改善/异常",
                    "score": 0-100的饮食评分,
                    "analysis": "饮食分析（100字内）",
                    "suggestions": "饮食建议（80字内）",
                    "warnings": "注意事项（50字内）",
                    "confidence": 置信度0-1
                }
            """.trimIndent()
        }
    }

    private fun parseAnalysisResult(aiResponse: String, type: String): AnalysisResult {
        return try {
            var cleanedResponse = aiResponse.trim()
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.removePrefix("```").removeSuffix("```").trim()
            }

            Log.d(TAG, "清理后的响应: $cleanedResponse")

            val json = org.json.JSONObject(cleanedResponse)
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
            Log.e(TAG, "JSON解析失败", e)
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