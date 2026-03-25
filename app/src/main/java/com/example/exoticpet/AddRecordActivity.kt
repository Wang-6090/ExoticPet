package com.example.exoticpet

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.exoticpet.models.Record
import java.text.SimpleDateFormat
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabase
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbFeed: RadioButton
    private lateinit var rbCheckup: RadioButton
    private lateinit var rbAbnormal: RadioButton
    private lateinit var rbOther: RadioButton
    private lateinit var tvDateTime: TextView
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        dbHelper = PetDatabase(this)
        initViews()
        setCurrentDateTime()
        setupClickListeners()
    }

    private fun initViews() {
        radioGroup = findViewById(R.id.radioGroup)
        rbFeed = findViewById(R.id.rbFeed)
        rbCheckup = findViewById(R.id.rbCheckup)
        rbAbnormal = findViewById(R.id.rbAbnormal)
        rbOther = findViewById(R.id.rbOther)
        tvDateTime = findViewById(R.id.tvDateTime)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)

        tvDateTime.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun setCurrentDateTime() {
        val now = Date()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        tvDateTime.text = format.format(now)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = android.app.DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val timePicker = android.app.TimePickerDialog(this,
                    { _, hourOfDay, minute ->
                        val dateStr = String.format("%04d-%02d-%02d %02d:%02d",
                            year, month + 1, dayOfMonth, hourOfDay, minute)
                        tvDateTime.text = dateStr
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun saveRecord() {
        val selectedType = when (radioGroup.checkedRadioButtonId) {
            R.id.rbFeed -> "喂食"
            R.id.rbCheckup -> "体检"
            R.id.rbAbnormal -> "异常"
            R.id.rbOther -> "其他"
            else -> "喂食"
        }

        val dateTime = tvDateTime.text.toString()
        val date = dateTime.substring(0, 10)
        val time = dateTime.substring(11)

        val description = etDescription.text.toString()

        if (description.isBlank()) {
            Toast.makeText(this, "请输入描述", Toast.LENGTH_SHORT).show()
            return
        }

        val record = Record(
            date = date,
            time = time,
            type = selectedType,
            description = description,
            suggestion = ""
        )

        val result = dbHelper.addRecord(record)
        if (result > 0) {
            Toast.makeText(this, "记录保存成功", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
        }
    }
}