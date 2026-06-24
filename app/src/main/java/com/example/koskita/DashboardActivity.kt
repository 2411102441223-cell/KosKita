package com.example.koskita

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.InfoAdapter
import com.example.koskita.data.FirebaseNotifikasiRepository
import com.example.koskita.data.FirebaseTagihanRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityDashboardBinding
import com.example.koskita.model.InfoKos
import com.example.koskita.model.TipeInfo
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : BaseActivity() {

    private lateinit var binding   : ActivityDashboardBinding
    private lateinit var userRepo  : UserRepository
    private var namaUser  = ""
    private var emailUser = ""
    private var roleUser  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        namaUser  = intent.getStringExtra("nama")  ?: ""
        emailUser = intent.getStringExtra("email") ?: ""
        roleUser  = intent.getStringExtra("role")  ?: ""

        setupInfoList()
        setupMenuClick()
        setupBottomNav()

        // Selalu ambil data terbaru dari Firebase
        ambilDataUserDariFirebase()
    }

    override fun onResume() {
        super.onResume()
        updateNotifBadge()
    }

    private fun ambilDataUserDariFirebase() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                // Update semua data dari Firebase
                namaUser  = user.nama
                emailUser = user.email
                roleUser  = user.role
                userRepo.saveSession(user)

                // Update nama di Dashboard
                binding.tvNamaPenghuni.text = namaUser

                // Ambil tagihan terbaru berdasarkan nomor kamar
                if (user.nomorKamar.isNotEmpty()) {
                    ambilTagihanPenghuni(user.nomorKamar)
                } else {
                    binding.tvNominalTagihan.text = "Rp 0"
                    binding.tvInfoKamar.text      = "Belum ada kamar terdaftar"
                }
            },
            onError = {
                val session = userRepo.getSession()
                namaUser  = session?.nama  ?: "Penghuni"
                emailUser = session?.email ?: ""
                roleUser  = session?.role  ?: "Penghuni"
                binding.tvNamaPenghuni.text = namaUser
            }
        )
    }

    private fun ambilTagihanPenghuni(nomorKamar: String) {
        FirebaseTagihanRepository.listenByKamar(nomorKamar) { list ->
            val belumBayar = list.filter { it.status == "Belum Bayar" }
            if (belumBayar.isNotEmpty()) {
                val tagihan = belumBayar.first()
                binding.tvNominalTagihan.text = "Rp ${String.format("%,d", tagihan.nominal).replace(',', '.')}"
                binding.tvInfoKamar.text      = "Kamar $nomorKamar  •  Jatuh tempo 5 ${tagihan.bulan} ${tagihan.tahun}"
            } else if (list.isNotEmpty()) {
                val tagihan = list.first()
                binding.tvNominalTagihan.text = "Rp ${String.format("%,d", tagihan.nominal).replace(',', '.')}"
                binding.tvInfoKamar.text      = "Kamar $nomorKamar  •  ✅ Semua tagihan lunas"
            } else {
                binding.tvNominalTagihan.text = "Rp 0"
                binding.tvInfoKamar.text      = "Kamar $nomorKamar  •  Belum ada tagihan"
            }
        }
    }

    private fun updateNotifBadge() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseNotifikasiRepository.getBelumDibacaCount(uid) { count ->
                val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_notifikasi)
                if (count > 0) {
                    badge.isVisible = true
                    badge.number    = count
                } else {
                    badge.isVisible = false
                }
            }
        }
    }

    private fun setupInfoList() {
        val list = mutableListOf(
            InfoKos("Air mati sementara",   "Perbaikan pompa pukul 10-12 WIB", "Hari ini",    TipeInfo.PERINGATAN),
            InfoKos("Pembayaran berhasil",   "April 2026 terkonfirmasi",         "2 hari lalu", TipeInfo.SUKSES),
            InfoKos("Tagihan Mei sudah ada", "Segera lakukan pembayaran",        "3 hari lalu", TipeInfo.INFO),
        )
        binding.rvInfo.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = InfoAdapter(list)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupMenuClick() {
        binding.menuBayarTagihan.setOnClickListener {
            startActivity(Intent(this, BayarTagihanActivity::class.java))
        }
        binding.menuRequest.setOnClickListener {
            startActivity(Intent(this, RequestPerbaikanActivity::class.java))
        }
        binding.menuPengumuman.setOnClickListener {
            startActivity(Intent(this, PengumumanActivity::class.java))
        }
        binding.menuRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatBayarActivity::class.java))
        }
        binding.tvLihatSemua.setOnClickListener {
            startActivity(Intent(this, PengumumanActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda    -> true
                R.id.nav_tagihan    -> {
                    startActivity(Intent(this, BayarTagihanActivity::class.java)); true
                }
                R.id.nav_notifikasi -> {
                    startActivity(Intent(this, NotifikasiActivity::class.java)); true
                }
                R.id.nav_profil     -> {
                    startActivity(Intent(this, ProfilActivity::class.java)); true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_beranda
    }
}