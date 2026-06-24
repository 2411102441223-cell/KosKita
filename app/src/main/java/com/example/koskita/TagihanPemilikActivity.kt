package com.example.koskita

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.TagihanAdapter
import com.example.koskita.data.FirebaseKamarRepository
import com.example.koskita.data.FirebaseNotifikasiRepository
import com.example.koskita.data.FirebaseTagihanRepository
import com.example.koskita.data.Kamar
import com.example.koskita.databinding.ActivityTagihanPemilikBinding
import com.example.koskita.databinding.DialogKonfirmasiBayarBinding
import com.example.koskita.model.Tagihan
import com.example.koskita.utils.LoadingDialog
import com.example.koskita.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

class TagihanPemilikActivity : BaseActivity() {

    private lateinit var binding    : ActivityTagihanPemilikBinding
    private lateinit var adapter    : TagihanAdapter
    private var isFirstLoad         = true
    private var daftarKamarTerisi   = listOf<Kamar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagihanPemilikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGenerate.setOnClickListener { showPilihPenghuniDialog() }
        binding.btnGenerate.setOnLongClickListener { eksporPDF(); true }

        setupList()
        LoadingDialog.show(this, "Memuat tagihan...")
        listenData()
        loadDaftarKamar()
    }

    private fun loadDaftarKamar() {
        FirebaseKamarRepository.getAll(
            onSuccess = { list ->
                daftarKamarTerisi = list.filter { it.status == "Terisi" }
            },
            onError = {}
        )
    }

    private fun showPilihPenghuniDialog() {
        if (daftarKamarTerisi.isEmpty()) {
            Toast.makeText(this, "Tidak ada kamar terisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat list pilihan: "Semua Penghuni" + nama per penghuni
        val pilihanList = mutableListOf("⚡ Semua Penghuni Sekaligus")
        daftarKamarTerisi.forEach { kamar ->
            pilihanList.add("🏠 ${kamar.nomorKamar} — ${kamar.namaPenghuni}")
        }

        var dipilihIndex = 0

        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Penghuni yang Ditagih")
            .setSingleChoiceItems(pilihanList.toTypedArray(), 0) { _, which ->
                dipilihIndex = which
            }
            .setPositiveButton("Generate") { _, _ ->
                if (dipilihIndex == 0) {
                    // Generate semua
                    generateTagihanUntuk(daftarKamarTerisi)
                } else {
                    // Generate untuk 1 penghuni
                    val kamarDipilih = daftarKamarTerisi[dipilihIndex - 1]
                    generateTagihanUntuk(listOf(kamarDipilih))
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun generateTagihanUntuk(kamarList: List<Kamar>) {
        val cal     = Calendar.getInstance()
        val bulan   = SimpleDateFormat("MMMM", Locale("id")).format(cal.time)
        val tahun   = cal.get(Calendar.YEAR)
        val tglBuat = SimpleDateFormat("d MMM yyyy", Locale("id")).format(Date())

        // Ambil semua tagihan dulu, baru cek duplikat
        FirebaseTagihanRepository.getAll(
            onSuccess = { semuaTagihan ->
                var jumlahBaru  = 0
                var jumlahSkip  = 0

                kamarList.forEach { kamar ->
                    // Cek apakah sudah ada tagihan bulan ini untuk kamar ini
                    val sudahAda = semuaTagihan.any {
                        it.nomorKamar == kamar.nomorKamar &&
                                it.bulan      == bulan &&
                                it.tahun      == tahun
                    }

                    if (sudahAda) {
                        jumlahSkip++
                    } else {
                        val tagihan = Tagihan(
                            id           = UUID.randomUUID().toString(),
                            kamarId      = kamar.id,
                            nomorKamar   = kamar.nomorKamar,
                            namaPenghuni = kamar.namaPenghuni,
                            bulan        = bulan,
                            tahun        = tahun,
                            nominal      = kamar.harga,
                            status       = "Belum Bayar",
                            tanggalBuat  = tglBuat
                        )
                        FirebaseTagihanRepository.tambah(
                            tagihan   = tagihan,
                            onSuccess = {
                                jumlahBaru++
                                FirebaseNotifikasiRepository.cariUserLaluKirimTagihanBaru(
                                    namaPenghuni = kamar.namaPenghuni,
                                    nomorKamar   = kamar.nomorKamar,
                                    bulan        = bulan,
                                    nominal      = "Rp ${String.format("%,d", kamar.harga).replace(',', '.')}"
                                )
                            },
                            onError = {}
                        )
                    }
                }

                // Tampilkan hasil
                val pesanNama = if (kamarList.size == 1)
                    kamarList[0].namaPenghuni
                else
                    "${kamarList.size} penghuni"

                if (jumlahSkip > 0 && jumlahBaru == 0) {
                    Toast.makeText(
                        this,
                        "⚠️ $pesanNama sudah punya tagihan bulan $bulan!",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (jumlahBaru > 0) {
                    Toast.makeText(
                        this,
                        "✅ Tagihan bulan $bulan berhasil dikirim ke $pesanNama!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onError = {
                Toast.makeText(this, "❌ Gagal cek tagihan: $it", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun setupList() {
        adapter = TagihanAdapter(mutableListOf()) { tagihan ->
            showDialogKonfirmasi(tagihan)
        }
        binding.rvTagihan.apply {
            layoutManager = LinearLayoutManager(this@TagihanPemilikActivity)
            adapter = this@TagihanPemilikActivity.adapter
        }
    }

    private fun listenData() {
        FirebaseTagihanRepository.listenAll { list ->
            if (isFirstLoad) {
                LoadingDialog.dismiss()
                isFirstLoad = false
            }
            adapter.refresh(list.toMutableList())
            binding.tvTotalTagihan.text = list.size.toString()
            binding.tvBelumBayar.text   = list.count { it.status == "Belum Bayar" }.toString()
            binding.tvSudahBayar.text   = list.count { it.status == "Sudah Bayar" }.toString()

            if (list.isEmpty()) {
                binding.tvEmptyTagihan.visibility = View.VISIBLE
                binding.rvTagihan.visibility      = View.GONE
            } else {
                binding.tvEmptyTagihan.visibility = View.GONE
                binding.rvTagihan.visibility      = View.VISIBLE
            }
        }
    }

    private fun eksporPDF() {
        FirebaseTagihanRepository.getAll(
            onSuccess = { list ->
                if (list.isEmpty()) {
                    Toast.makeText(this, "Belum ada data tagihan!", Toast.LENGTH_SHORT).show()
                    return@getAll
                }
                Toast.makeText(this, "⏳ Membuat PDF...", Toast.LENGTH_SHORT).show()
                val file = PdfGenerator.generateTagihanPDF(this, list)
                if (file != null) {
                    Toast.makeText(this, "✅ PDF berhasil dibuat!", Toast.LENGTH_LONG).show()
                    val uri = FileProvider.getUriForFile(
                        this, "${packageName}.provider", file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        startActivity(Intent.createChooser(intent, "Buka PDF dengan..."))
                    } catch (e: Exception) {
                        Toast.makeText(this, "PDF di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "❌ Gagal membuat PDF", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun showDialogKonfirmasi(tagihan: Tagihan) {
        val dialog        = Dialog(this)
        val dialogBinding = DialogKonfirmasiBayarBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        dialogBinding.tvInfoTagihan.text =
            "Kamar ${tagihan.nomorKamar} — ${tagihan.bulan} ${tagihan.tahun}\n" +
                    "Penghuni: ${tagihan.namaPenghuni}\n" +
                    "Nominal: Rp ${String.format("%,d", tagihan.nominal).replace(',', '.')}"

        dialogBinding.btnKonfirmasiDialog.setOnClickListener {
            val metode = when (dialogBinding.rgMetode.checkedRadioButtonId) {
                dialogBinding.rbQris.id  -> "QRIS"
                dialogBinding.rbTunai.id -> "Tunai"
                else                     -> "Transfer Bank"
            }
            val tglBayar = SimpleDateFormat("d MMM yyyy", Locale("id")).format(Date())
            val updated  = tagihan.copy(
                status       = "Sudah Bayar",
                metodeBayar  = metode,
                tanggalBayar = tglBayar
            )
            FirebaseTagihanRepository.update(
                tagihan   = updated,
                onSuccess = {
                    Toast.makeText(this, "✅ Pembayaran dikonfirmasi!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    FirebaseNotifikasiRepository.cariUserLaluKirimTagihanLunas(
                        namaPenghuni = tagihan.namaPenghuni,
                        nomorKamar   = tagihan.nomorKamar,
                        bulan        = "${tagihan.bulan} ${tagihan.tahun}"
                    )
                },
                onError = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
            )
        }
        dialog.show()
    }
}