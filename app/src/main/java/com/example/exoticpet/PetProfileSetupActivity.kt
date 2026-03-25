package com.example.exoticpet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.exoticpet.api.PetProfileRequest
import com.example.exoticpet.api.RetrofitClient
import kotlinx.coroutines.launch

class PetProfileSetupActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var etGender: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etLength: EditText
    private lateinit var etWeight: EditText
    private lateinit var etSpecialMark: EditText
    private lateinit var etEnclosureSize: EditText
    private lateinit var etStapleFood: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pet)

        etName = findViewById(R.id.etName)
        etSpecies = findViewById(R.id.etSpecies)
        etGender = findViewById(R.id.etGender)
        etBirthDate = findViewById(R.id.etBirthDate)
        etLength = findViewById(R.id.etLength)
        etWeight = findViewById(R.id.etWeight)
        etSpecialMark = findViewById(R.id.etSpecialMark)
        etEnclosureSize = findViewById(R.id.etEnclosureSize)
        etStapleFood = findViewById(R.id.etStapleFood)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        btnSave.text = "完成问卷并进入首页"
        btnCancel.text = "稍后填写"

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun saveProfile() {
        val userId = UserSession.getUserId(this)
        if (userId == 0) {
            Toast.makeText(this, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show()
            return
        }

        if (etName.text.isNullOrBlank() || etSpecies.text.isNullOrBlank()) {
            Toast.makeText(this, "请至少填写宠物昵称和品种", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.saveMyPet(
                    PetProfileRequest(
                        userId = userId,
                        name = etName.text.toString().trim(),
                        species = etSpecies.text.toString().trim(),
                        gender = etGender.text.toString().trim(),
                        birthDate = etBirthDate.text.toString().trim(),
                        length = etLength.text.toString().toDoubleOrNull() ?: 0.0,
                        weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0,
                        specialMark = etSpecialMark.text.toString().trim(),
                        enclosureSize = etEnclosureSize.text.toString().trim(),
                        stapleFood = etStapleFood.text.toString().trim(),
                        healthScore = 0,
                        lastCheckup = "首次建档"
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@PetProfileSetupActivity, "宠物档案已创建", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PetProfileSetupActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@PetProfileSetupActivity, response.body()?.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PetProfileSetupActivity, "网络错误：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}