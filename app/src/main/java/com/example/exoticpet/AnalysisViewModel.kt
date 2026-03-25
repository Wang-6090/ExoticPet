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

    private val _analysisResult = MutableLiveData<AnalysisResult?>()
    val analysisResult: LiveData<AnalysisResult?> = _analysisResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _analysisType = MutableLiveData("health")
    val analysisType: LiveData<String> = _analysisType

    fun setAnalysisType(type: String) {
        _analysisType.value = type
    }

    fun analyzeImage(bitmap: Bitmap, pet: Pet) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentType = _analysisType.value ?: "health"
                val result = repository.analyzeImage(bitmap, currentType, pet)

                result.onSuccess { analysisResult ->
                    _analysisResult.value = analysisResult
                }.onFailure { exception ->
                    _error.value = "分析失败: ${exception.message}"
                    exception.printStackTrace()
                }
            } catch (e: Exception) {
                _error.value = "异常: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _analysisResult.value = null
        _error.value = null
    }
}