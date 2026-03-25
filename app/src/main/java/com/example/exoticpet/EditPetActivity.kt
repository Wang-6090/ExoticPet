package com.example.exoticpet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exoticpet.models.Pet

class EditPetActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabase
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

        dbHelper = PetDatabase(this)
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
        val pet = dbHelper.getPet()
        etName.setText(pet.name)
        etSpecies.setText(pet.species)
        etGender.setText(pet.gender)
        etBirthDate.setText(pet.birthDate)
        etLength.setText(pet.length.toString())
        etWeight.setText(pet.weight.toString())
        etSpecialMark.setText(pet.specialMark)
        etEnclosureSize.setText(pet.enclosureSize)
        etStapleFood.setText(pet.stapleFood)
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
        try {
            val pet = Pet(
                name = etName.text.toString(),
                species = etSpecies.text.toString(),
                gender = etGender.text.toString(),
                birthDate = etBirthDate.text.toString(),
                length = etLength.text.toString().toDouble(),
                weight = etWeight.text.toString().toDouble(),
                specialMark = etSpecialMark.text.toString(),
                enclosureSize = etEnclosureSize.text.toString(),
                stapleFood = etStapleFood.text.toString()
            )

            if (dbHelper.updatePet(pet)) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "输入格式错误", Toast.LENGTH_SHORT).show()
        }
    }
}