package com.example.koskita

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private lateinit var binding  : ActivityLoginBinding
    private lateinit var localRepo: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding   = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localRepo = UserRepository(this)

        // Cek Firebase session
        if (FirebaseUserRepository.isLoggedIn()) {
            showLoading(true)
            FirebaseUserRepository.getCurrentUser(
                onSuccess = { user ->
                    showLoading(false)
                    goToDashboard(user.nama, user.email, user.role)
                },
                onError = {
                    showLoading(false)
                }
            )
        }

        val slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        binding.layoutHeader.startAnimation(slideAnim)

        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            when {
                email.isEmpty()    -> Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                else -> {
                    showLoading(true)
                    FirebaseUserRepository.login(
                        email    = email,
                        password = password,
                        onSuccess = { user ->
                            showLoading(false)
                            goToDashboard(user.nama, user.email, user.role)
                        },
                        onError = { msg ->
                            showLoading(false)
                            Toast.makeText(this, "❌ $msg", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        binding.btnDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled  = !show
        binding.btnDaftar.isEnabled = !show
    }

    private fun goToDashboard(nama: String, email: String, role: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("nama",  nama)
        intent.putExtra("email", email)
        intent.putExtra("role",  role)
        startActivity(intent)
        finish()
    }
}