package com.example.koskita

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.TagihanAdapter
import com.example.koskita.data.FirebaseTagihanRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityBayarTagihanBinding
import com.example.koskita.databinding.DialogKonfirmasiBayarBinding
import com.example.koskita.model.Tagihan
import java.text.SimpleDateFormat
import java.util.*

class BayarTagihanActivity : BaseActivity() {

    private lateinit var binding  : ActivityBayarTagihanBinding
    private lateinit var userRepo : UserRepository
    private lateinit var adapter  : TagihanAdapter
    private var nomorKamar        = ""
    private var tagihanDipilih    : Tagihan? = null

    // Launcher untuk hasil dari QrisActivity
    private val qrisLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val tagihanId = result.data?.getStringExtra("tagihanId") ?: return@registerForActivityResult
            val metode    = result.data?.getStringExtra("metode") ?: "QRIS"
            konfirmasiPembayaran(tagihanId, metode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityBayarTagihanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)
        binding.btnBack.setOnClickListener { finish() }

        setupList()
        ambilDataUser()
    }

    private fun ambilDataUser() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                nomorKamar = user.nomorKamar
                binding.tvNamaUser.text   = user.nama
                binding.tvNomorKamar.text = "Kamar ${user.nomorKamar}"
                listenTagihan(user.nomorKamar)
            },
            onError = {
                val session = userRepo.getSession()
                nomorKamar = session?.nomorKamar ?: ""
                binding.tvNamaUser.text   = session?.nama ?: "Penghuni"
                binding.tvNomorKamar.text = "Kamar $nomorKamar"
                if (nomorKamar.isNotEmpty()) listenTagihan(nomorKamar)
            }
        )
    }

    private fun setupList() {
        adapter = TagihanAdapter(mutableListOf()) { tagihan ->
            if (tagihan.status == "Belum Bayar") {
                showDialogPilihMetode(tagihan)
            } else {
                Toast.makeText(this, "Tagihan ini sudah lunas ✅", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvTagihanSaya.apply {
            layoutManager = LinearLayoutManager(this@BayarTagihanActivity)
            adapter = this@BayarTagihanActivity.adapter
        }
    }

    private fun listenTagihan(kamar: String) {
        FirebaseTagihanRepository.listenByKamar(kamar) { list ->
            adapter.refresh(list.toMutableList())
            val belumBayar = list.count { it.status == "Belum Bayar" }
            val totalBelum = list.filter { it.status == "Belum Bayar" }.sumOf { it.nominal }

            if (belumBayar > 0) {
                binding.tvStatusTagihan.text   = "$belumBayar tagihan belum dibayar"
                binding.tvTotalBelumBayar.text = "Rp ${String.format("%,d", totalBelum).replace(',', '.')}"
                binding.cardInfoTagihan.setBackgroundColor(0xFFFCEBEB.toInt())
            } else {
                binding.tvStatusTagihan.text   = "Semua tagihan lunas ✅"
                binding.tvTotalBelumBayar.text = "Rp 0"
                binding.cardInfoTagihan.setBackgroundColor(0xFFEAF3DE.toInt())
            }
        }
    }

    private fun showDialogPilihMetode(tagihan: Tagihan) {
        tagihanDipilih = tagihan

        val dialog        = Dialog(this)
        val dialogBinding = DialogKonfirmasiBayarBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        dialogBinding.tvInfoTagihan.text =
            "Kamar ${tagihan.nomorKamar} — ${tagihan.bulan} ${tagihan.tahun}\n" +
                    "Nominal: Rp ${String.format("%,d", tagihan.nominal).replace(',', '.')}"

        dialogBinding.btnKonfirmasiDialog.text = "Lanjutkan Pembayaran"

        dialogBinding.btnKonfirmasiDialog.setOnClickListener {
            dialog.dismiss()
            val metode = when (dialogBinding.rgMetode.checkedRadioButtonId) {
                dialogBinding.rbQris.id  -> "QRIS"
                dialogBinding.rbTunai.id -> "Tunai"
                else                     -> "Transfer Bank"
            }

            when (metode) {
                "QRIS" -> {
                    // Buka halaman QRIS dengan QR otomatis
                    val intent = Intent(this, QrisActivity::class.java).apply {
                        putExtra("nominal",    tagihan.nominal)
                        putExtra("nomorKamar", tagihan.nomorKamar)
                        putExtra("bulan",      "${tagihan.bulan} ${tagihan.tahun}")
                        putExtra("tagihanId",  tagihan.id)
                    }
                    qrisLauncher.launch(intent)
                }
                else -> {
                    // Konfirmasi langsung untuk transfer/tunai
                    konfirmasiPembayaran(tagihan.id, metode)
                }
            }
        }
        dialog.show()
    }

    private fun konfirmasiPembayaran(tagihanId: String, metode: String) {
        FirebaseTagihanRepository.getAll(
            onSuccess = { list ->
                val tagihan = list.find { it.id == tagihanId } ?: return@getAll
                val tglBayar = SimpleDateFormat("d MMM yyyy", Locale("id")).format(Date())
                val updated  = tagihan.copy(
                    status       = "Sudah Bayar",
                    metodeBayar  = metode,
                    tanggalBayar = tglBayar
                )
                FirebaseTagihanRepository.update(
                    tagihan   = updated,
                    onSuccess = {
                        Toast.makeText(
                            this,
                            "✅ Pembayaran via $metode berhasil! Menunggu konfirmasi pemilik.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
                )
            },
            onError = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
        )
    }
}