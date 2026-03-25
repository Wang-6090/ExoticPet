package com.example.exoticpet.repository

import android.graphics.Bitmap
import com.example.exoticpet.api.AnalysisResult
import com.example.exoticpet.api.RetrofitClient
import com.example.exoticpet.models.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AnalysisRepository {

    private val api = RetrofitClient.instance

    // 分析图片
    suspend fun analyzeImage(
        bitmap: Bitmap,
        type: String,
        pet: Pet
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 将Bitmap转换为文件
            val file = bitmapToFile(bitmap)

            // 创建MultipartBody.Part
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // 创建petInfo
            val petInfo = """
                {
                    "name": "${pet.name}",
                    "species": "${pet.species}",
                    "age": "${pet.birthDate}",
                    "weight": ${pet.weight},
                    "length": ${pet.length}
                }
            """.trimIndent()

            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val petInfoBody = petInfo.toRequestBody("text/plain".toMediaTypeOrNull())

            // 调用API
            val response = api.analyzePetImage(imagePart, type, petInfo)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API调用失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bitmap转File
    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File.createTempFile("temp_image", ".jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }
}