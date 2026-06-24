package com.example.koskita.model

data class RiwayatBayar(
    val bulan      : String,
    val tanggalBayar: String,
    val nominal    : String,
    val metode     : String,
    val emojiMetode: String,
    val status     : String
)
