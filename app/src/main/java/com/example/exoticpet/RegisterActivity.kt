package com.example.exoticpet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.exoticpet.api.RegisterRequest
import com.example.exoticpet.api.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etRegisterUsername: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)

        btnRegister.setOnClickListener {
            register()
        }

        tvGoLogin.setOnClickListener {
            finish()
        }
    }

    private fun register() {
        val username = etRegisterUsername.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "请完整填写注册信息", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnRegister.isEnabled = false
                val response = RetrofitClient.instance.register(RegisterRequest(username, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@RegisterActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        response.body()?.message ?: "注册失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "网络错误：${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnRegister.isEnabled = true
            }
        }
    }
}