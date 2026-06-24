package com.example.koskita

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import com.example.koskita.databinding.ActivityQrisBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class QrisActivity : BaseActivity() {

    private lateinit var binding: ActivityQrisBinding
    private var tagihanId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nominal    = intent.getIntExtra("nominal", 0)
        val nomorKamar = intent.getStringExtra("nomorKamar") ?: ""
        val bulan      = intent.getStringExtra("bulan") ?: ""
        tagihanId      = intent.getStringExtra("tagihanId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        tampilkanInfo(nominal, nomorKamar, bulan)
        generateQrLokal(nominal, nomorKamar, bulan)

        binding.btnSudahBayar.setOnClickListener {
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("tagihanId", tagihanId)
            resultIntent.putExtra("metode", "QRIS")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun tampilkanInfo(nominal: Int, nomorKamar: String, bulan: String) {
        binding.tvNominalQris.text = "Rp ${String.format("%,d", nominal).replace(',', '.')}"
        binding.tvInfoQris.text    = "Pembayaran Kamar $nomorKamar — $bulan"
        binding.tvPetunjuk.text    = "Scan QR menggunakan m-banking\natau dompet digital kamu"
    }

    private fun generateQrLokal(nominal: Int, nomorKamar: String, bulan: String) {
        try {
            val isiQr = "KOSKITA-PAY|Kamar:$nomorKamar|Bulan:$bulan|Nominal:$nominal|ID:$tagihanId"

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                isiQr,
                BarcodeFormat.QR_CODE,
                400,
                400
            )

            val width  = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            binding.imgQris.setImageBitmap(bmp)

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat QR: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}