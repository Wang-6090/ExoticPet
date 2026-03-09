package com.example.exoticpet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class MonitorFragment : Fragment() {

    private lateinit var switchMonitoring: Switch
    private lateinit var tvStatus: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvActivity: TextView
    private lateinit var progressBarTemp: ProgressBar
    private lateinit var progressBarHumidity: ProgressBar
    private lateinit var btnSettings: ImageView
    private lateinit var alertCard: CardView
    private lateinit var tvAlertMessage: TextView
    private lateinit var btnDismissAlert: Button
    private lateinit var alertCardLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monitor, container, false)

        initViews(view)
        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        switchMonitoring = view.findViewById(R.id.switchMonitoring)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvTemperature = view.findViewById(R.id.tvTemperature)
        tvHumidity = view.findViewById(R.id.tvHumidity)
        tvActivity = view.findViewById(R.id.tvActivity)
        progressBarTemp = view.findViewById(R.id.progressBarTemp)
        progressBarHumidity = view.findViewById(R.id.progressBarHumidity)
        btnSettings = view.findViewById(R.id.btnSettings)
        alertCard = view.findViewById(R.id.alertCard)
        tvAlertMessage = view.findViewById(R.id.tvAlertMessage)
        btnDismissAlert = view.findViewById(R.id.btnDismissAlert)
        alertCardLayout = view.findViewById(R.id.alertCardLayout)

        tvTemperature.text = "【温度数据】"
        tvHumidity.text = "【湿度数据】"
        tvActivity.text = "【活动状态】"
        tvAlertMessage.text = "【此处用于输出警报信息】"

        progressBarTemp.progress = 0
        progressBarHumidity.progress = 0
    }

    private fun setupClickListeners() {
        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvStatus.text = "监控中"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                Toast.makeText(requireContext(), "监控已开启", Toast.LENGTH_SHORT).show()

                tvTemperature.text = "28°C"
                tvHumidity.text = "55%"
                tvActivity.text = "轻微活动"
                progressBarTemp.progress = 60
                progressBarHumidity.progress = 50
            } else {
                tvStatus.text = "已停止"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
                Toast.makeText(requireContext(), "监控已关闭", Toast.LENGTH_SHORT).show()

                tvTemperature.text = "【温度数据】"
                tvHumidity.text = "【湿度数据】"
                tvActivity.text = "【活动状态】"
                progressBarTemp.progress = 0
                progressBarHumidity.progress = 0
            }
        }

        btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "设置界面（可配置监控参数）", Toast.LENGTH_SHORT).show()
        }

        btnDismissAlert.setOnClickListener {
            alertCard.visibility = View.GONE
        }

        alertCardLayout.setOnLongClickListener {
            alertCard.visibility = View.VISIBLE
            tvAlertMessage.text = "【示例警报】温度过高，请注意"
            true
        }
    }
}