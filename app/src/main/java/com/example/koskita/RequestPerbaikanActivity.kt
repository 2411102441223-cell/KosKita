package com.example.koskita

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.RequestAdapter
import com.example.koskita.data.FirebaseRequestRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityRequestPerbaikanBinding
import com.example.koskita.model.RequestItem
import java.text.SimpleDateFormat
import java.util.*

class RequestPerbaikanActivity : BaseActivity() {

    private lateinit var binding        : ActivityRequestPerbaikanBinding
    private lateinit var adapter        : RequestAdapter
    private lateinit var userRepo       : UserRepository
    private var kategoriDipilih         = "Listrik"
    private var namaUser                = ""
    private var nomorKamarUser          = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityRequestPerbaikanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        setupKategori()

        binding.btnUploadFoto.setOnClickListener {
            Toast.makeText(this, "Kamera — coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnKirimRequest.setOnClickListener { kirimRequest() }

        // Ambil data user dari Firebase dulu baru setup riwayat
        ambilDataUserLaluSetup()
    }

    private fun ambilDataUserLaluSetup() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                namaUser       = user.nama
                nomorKamarUser = user.nomorKamar
                setupRiwayat()
            },
            onError = {
                // Fallback ke session lokal
                val session    = userRepo.getSession()
                namaUser       = session?.nama       ?: ""
                nomorKamarUser = session?.nomorKamar ?: ""
                setupRiwayat()
            }
        )
    }

    private fun setupKategori() {
        val kategoriMap = mapOf(
            binding.kategoriListrik to Pair("⚡", "Listrik"),
            binding.kategoriAir     to Pair("💧", "Air"),
            binding.kategoriPintu   to Pair("🚪", "Pintu"),
            binding.kategoriLainnya to Pair("🔨", "Lainnya")
        )
        kategoriMap.forEach { (tv, pair) ->
            tv.setOnClickListener {
                kategoriMap.keys.forEach { k ->
                    k.setBackgroundColor(0xFFFFFFFF.toInt())
                    k.setTextColor(0xFF2C2C2A.toInt())
                }
                tv.setBackgroundColor(0xFF854F0B.toInt())
                tv.setTextColor(0xFFFFFFFF.toInt())
                kategoriDipilih = pair.second
            }
        }
    }

    private fun setupRiwayat() {
        adapter = RequestAdapter(mutableListOf())
        binding.rvRiwayatRequest.apply {
            layoutManager = LinearLayoutManager(this@RequestPerbaikanActivity)
            adapter       = this@RequestPerbaikanActivity.adapter
            isNestedScrollingEnabled = false
        }

        // Tampilkan request milik penghuni yang login saja
        if (namaUser.isNotEmpty()) {
            FirebaseRequestRepository.listenByUser(namaUser) { list ->
                adapter.refresh(list)
            }
        } else {
            FirebaseRequestRepository.listenAll { list ->
                adapter.refresh(list)
            }
        }
    }

    private fun kirimRequest() {
        val desk = binding.etDeskripsi.text.toString().trim()
        if (desk.isEmpty()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        if (namaUser.isEmpty()) {
            Toast.makeText(this, "Data user belum siap, coba lagi!", Toast.LENGTH_SHORT).show()
            return
        }

        val emoji = when (kategoriDipilih) {
            "Air"     -> "💧"
            "Pintu"   -> "🚪"
            "Lainnya" -> "🔨"
            else      -> "⚡"
        }
        val tgl = SimpleDateFormat("d MMM yyyy", Locale("id")).format(Date())

        val request = RequestItem(
            id           = UUID.randomUUID().toString(),
            emoji        = emoji,
            kategori     = kategoriDipilih,
            deskripsi    = desk,
            tanggal      = tgl,
            status       = "Menunggu",
            namaPenghuni = namaUser,
            nomorKamar   = nomorKamarUser
        )

        binding.btnKirimRequest.isEnabled = false
        binding.btnKirimRequest.text      = "Mengirim..."

        FirebaseRequestRepository.tambah(
            item      = request,
            onSuccess = {
                binding.btnKirimRequest.isEnabled = true
                binding.btnKirimRequest.text      = "Kirim Request"
                binding.etDeskripsi.text?.clear()
                Toast.makeText(
                    this,
                    "✅ Request berhasil dikirim sebagai $namaUser!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { msg ->
                binding.btnKirimRequest.isEnabled = true
                binding.btnKirimRequest.text      = "Kirim Request"
                Toast.makeText(this, "❌ $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }
}