package com.example.exoticpet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.exoticpet.api.PetProfileRequest
import com.example.exoticpet.api.BackendRetrofitClient
import kotlinx.coroutines.launch

class EditPetActivity : AppCompatActivity() {

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

        initViews()
        loadPetData()
        setupClickListeners()
    }

    private fun initViews() {
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
    }

    private fun loadPetData() {
        val userId = UserSession.getUserId(this)
        lifecycleScope.launch {
            try {
                val response = BackendRetrofitClient.instance.getMyPet(userId)
                val pet = response.body() ?: return@launch

                etName.setText(pet.name)
                etSpecies.setText(pet.species)
                etGender.setText(pet.gender ?: "")
                etBirthDate.setText(pet.birthDate ?: "")
                etLength.setText((pet.length ?: 0.0).toString())
                etWeight.setText((pet.weight ?: 0.0).toString())
                etSpecialMark.setText(pet.specialMark ?: "")
                etEnclosureSize.setText(pet.enclosureSize ?: "")
                etStapleFood.setText(pet.stapleFood ?: "")
            } catch (e: Exception) {
                Toast.makeText(this@EditPetActivity, "加载宠物信息失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInputs()) {
                savePetData()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        if (etName.text.isNullOrBlank()) {
            etName.error = "请输入昵称"
            return false
        }
        if (etSpecies.text.isNullOrBlank()) {
            etSpecies.error = "请输入品种"
            return false
        }
        if (etLength.text.isNullOrBlank()) {
            etLength.error = "请输入体长"
            return false
        }
        if (etWeight.text.isNullOrBlank()) {
            etWeight.error = "请输入体重"
            return false
        }
        return true
    }

    private fun savePetData() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@EditPetActivity)
                val response = BackendRetrofitClient.instance.saveMyPet(
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
                        lastCheckup = "手动更新资料"
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditPetActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditPetActivity, response.body()?.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditPetActivity, "输入格式错误或网络异常：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}