package com.example.koskita

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityProfilBinding

class ProfilActivity : BaseActivity() {

    private lateinit var binding  : ActivityProfilBinding
    private lateinit var userRepo : UserRepository
    private lateinit var prefs    : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)
        prefs    = getSharedPreferences("koskita_settings", MODE_PRIVATE)

        setupDarkMode()
        setupMenu()
        loadProfilDariFirebase()
    }

    override fun onResume() {
        super.onResume()
        loadProfilDariFirebase()
    }

    private fun loadProfilDariFirebase() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                userRepo.saveSession(user)
                tampilkanProfil(user.nama, user.email, user.role)
            },
            onError = {
                val session = userRepo.getSession()
                if (session != null) {
                    tampilkanProfil(session.nama, session.email, session.role)
                }
            }
        )
    }

    private fun tampilkanProfil(nama: String, email: String, role: String) {
        binding.tvNamaProfil.text  = nama
        binding.tvEmailProfil.text = email
        binding.tvRoleBadge.text   = role

        if (role == "Pemilik") {
            binding.menuRiwayatBayar.visibility  = View.VISIBLE
            binding.menuTagihan.visibility       = View.VISIBLE
            binding.menuKelolaRequest.visibility = View.VISIBLE
            binding.menuKelolaAkun.visibility    = View.VISIBLE
        } else {
            binding.menuRiwayatBayar.visibility  = View.GONE
            binding.menuTagihan.visibility       = View.GONE
            binding.menuKelolaRequest.visibility = View.GONE
            binding.menuKelolaAkun.visibility    = View.GONE
        }
    }

    private fun setupDarkMode() {
        val isDark = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDark
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupMenu() {
        binding.menuEditProfil.setOnClickListener {
            startActivity(Intent(this, EditProfilActivity::class.java))
        }
        binding.menuRiwayatBayar.setOnClickListener {
            startActivity(Intent(this, KelolaKamarActivity::class.java))
        }
        binding.menuTagihan.setOnClickListener {
            startActivity(Intent(this, TagihanPemilikActivity::class.java))
        }
        binding.menuKelolaRequest.setOnClickListener {
            startActivity(Intent(this, KelolaRequestActivity::class.java))
        }
        binding.menuKelolaAkun.setOnClickListener {
            startActivity(Intent(this, HapusPenghuniActivity::class.java))
        }
        binding.menuBantuan.setOnClickListener {
            Toast.makeText(this, "Bantuan — coming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.menuKeluar.setOnClickListener {
            FirebaseUserRepository.logout()
            userRepo.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}