package com.example.koskita.model

data class Tagihan(
    val id           : String = "",
    val kamarId      : String = "",
    val nomorKamar   : String = "",
    val namaPenghuni : String = "",
    val bulan        : String = "",
    val tahun        : Int    = 0,
    val nominal      : Int    = 0,
    val status       : String = "Belum Bayar",
    val tanggalBayar : String = "",
    val metodeBayar  : String = "",
    val tanggalBuat  : String = ""
)