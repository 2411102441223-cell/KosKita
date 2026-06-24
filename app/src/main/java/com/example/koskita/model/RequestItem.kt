package com.example.koskita.model

data class RequestItem(
    val id           : String = "",
    val emoji        : String = "",
    val kategori     : String = "",
    val deskripsi    : String = "",
    val tanggal      : String = "",
    val status       : String = "Menunggu",
    val kamarId      : String = "",
    val nomorKamar   : String = "",
    val namaPenghuni : String = ""
)