package com.example.exoticpet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exoticpet.models.Pet
import com.example.exoticpet.models.Record
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dbHelper: PetDatabase
    private lateinit var tvName: TextView
    private lateinit var tvSpecies: TextView
    private lateinit var tvLength: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHealthScore: TextView
    private lateinit var tvLastCheckup: TextView
    private lateinit var lineChart: LineChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnEdit: ImageView
    private lateinit var btnAddRecord: Button
    private lateinit var btnViewHistory: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dbHelper = PetDatabase(requireContext())
        initViews(view)
        setupClickListeners()
        loadPetData()
        loadRecentRecords()
        setupGrowthChart()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPetData()
        loadRecentRecords()
        setupGrowthChart()
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
        btnAddRecord = view.findViewById(R.id.btnAddRecord)
        btnViewHistory = view.findViewById(R.id.btnViewHistory)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        btnEdit.setOnClickListener {
            startActivity(Intent(requireContext(), EditPetActivity::class.java))
        }

        btnAddRecord.setOnClickListener {
            startActivity(Intent(requireContext(), AddRecordActivity::class.java))
        }

        btnViewHistory.setOnClickListener {
            // 通过底部导航切换到历史记录
            val activity = requireActivity() as MainActivity
            activity.bottomNavigation.selectedItemId = R.id.navigation_history
        }
    }

    private fun loadPetData() {
        val pet = dbHelper.getPet()
        tvName.text = pet.name
        tvSpecies.text = "${pet.species} | 年龄：${calculateAge(pet.birthDate)}"
        tvLength.text = "体长：${pet.length}cm"
        tvWeight.text = "体重：${pet.weight}g"
        tvHealthScore.text = "最新分析：健康（${pet.healthScore}分）"
        tvLastCheckup.text = "上次体检：${pet.lastCheckup}"
    }

    private fun calculateAge(birthDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birth = format.parse(birthDate)
            val now = Date()
            val diff = now.time - birth.time
            val months = (diff / (1000L * 60 * 60 * 24 * 30)).toInt()
            "${months}个月"
        } catch (e: Exception) {
            "未知"
        }
    }

    private fun loadRecentRecords() {
        val records = dbHelper.getAllRecords()
        val recentRecords = if (records.size > 2) records.subList(0, 2) else records

        val adapter = RecentRecordsAdapter(recentRecords)
        recyclerView.adapter = adapter
    }

    private fun setupGrowthChart() {
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 100f))
        entries.add(Entry(1f, 110f))
        entries.add(Entry(2f, 118f))
        entries.add(Entry(3f, 125f))

        val dataSet = LineDataSet(entries, "体重趋势(g)")
        dataSet.color = android.graphics.Color.parseColor("#FF9800")
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(android.graphics.Color.parseColor("#FF9800"))

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }

    // 将 RecentRecordsAdapter 移到这里作为内部类
    inner class RecentRecordsAdapter(private val records: List<Record>) :
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
            holder.tvDate.text = record.date.substring(5) // 只显示月-日
            holder.tvType.text = record.type
            holder.tvDescription.text = record.description
        }

        override fun getItemCount() = records.size
    }
}