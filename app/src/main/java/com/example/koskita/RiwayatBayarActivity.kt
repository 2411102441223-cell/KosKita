package com.example.koskita

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.RiwayatBayarAdapter
import com.example.koskita.data.FirebaseTagihanRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityRiwayatBayarBinding
import com.example.koskita.model.RiwayatBayar
import com.example.koskita.model.Tagihan

class RiwayatBayarActivity : BaseActivity() {

    private lateinit var binding  : ActivityRiwayatBayarBinding
    private lateinit var userRepo : UserRepository
    private lateinit var adapter  : RiwayatBayarAdapter
    private var semuaData         = listOf<Tagihan>()
    private var filterAktif       = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityRiwayatBayarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        setupList()
        ambilDataUser()
    }

    private fun ambilDataUser() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                listenTagihan(user.nomorKamar)
            },
            onError = {
                val session = userRepo.getSession()
                val kamar   = session?.nomorKamar ?: ""
                if (kamar.isNotEmpty()) listenTagihan(kamar)
            }
        )
    }

    private fun listenTagihan(kamar: String) {
        FirebaseTagihanRepository.listenByKamar(kamar) { list ->
            // Hanya tampilkan yang sudah bayar di riwayat
            semuaData = list.filter { it.status == "Sudah Bayar" }
            setupFilter()
            tampilkan(semuaData)
        }
    }

    private fun setupList() {
        adapter = RiwayatBayarAdapter(mutableListOf())
        binding.rvRiwayatBayar.apply {
            layoutManager = LinearLayoutManager(this@RiwayatBayarActivity)
            adapter = this@RiwayatBayarActivity.adapter
        }
    }

    private fun setupFilter() {
        binding.layoutFilterBulan.removeAllViews()
        val tahunList = semuaData.map { it.tahun.toString() }.distinct().sorted()
        val filterList = mutableListOf("Semua") + tahunList

        filterList.forEach { label ->
            val tv = TextView(this).apply {
                text      = label
                textSize  = 12f
                setPadding(32, 16, 32, 16)
                gravity   = Gravity.CENTER
                setTextColor(if (label == filterAktif) 0xFFFFFFFF.toInt() else 0xFF854F0B.toInt())
                setBackgroundColor(if (label == filterAktif) 0xFF854F0B.toInt() else 0xFFFAEEDA.toInt())
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = 8
                layoutParams = lp
                setOnClickListener {
                    filterAktif = label
                    setupFilter()
                    val filtered = if (label == "Semua") semuaData
                    else semuaData.filter { it.tahun.toString() == label }
                    tampilkan(filtered)
                }
            }
            binding.layoutFilterBulan.addView(tv)
        }
    }

    private fun tampilkan(list: List<Tagihan>) {
        val riwayatList = list.map { t ->
            val emoji = when (t.metodeBayar) {
                "QRIS"  -> "📱"
                "Tunai" -> "💵"
                else    -> "🏦"
            }
            RiwayatBayar(
                bulan       = "${t.bulan} ${t.tahun}",
                tanggalBayar= t.tanggalBayar,
                nominal     = "Rp ${String.format("%,d", t.nominal).replace(',', '.')}",
                metode      = t.metodeBayar,
                emojiMetode = emoji,
                status      = "Lunas"
            )
        }
        adapter.refresh(riwayatList.toMutableList())

        val total = list.sumOf { it.nominal }
        binding.tvTotalDibayar.text = "Rp ${String.format("%,d", total).replace(',', '.')}"
        binding.tvBulanLunas.text   = "${list.size} Bulan"
    }
}