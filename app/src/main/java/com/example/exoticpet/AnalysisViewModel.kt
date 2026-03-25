package com.example.exoticpet.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exoticpet.api.AnalysisResult
import com.example.exoticpet.models.Pet
import com.example.exoticpet.repository.AnalysisRepository
import kotlinx.coroutines.launch

class AnalysisViewModel : ViewModel() {

    private val repository = AnalysisRepository()

    // 分析结果
    private val _analysisResult = MutableLiveData<AnalysisResult?>()
    val analysisResult: LiveData<AnalysisResult?> = _analysisResult

    // 加载状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 分析类型
    private val _analysisType = MutableLiveData("health")
    val analysisType: LiveData<String> = _analysisType

    fun setAnalysisType(type: String) {
        _analysisType.value = type
    }

    // 分析图片
    fun analyzeImage(bitmap: Bitmap, pet: Pet) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.analyzeImage(bitmap, _analysisType.value!!, pet)

                result.onSuccess { analysisResult ->
                    _analysisResult.value = analysisResult
                    // 保存到数据库（可选）
                    saveAnalysisToDatabase(analysisResult)
                }.onFailure { exception ->
                    _error.value = "分析失败: ${exception.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 保存分析结果到数据库（可选）
    private fun saveAnalysisToDatabase(result: AnalysisResult) {
        // TODO: 实现数据库保存
    }

    // 清除结果
    fun clearResult() {
        _analysisResult.value = null
    }
}