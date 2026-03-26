package com.example.exoticpet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exoticpet.api.RecordDto
import com.example.exoticpet.api.BackendRetrofitClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvSpecies: TextView
    private lateinit var tvLength: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHealthScore: TextView
    private lateinit var tvLastCheckup: TextView
    private lateinit var lineChart: LineChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnEdit: ImageView
    private lateinit var btnLogout: ImageView
    private lateinit var btnAddRecord: Button
    private lateinit var btnViewHistory: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initViews(view)
        setupClickListeners()
        loadPetData()
        loadRecentRecords()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPetData()
        loadRecentRecords()
    }

    private fun initViews(view: View) {
        tvName = view.findViewById(R.id.tvName)
        tvSpecies = view.findViewById(R.id.tvSpecies)
        tvLength = view.findViewById(R.id.tvLength)
        tvWeight = view.findViewById(R.id.tvWeight)
        tvHealthScore = view.findViewById(R.id.tvHealthScore)
        tvLastCheckup = view.findViewById(R.id.tvLastCheckup)
        lineChart = view.findViewById(R.id.lineChart)
        recyclerView = view.findViewById(R.id.recyclerView)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnAddRecord = view.findViewById(R.id.btnAddRecord)
        btnViewHistory = view.findViewById(R.id.btnViewHistory)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        btnEdit.setOnClickListener {
            startActivity(Intent(requireContext(), EditPetActivity::class.java))
        }

        btnLogout.setOnClickListener {
            UserSession.clear(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        btnAddRecord.setOnClickListener {
            startActivity(Intent(requireContext(), AddRecordActivity::class.java))
        }

        btnViewHistory.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.bottomNavigation.selectedItemId = R.id.navigation_history
        }
    }

    private fun loadPetData() {
        val userId = UserSession.getUserId(requireContext())
        if (userId == 0) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = BackendRetrofitClient.instance.getMyPet(userId)
                val pet = response.body()

                if (response.isSuccessful && pet != null) {
                    tvName.text = pet.name
                    tvSpecies.text = "${pet.species} | 年龄：${calculateAge(pet.birthDate ?: "")}"
                    tvLength.text = "体长：${pet.length ?: 0.0}cm"
                    tvWeight.text = "体重：${pet.weight ?: 0.0}g"
                    tvHealthScore.text = "最新分析：${pet.healthScore ?: 0}分"
                    tvLastCheckup.text = "上次体检：${pet.lastCheckup ?: "暂无"}"
                    setupGrowthChart(pet.weight?.toFloat() ?: 0f)
                } else {
                    tvName.text = "还未建档"
                    tvSpecies.text = "请先填写宠物问卷"
                    tvLength.text = "体长：--"
                    tvWeight.text = "体重：--"
                    tvHealthScore.text = "最新分析：暂无"
                    tvLastCheckup.text = "上次体检：暂无"
                    setupGrowthChart(0f)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载宠物档案失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateAge(birthDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birth = format.parse(birthDate) ?: return "未知"
            val now = Date()
            val diff = now.time - birth.time
            val months = (diff / (1000L * 60 * 60 * 24 * 30)).toInt()
            "${months}个月"
        } catch (e: Exception) {
            "未知"
        }
    }

    private fun loadRecentRecords() {
        val userId = UserSession.getUserId(requireContext())
        if (userId == 0) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = BackendRetrofitClient.instance.getRecords(userId, "全部")
                val records = response.body().orEmpty()
                val recentRecords = if (records.size > 2) records.subList(0, 2) else records
                recyclerView.adapter = RecentRecordsAdapter(recentRecords)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载历史记录失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGrowthChart(currentWeight: Float) {
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, if (currentWeight > 0) currentWeight * 0.8f else 0f))
        entries.add(Entry(1f, if (currentWeight > 0) currentWeight * 0.9f else 0f))
        entries.add(Entry(2f, if (currentWeight > 0) currentWeight * 0.95f else 0f))
        entries.add(Entry(3f, currentWeight))

        val dataSet = LineDataSet(entries, "体重趋势(g)")
        dataSet.color = android.graphics.Color.parseColor("#FF9800")
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(android.graphics.Color.parseColor("#FF9800"))

        lineChart.data = LineData(dataSet)
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }

    inner class RecentRecordsAdapter(private val records: List<RecordDto>) :
        RecyclerView.Adapter<RecentRecordsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            val tvType: TextView = itemView.findViewById(R.id.tvType)
            val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recent_record, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = records[position]

            val dateText = record.date ?: ""
            holder.tvDate.text =
                if (dateText.length >= 5) dateText.substring(5) else if (dateText.isNotBlank()) dateText else "--"

            holder.tvType.text = record.type ?: "未分类"
            holder.tvDescription.text = record.description ?: "无描述"
        }

        override fun getItemCount() = records.size
    }
}