//package com.example.exoticpet
//
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.ProgressBar
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.fragment.app.Fragment
//import androidx.cardview.widget.CardView
//
//class AnalysisFragment : Fragment() {
//
//    private lateinit var imageView: ImageView
//    private lateinit var btnTakePhoto: Button
//    private lateinit var btnChooseFromGallery: Button
//    private lateinit var btnAnalyze: Button
//    private lateinit var progressBar: ProgressBar
//    private lateinit var resultCard: CardView
//
//    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
//        bitmap?.let {
//            imageView.setImageBitmap(it)
//            btnAnalyze.isEnabled = true
//            Toast.makeText(requireContext(), "照片已选择", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let {
//            imageView.setImageURI(uri)
//            btnAnalyze.isEnabled = true
//            Toast.makeText(requireContext(), "图片已选择", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_analysis, container, false)
//
//        initViews(view)
//        setupClickListeners()
//
//        return view
//    }
//
//    private fun initViews(view: View) {
//        imageView = view.findViewById(R.id.imageView)
//        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
//        btnChooseFromGallery = view.findViewById(R.id.btnChooseFromGallery)
//        btnAnalyze = view.findViewById(R.id.btnAnalyze)
//        progressBar = view.findViewById(R.id.progressBar)
//        resultCard = view.findViewById(R.id.resultCard)
//
//        btnAnalyze.isEnabled = false
//        resultCard.visibility = View.GONE
//    }
//
//    private fun setupClickListeners() {
//        btnTakePhoto.setOnClickListener {
//            takePhotoLauncher.launch(null)
//        }
//
//        btnChooseFromGallery.setOnClickListener {
//            pickImageLauncher.launch("image/*")
//        }
//
//        btnAnalyze.setOnClickListener {
//            progressBar.visibility = View.VISIBLE
//            btnAnalyze.isEnabled = false
//
//            view?.postDelayed({
//                progressBar.visibility = View.GONE
//                btnAnalyze.isEnabled = true
//                resultCard.visibility = View.VISIBLE
//                Toast.makeText(requireContext(), "分析完成，请查看结果区域", Toast.LENGTH_SHORT).show()
//            }, 2000)
//        }
//    }
//}

package com.example.exoticpet

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.exoticpet.viewmodel.AnalysisViewModel
import android.widget.LinearLayout
import com.example.exoticpet.models.Pet

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

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            currentImageBitmap = it
            imageView.setImageBitmap(it)
            btnAnalyze.isEnabled = true
            viewModel.clearResult()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageView.setImageURI(uri)
            // 将URI转换为Bitmap
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                currentImageBitmap = bitmap
                btnAnalyze.isEnabled = true
                viewModel.clearResult()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        dbHelper = PetDatabase(requireContext())
        viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

        initViews(view)
        setupClickListeners()
        observeViewModel()

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
            takePhotoLauncher.launch(null)
        }

        btnChooseFromGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAnalyze.setOnClickListener {
            if (currentImageBitmap != null) {
                val pet = dbHelper.getPet()
                viewModel.analyzeImage(currentImageBitmap!!, pet)
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

                // 根据评分设置颜色
                val statusColor = when {
                    it.score >= 75 -> android.graphics.Color.parseColor("#4CAF50")
                    it.score >= 60 -> android.graphics.Color.parseColor("#FF9800")
                    else -> android.graphics.Color.parseColor("#F44336")
                }
                tvHealthStatus.setTextColor(statusColor)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
}