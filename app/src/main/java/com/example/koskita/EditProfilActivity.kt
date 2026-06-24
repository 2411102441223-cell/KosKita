package com.example.koskita

import android.os.Bundle
import android.widget.Toast
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityEditProfilBinding

class EditProfilActivity : BaseActivity() {

    private lateinit var binding  : ActivityEditProfilBinding
    private lateinit var userRepo : UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityEditProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        val user = userRepo.getSession()
        if (user == null) {
            Toast.makeText(this, "Session tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Isi form dengan data saat ini
        binding.etNama.setText(user.nama)
        binding.etEmail.setText(user.email)
        binding.etNomorKamar.setText(user.nomorKamar)
        binding.tvRoleBadge.text = user.role

        binding.btnSimpan.setOnClickListener {
            simpanPerubahan(user.id)
        }
    }

    private fun simpanPerubahan(userId: String) {
        val nama       = binding.etNama.text.toString().trim()
        val email      = binding.etEmail.text.toString().trim()
        val kamar      = binding.etNomorKamar.text.toString().trim()
        val passLama   = binding.etPasswordLama.text.toString().trim()
        val passBaru   = binding.etPasswordBaru.text.toString().trim()
        val konfirmasi = binding.etKonfirmasiPassword.text.toString().trim()

        // Validasi profil
        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan profil
        val profilBerhasil = userRepo.updateProfil(userId, nama, email, kamar)
        if (!profilBerhasil) {
            Toast.makeText(this, "❌ Email sudah digunakan akun lain!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ganti password jika diisi
        if (passLama.isNotEmpty() || passBaru.isNotEmpty()) {
            when {
                passLama.isEmpty() -> {
                    Toast.makeText(this, "Masukkan password lama!", Toast.LENGTH_SHORT).show()
                    return
                }
                passBaru.isEmpty() -> {
                    Toast.makeText(this, "Masukkan password baru!", Toast.LENGTH_SHORT).show()
                    return
                }
                passBaru.length < 6 -> {
                    Toast.makeText(this, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                    return
                }
                passBaru != konfirmasi -> {
                    Toast.makeText(this, "Konfirmasi password tidak sama!", Toast.LENGTH_SHORT).show()
                    return
                }
                else -> {
                    val passOk = userRepo.gantiPassword(userId, passLama, passBaru)
                    if (!passOk) {
                        Toast.makeText(this, "❌ Password lama salah!", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
        }

        Toast.makeText(this, "✅ Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
        finish()
    }
}