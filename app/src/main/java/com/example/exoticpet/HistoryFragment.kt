package com.example.exoticpet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exoticpet.models.Record

class HistoryFragment : Fragment() {

    private lateinit var dbHelper: PetDatabase
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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        dbHelper = PetDatabase(requireContext())
        initViews(view)
        setupFilterButtons()
        loadRecords()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        btnAll = view.findViewById(R.id.btnAll)
        btnFeed = view.findViewById(R.id.btnFeed)
        btnCheckup = view.findViewById(R.id.btnCheckup)
        btnAbnormal = view.findViewById(R.id.btnAbnormal)
        btnOther = view.findViewById(R.id.btnOther)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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

        val grayColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#9E9E9E")
        )
        val orangeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#FF9800")
        )

        btnAll.backgroundTintList = grayColor
        btnFeed.backgroundTintList = grayColor
        btnCheckup.backgroundTintList = grayColor
        btnAbnormal.backgroundTintList = grayColor
        btnOther.backgroundTintList = grayColor

        selectedButton.backgroundTintList = orangeColor

        loadRecords()
    }

    private fun loadRecords() {
        val records = dbHelper.getRecordsByType(currentFilter)
        adapter = HistoryAdapter(records)
        recyclerView.adapter = adapter
    }
}

class HistoryAdapter(private val records: List<Record>) :
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
        holder.tvDate.text = "📅 ${record.date}"
        holder.tvType.text = "• 类型：${record.type}"
        holder.tvDescription.text = "• 描述：${record.description}"
        holder.tvSuggestion.text = "• 建议：${record.suggestion.ifEmpty { "无" }}"
    }

    override fun getItemCount() = records.size
}