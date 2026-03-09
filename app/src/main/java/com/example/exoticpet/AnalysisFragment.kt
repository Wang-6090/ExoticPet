package com.example.exoticpet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

class AnalysisFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnChooseFromGallery: Button
    private lateinit var btnAnalyze: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultCard: CardView

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            imageView.setImageBitmap(it)
            btnAnalyze.isEnabled = true
            Toast.makeText(requireContext(), "照片已选择", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageView.setImageURI(uri)
            btnAnalyze.isEnabled = true
            Toast.makeText(requireContext(), "图片已选择", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        initViews(view)
        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        imageView = view.findViewById(R.id.imageView)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnChooseFromGallery = view.findViewById(R.id.btnChooseFromGallery)
        btnAnalyze = view.findViewById(R.id.btnAnalyze)
        progressBar = view.findViewById(R.id.progressBar)
        resultCard = view.findViewById(R.id.resultCard)

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
            progressBar.visibility = View.VISIBLE
            btnAnalyze.isEnabled = false

            view?.postDelayed({
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                resultCard.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "分析完成，请查看结果区域", Toast.LENGTH_SHORT).show()
            }, 2000)
        }
    }
}