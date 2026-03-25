package com.example.exoticpet

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exoticpet.viewmodel.AnalysisViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MonitorFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var switchMonitoring: Switch
    private lateinit var tvStatus: TextView
    private lateinit var tvLastAnalysis: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvActivity: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var alertCard: LinearLayout
    private lateinit var tvAlertMessage: TextView
    private lateinit var btnDismissAlert: Button
    private lateinit var spinnerInterval: Spinner
    private lateinit var btnTakeSnapshot: Button

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: Camera? = null

    private lateinit var dbHelper: PetDatabase
    private lateinit var analysisViewModel: AnalysisViewModel

    private var isMonitoring = false
    private var monitorJob: kotlinx.coroutines.Job? = null
    private var intervalMinutes = 5L

    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_monitor, container, false)

        initViews(view)
        initData()
        setupClickListeners()
        checkCameraPermission()

        return view
    }

    private fun initViews(view: View) {
        previewView = view.findViewById(R.id.previewView)
        switchMonitoring = view.findViewById(R.id.switchMonitoring)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvLastAnalysis = view.findViewById(R.id.tvLastAnalysis)
        tvTemperature = view.findViewById(R.id.tvTemperature)
        tvHumidity = view.findViewById(R.id.tvHumidity)
        tvActivity = view.findViewById(R.id.tvActivity)
        btnSettings = view.findViewById(R.id.btnSettings)
        alertCard = view.findViewById(R.id.alertCardLayout)
        tvAlertMessage = view.findViewById(R.id.tvAlertMessage)
        btnDismissAlert = view.findViewById(R.id.btnDismissAlert)
        spinnerInterval = view.findViewById(R.id.spinnerInterval)
        btnTakeSnapshot = view.findViewById(R.id.btnTakeSnapshot)

        val intervals = arrayOf("1分钟", "5分钟", "10分钟", "30分钟", "1小时")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapter
        spinnerInterval.setSelection(1)

        tvLastAnalysis.text = "暂无分析结果"
        alertCard.visibility = View.GONE
    }

    private fun initData() {
        dbHelper = PetDatabase(requireContext())
        analysisViewModel =
            androidx.lifecycle.ViewModelProvider(requireActivity())[AnalysisViewModel::class.java]
        cameraExecutor = Executors.newSingleThreadExecutor()

        analysisViewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                tvLastAnalysis.text = "[$time] 评分：${it.score}分 - ${it.status}\n${it.analysis}"

                if (it.warnings.isNotEmpty() && it.warnings != "本分析结果不构成医疗诊断") {
                    showAlert(it.warnings)
                }
            }
        }

        analysisViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                tvStatus.text = "分析中..."
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            } else if (isMonitoring) {
                tvStatus.text = "监控中"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }
    }

    private fun setupClickListeners() {
        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startMonitoring()
            } else {
                stopMonitoring()
            }
        }

        btnTakeSnapshot.setOnClickListener {
            takeSnapshotAndAnalyze()
        }

        btnSettings.setOnClickListener {
            showIntervalDialog()
        }

        btnDismissAlert.setOnClickListener {
            alertCard.visibility = View.GONE
        }
    }

    private fun startMonitoring() {
        if (!hasCameraPermission()) {
            checkCameraPermission()
            switchMonitoring.isChecked = false
            return
        }

        isMonitoring = true
        val selectedPosition = spinnerInterval.selectedItemPosition
        intervalMinutes = when (selectedPosition) {
            0 -> 1
            1 -> 5
            2 -> 10
            3 -> 30
            else -> 60
        }

        tvStatus.text = "监控中"
        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

        monitorJob = lifecycleScope.launch {
            while (isMonitoring) {
                takeSnapshotAndAnalyze()
                delay(intervalMinutes * 60 * 1000)
            }
        }

        Toast.makeText(requireContext(), "监控已启动，间隔${intervalMinutes}分钟", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoring() {
        isMonitoring = false
        monitorJob?.cancel()

        tvStatus.text = "已停止"
        tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))

        Toast.makeText(requireContext(), "监控已停止", Toast.LENGTH_SHORT).show()
    }

    private fun takeSnapshotAndAnalyze() {
        if (!hasCameraPermission()) {
            Toast.makeText(requireContext(), "需要相机权限", Toast.LENGTH_SHORT).show()
            return
        }

        val imageCapture = imageCapture ?: run {
            Toast.makeText(requireContext(), "相机未就绪", Toast.LENGTH_SHORT).show()
            return
        }

        val outputDir = File(requireContext().cacheDir, "snapshots")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(outputDir, "snapshot_$timestamp.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        val rotatedBitmap = rotateBitmapIfNeeded(bitmap)
                        analyzeImage(rotatedBitmap)
                    } else {
                        Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("MonitorFragment", "拍照失败", exception)
                    Toast.makeText(requireContext(), "拍照失败: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun analyzeImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val pet = dbHelper.getPet()
                val compressedBitmap = compressBitmap(bitmap)
                analysisViewModel.analyzeImage(compressedBitmap, pet)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "分析失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 1024
        val width = bitmap.width
        val height = bitmap.height

        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }

        if (scale < 1) {
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
        return bitmap
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        return bitmap
    }

    private fun showAlert(message: String) {
        tvAlertMessage.text = message
        alertCard.visibility = View.VISIBLE

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (alertCard.visibility == View.VISIBLE) {
                alertCard.visibility = View.GONE
            }
        }, 5000)
    }

    private fun showIntervalDialog() {
        val intervals = arrayOf("1分钟", "5分钟", "10分钟", "30分钟", "1小时")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("设置监控间隔")
            .setItems(intervals) { _, which ->
                spinnerInterval.setSelection(which)
                if (isMonitoring) {
                    Toast.makeText(requireContext(), "监控间隔已更改，将下次生效", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("MonitorFragment", "相机启动失败", exc)
                Toast.makeText(requireContext(), "相机启动失败: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "需要相机权限才能使用监控功能", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        monitorJob?.cancel()
    }
}