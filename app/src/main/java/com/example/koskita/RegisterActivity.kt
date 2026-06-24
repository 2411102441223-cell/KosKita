package com.example.koskita

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.databinding.ActivityRegisterBinding

class RegisterActivity : BaseActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private var roleDipilih      = "Penghuni"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRolePenghuni()

        binding.rolePenghuni.setOnClickListener { setRolePenghuni() }
        binding.rolePemilik.setOnClickListener  { setRolePemilik()  }
        binding.btnDaftar.setOnClickListener    { daftar()          }
        binding.tvMasuk.setOnClickListener      { finish()          }
    }

    private fun setRolePenghuni() {
        roleDipilih = "Penghuni"
        binding.rolePenghuni.setBackgroundColor(0xFF854F0B.toInt())
        binding.rolePemilik.setBackgroundColor(0xFFDDDDDD.toInt())
        binding.tvCheckPenghuni.text = "✓ Dipilih"
        binding.tvCheckPenghuni.setTextColor(0xFFFAC775.toInt())
        binding.tvCheckPemilik.text = "Tap untuk pilih"
        binding.tvCheckPemilik.setTextColor(0xFF888780.toInt())
        binding.layoutNomorKamar.visibility = View.VISIBLE
    }

    private fun setRolePemilik() {
        roleDipilih = "Pemilik"
        binding.rolePemilik.setBackgroundColor(0xFF854F0B.toInt())
        binding.rolePenghuni.setBackgroundColor(0xFFDDDDDD.toInt())
        binding.tvCheckPemilik.text = "✓ Dipilih"
        binding.tvCheckPemilik.setTextColor(0xFFFAC775.toInt())
        binding.tvCheckPenghuni.text = "Tap untuk pilih"
        binding.tvCheckPenghuni.setTextColor(0xFF888780.toInt())
        binding.layoutNomorKamar.visibility = View.GONE
    }

    private fun daftar() {
        val nama     = binding.etNama.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val konfirm  = binding.etKonfirmasiPassword.text.toString().trim()
        val kamar    = binding.etNomorKamar.text.toString().trim()

        when {
            nama.isEmpty()      -> { Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show(); return }
            email.isEmpty()     -> { Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show(); return }
            password.isEmpty()  -> { Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show(); return }
            password.length < 6 -> { Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show(); return }
            password != konfirm -> { Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show(); return }
            roleDipilih == "Penghuni" && kamar.isEmpty() -> {
                Toast.makeText(this, "Nomor kamar tidak boleh kosong!", Toast.LENGTH_SHORT).show(); return
            }
        }

        Toast.makeText(this, "Mendaftarkan sebagai $roleDipilih...", Toast.LENGTH_SHORT).show()

        binding.btnDaftar.isEnabled = false
        binding.btnDaftar.text      = "Mendaftarkan..."

        FirebaseUserRepository.register(
            nama       = nama,
            email      = email,
            password   = password,
            role       = roleDipilih,
            nomorKamar = kamar,
            onSuccess  = { user ->
                Toast.makeText(this, "✅ Akun ${user.role} berhasil dibuat!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("nama",  user.nama)
                intent.putExtra("email", user.email)
                intent.putExtra("role",  user.role)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onError = { msg ->
                binding.btnDaftar.isEnabled = true
                binding.btnDaftar.text      = "Daftar Sekarang"
                Toast.makeText(this, "❌ $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }
}