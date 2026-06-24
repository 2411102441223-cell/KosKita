package com.example.koskita

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.InfoAdapter
import com.example.koskita.data.FirebasePengumumanRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityPengumumanBinding
import com.example.koskita.model.InfoKos
import com.example.koskita.model.TipeInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class PengumumanActivity : BaseActivity() {

    private lateinit var binding  : ActivityPengumumanBinding
    private lateinit var userRepo : UserRepository
    private lateinit var adapter  : InfoAdapter
    private var isPemilik         = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityPengumumanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        cekRoleDanSetup()
        setupList()
        listenData()
    }

    private fun cekRoleDanSetup() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                isPemilik = user.role == "Pemilik"
                binding.btnTambahPengumuman.visibility =
                    if (isPemilik) View.VISIBLE else View.GONE
            },
            onError = {
                val session = userRepo.getSession()
                isPemilik = session?.role == "Pemilik"
                binding.btnTambahPengumuman.visibility =
                    if (isPemilik) View.VISIBLE else View.GONE
            }
        )

        binding.btnTambahPengumuman.setOnClickListener {
            showDialogTambah()
        }
    }

    private fun setupList() {
        adapter = InfoAdapter(mutableListOf())
        binding.rvPengumuman.apply {
            layoutManager = LinearLayoutManager(this@PengumumanActivity)
            adapter = this@PengumumanActivity.adapter
        }
    }

    private fun listenData() {
        FirebasePengumumanRepository.listenAll { list ->
            val infoList = list.map { p ->
                val tipe = when (p.tipe) {
                    "PERINGATAN" -> TipeInfo.PERINGATAN
                    "SUKSES"     -> TipeInfo.SUKSES
                    else         -> TipeInfo.INFO
                }
                InfoKos(
                    judul = p.judul,
                    sub   = p.isi,
                    waktu = p.waktu,
                    tipe  = tipe
                )
            }
            adapter.refresh(infoList.toMutableList())
        }
    }

    private fun showDialogTambah() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }

        val etJudul = com.google.android.material.textfield.TextInputEditText(this)
        val layoutJudul = com.google.android.material.textfield.TextInputLayout(this).apply {
            hint = "Judul Pengumuman"
            addView(etJudul)
        }

        val etIsi = com.google.android.material.textfield.TextInputEditText(this)
        val layoutIsi = com.google.android.material.textfield.TextInputLayout(this).apply {
            hint = "Isi Pengumuman"
            addView(etIsi)
        }

        layout.addView(layoutJudul)
        layout.addView(layoutIsi)

        AlertDialog.Builder(this)
            .setTitle("Tambah Pengumuman")
            .setView(layout)
            .setPositiveButton("Kirim") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val isi   = etIsi.text.toString().trim()
                if (judul.isEmpty() || isi.isEmpty()) {
                    Toast.makeText(this, "Judul dan isi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val waktu = SimpleDateFormat("d MMM yyyy", Locale("id")).format(Date())
                val pengumuman = FirebasePengumumanRepository.Pengumuman(
                    id    = UUID.randomUUID().toString(),
                    judul = judul,
                    isi   = isi,
                    waktu = waktu,
                    tipe  = "INFO"
                )
                FirebasePengumumanRepository.tambah(
                    pengumuman = pengumuman,
                    onSuccess  = { Toast.makeText(this, "✅ Pengumuman terkirim!", Toast.LENGTH_SHORT).show() },
                    onError    = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}