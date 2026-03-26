package com.example.exoticpet

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exoticpet.api.RecordDto
import com.example.exoticpet.api.BackendRetrofitClient
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAll: Button
    private lateinit var btnFeed: Button
    private lateinit var btnCheckup: Button
    private lateinit var btnAbnormal: Button
    private lateinit var btnOther: Button

    private var currentFilter = "全部"
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        initViews(view)
        setupFilterButtons()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadRecords()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        btnAll = view.findViewById(R.id.btnAll)
        btnFeed = view.findViewById(R.id.btnFeed)
        btnCheckup = view.findViewById(R.id.btnCheckup)
        btnAbnormal = view.findViewById(R.id.btnAbnormal)
        btnOther = view.findViewById(R.id.btnOther)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun setupFilterButtons() {
        btnAll.setOnClickListener { updateFilter("全部", btnAll) }
        btnFeed.setOnClickListener { updateFilter("喂食", btnFeed) }
        btnCheckup.setOnClickListener { updateFilter("体检", btnCheckup) }
        btnAbnormal.setOnClickListener { updateFilter("异常", btnAbnormal) }
        btnOther.setOnClickListener { updateFilter("其他", btnOther) }
    }

    private fun updateFilter(filter: String, selectedButton: Button) {
        currentFilter = filter

        val grayColor = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        val orangeColor = ColorStateList.valueOf(Color.parseColor("#FF9800"))

        btnAll.backgroundTintList = grayColor
        btnFeed.backgroundTintList = grayColor
        btnCheckup.backgroundTintList = grayColor
        btnAbnormal.backgroundTintList = grayColor
        btnOther.backgroundTintList = grayColor

        selectedButton.backgroundTintList = orangeColor

        loadRecords()
    }

    private fun loadRecords() {
        val userId = UserSession.getUserId(requireContext())
        if (userId == 0) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = BackendRetrofitClient.instance.getRecords(userId, currentFilter)

                if (!response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "加载记录失败：${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val records = response.body() ?: emptyList()
                adapter = HistoryAdapter(records)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "加载记录失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

class HistoryAdapter(private val records: List<RecordDto>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvSuggestion: TextView = itemView.findViewById(R.id.tvSuggestion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        val dateText = record.date?.takeIf { it.isNotBlank() } ?: "未知日期"
        val typeText = record.type?.takeIf { it.isNotBlank() } ?: "未分类"
        val descriptionText = record.description?.takeIf { it.isNotBlank() } ?: "无"
        val suggestionText = record.suggestion?.takeIf { it.isNotBlank() } ?: "无"

        holder.tvDate.text = "📅 $dateText"
        holder.tvType.text = "• 类型：$typeText"
        holder.tvDescription.text = "• 描述：$descriptionText"
        holder.tvSuggestion.text = "• 建议：$suggestionText"
    }

    override fun getItemCount() = records.size
}