package com.example.exoticpet

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.exoticpet.viewmodel.AnalysisViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.exoticpet.api.BackendRetrofitClient
import com.example.exoticpet.api.RecordRequest
import com.example.exoticpet.models.Pet
import android.util.Log

class AnalysisFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnChooseFromGallery: Button
    private lateinit var btnAnalyze: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultCard: CardView
    private lateinit var tvHealthStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvAnalysis: TextView
    private lateinit var tvSuggestion: TextView
    private lateinit var tvWarning: TextView
    private lateinit var tabHealth: LinearLayout
    private lateinit var tabBehavior: LinearLayout
    private lateinit var tabDiet: LinearLayout

    private lateinit var viewModel: AnalysisViewModel
    private lateinit var dbHelper: PetDatabase
    private var currentImageBitmap: Bitmap? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "需要存储权限才能选择图片", Toast.LENGTH_LONG).show()
        }
    }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                currentImageBitmap = it
                imageView.setImageBitmap(it)
                btnAnalyze.isEnabled = true
                viewModel.clearResult()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageView.setImageURI(uri)
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    currentImageBitmap = bitmap
                    btnAnalyze.isEnabled = true
                    viewModel.clearResult()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "图片加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        try {
            dbHelper = PetDatabase(requireContext())
            viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

            initViews(view)
            setupClickListeners()
            observeViewModel()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun initViews(view: View) {
        imageView = view.findViewById(R.id.imageView)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnChooseFromGallery = view.findViewById(R.id.btnChooseFromGallery)
        btnAnalyze = view.findViewById(R.id.btnAnalyze)
        progressBar = view.findViewById(R.id.progressBar)
        resultCard = view.findViewById(R.id.resultCard)
        tvHealthStatus = view.findViewById(R.id.tvHealthStatus)
        tvScore = view.findViewById(R.id.tvScore)
        tvAnalysis = view.findViewById(R.id.tvAnalysis)
        tvSuggestion = view.findViewById(R.id.tvSuggestion)
        tvWarning = view.findViewById(R.id.tvWarning)
        tabHealth = view.findViewById(R.id.tabHealth)
        tabBehavior = view.findViewById(R.id.tabBehavior)
        tabDiet = view.findViewById(R.id.tabDiet)

        btnAnalyze.isEnabled = false
        resultCard.visibility = View.GONE
    }

    private fun setupClickListeners() {
        btnTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        btnChooseFromGallery.setOnClickListener {
            checkStoragePermission()
        }

        btnAnalyze.setOnClickListener {
            if (currentImageBitmap != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val pet = loadPetForAnalysis()
                        viewModel.analyzeImage(currentImageBitmap!!, pet)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "获取宠物信息失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "请先选择图片", Toast.LENGTH_SHORT).show()
            }
        }

        tabHealth.setOnClickListener {
            updateSelectedTab(tabHealth)
            viewModel.setAnalysisType("health")
        }

        tabBehavior.setOnClickListener {
            updateSelectedTab(tabBehavior)
            viewModel.setAnalysisType("behavior")
        }

        tabDiet.setOnClickListener {
            updateSelectedTab(tabDiet)
            viewModel.setAnalysisType("diet")
        }
    }

    private fun checkStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun checkCameraPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            takePhotoLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun updateSelectedTab(selectedTab: LinearLayout) {
        val defaultBg = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#F5F5F5")
        )
        val selectedBg = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#FF9800")
        )

        tabHealth.backgroundTintList = defaultBg
        tabBehavior.backgroundTintList = defaultBg
        tabDiet.backgroundTintList = defaultBg

        selectedTab.backgroundTintList = selectedBg
    }

    private suspend fun loadPetForAnalysis(): Pet {
        val userId = UserSession.getUserId(requireContext())

        if (userId != 0) {
            try {
                val response = BackendRetrofitClient.instance.getMyPet(userId)
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!

                    val pet = Pet(
                        id = dto.id ?: 1,
                        name = dto.name,
                        species = dto.species,
                        gender = dto.gender ?: "",
                        birthDate = dto.birthDate ?: "",
                        length = dto.length ?: 0.0,
                        weight = dto.weight ?: 0.0,
                        specialMark = dto.specialMark ?: "",
                        enclosureSize = dto.enclosureSize ?: "",
                        stapleFood = dto.stapleFood ?: "",
                        healthScore = dto.healthScore ?: 0,
                        lastCheckup = dto.lastCheckup ?: ""
                    )

                    dbHelper.updatePet(pet)
                    return pet
                }
            } catch (e: Exception) {
                Log.e("AnalysisFragment", "从后端获取宠物资料失败，改用本地资料", e)
            }
        }

        return dbHelper.getPet()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                btnAnalyze.isEnabled = false
                resultCard.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = currentImageBitmap != null
            }
        }

        viewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                resultCard.visibility = View.VISIBLE
                tvHealthStatus.text = "健康状态：${it.status}"
                tvScore.text = "健康评分：${it.score} 分"
                tvAnalysis.text = "分析结果：${it.analysis}"
                tvSuggestion.text = "饲养建议：${it.suggestions}"
                tvWarning.text = "注意事项：${it.warnings}"

                val statusColor = when {
                    it.score >= 75 -> android.graphics.Color.parseColor("#4CAF50")
                    it.score >= 60 -> android.graphics.Color.parseColor("#FF9800")
                    else -> android.graphics.Color.parseColor("#F44336")
                }
                tvHealthStatus.setTextColor(statusColor)

                saveAnalysisRecordToOracle(
                    status = it.status,
                    score = it.score,
                    analysis = it.analysis,
                    suggestions = it.suggestions,
                    confidence = it.confidence
                )
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveAnalysisRecordToOracle(
        status: String,
        score: Int,
        analysis: String,
        suggestions: String,
        confidence: Float
    ) {
        val userId = UserSession.getUserId(requireContext())
        if (userId == 0) return

        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
        val date = now.substring(0, 10)
        val time = now.substring(11)

        val currentType = when (viewModel.analysisType.value ?: "health") {
            "health" -> if (score < 60 || status.contains("紧急") || status.contains("异常")) "异常" else "体检"
            "behavior" -> if (score < 60 || status.contains("异常")) "异常" else "其他"
            "diet" -> if (score < 60 || status.contains("异常")) "异常" else "喂食"
            else -> "体检"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                BackendRetrofitClient.instance.saveAnalysisRecord(
                    RecordRequest(
                        userId = userId,
                        date = date,
                        time = time,
                        type = currentType,
                        description = analysis,
                        suggestion = suggestions,
                        score = score,
                        status = status,
                        confidence = confidence.toDouble(),
                        recordSource = "AI",
                        analysisType = viewModel.analysisType.value ?: "health"
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "AI结果写入历史失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}