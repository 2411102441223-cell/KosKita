package com.example.koskita

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.koskita.data.FirebaseKamarRepository
import com.example.koskita.data.Kamar
import com.example.koskita.databinding.ActivityFormKamarBinding

class FormKamarActivity : BaseActivity() {

    private lateinit var binding      : ActivityFormKamarBinding
    private var kamarId               : String? = null
    private var statusDipilih         = "Kosong"
    private var kamarLama             : Kamar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kamarId = intent.getStringExtra("kamar_id")

        binding.btnBack.setOnClickListener { finish() }
        setupStatusToggle()

        kamarId?.let { id ->
            binding.tvJudulForm.text = "Edit Kamar"
            binding.btnSimpan.text   = "Update Kamar"
            // Load dari Firestore
            FirebaseKamarRepository.getAll(
                onSuccess = { list ->
                    val kamar = list.find { it.id == id }
                    kamar?.let {
                        kamarLama = it
                        isiForm(it)
                    }
                },
                onError = { Toast.makeText(this, "Gagal load: $it", Toast.LENGTH_SHORT).show() }
            )
        }

        binding.btnSimpan.setOnClickListener { simpan() }
    }

    private fun setupStatusToggle() {
        binding.statusKosong.setOnClickListener {
            statusDipilih = "Kosong"
            binding.statusKosong.setBackgroundColor(0xFF854F0B.toInt())
            binding.statusKosong.setTextColor(0xFFFFFFFF.toInt())
            binding.statusTerisi.setBackgroundColor(0xFFFFFFFF.toInt())
            binding.statusTerisi.setTextColor(0xFF2C2C2A.toInt())
            binding.layoutPenghuni.visibility = View.GONE
        }
        binding.statusTerisi.setOnClickListener {
            statusDipilih = "Terisi"
            binding.statusTerisi.setBackgroundColor(0xFF854F0B.toInt())
            binding.statusTerisi.setTextColor(0xFFFFFFFF.toInt())
            binding.statusKosong.setBackgroundColor(0xFFFFFFFF.toInt())
            binding.statusKosong.setTextColor(0xFF2C2C2A.toInt())
            binding.layoutPenghuni.visibility = View.VISIBLE
        }
    }

    private fun isiForm(kamar: Kamar) {
        binding.etNomorKamar.setText(kamar.nomorKamar)
        binding.etLantai.setText(kamar.lantai)
        binding.etHarga.setText(kamar.harga.toString())
        binding.etNamaPenghuni.setText(kamar.namaPenghuni)
        binding.etNoHp.setText(kamar.noHp)
        if (kamar.status == "Terisi") binding.statusTerisi.performClick()
        else binding.statusKosong.performClick()
    }

    private fun simpan() {
        val nomor  = binding.etNomorKamar.text.toString().trim()
        val lantai = binding.etLantai.text.toString().trim()
        val harga  = binding.etHarga.text.toString().trim()

        if (nomor.isEmpty() || lantai.isEmpty() || harga.isEmpty()) {
            Toast.makeText(this, "Nomor kamar, lantai, dan harga wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val kamar = Kamar(
            id           = kamarId ?: java.util.UUID.randomUUID().toString(),
            nomorKamar   = nomor,
            lantai       = lantai,
            harga        = harga.toIntOrNull() ?: 0,
            status       = statusDipilih,
            namaPenghuni = binding.etNamaPenghuni.text.toString().trim(),
            noHp         = binding.etNoHp.text.toString().trim()
        )

        binding.btnSimpan.isEnabled = false

        if (kamarId != null) {
            FirebaseKamarRepository.update(
                kamar     = kamar,
                onSuccess = {
                    Toast.makeText(this, "✅ Kamar diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = {
                    binding.btnSimpan.isEnabled = true
                    Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            FirebaseKamarRepository.tambah(
                kamar     = kamar,
                onSuccess = {
                    Toast.makeText(this, "✅ Kamar berhasil ditambah!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = {
                    binding.btnSimpan.isEnabled = true
                    Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}