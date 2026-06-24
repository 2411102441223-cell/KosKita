package com.example.koskita.data

data class Kamar(
    val id          : String = "",
    val nomorKamar  : String = "",
    val lantai      : String = "",
    val harga       : Int    = 0,
    val status      : String = "Kosong",
    val namaPenghuni: String = "",
    val noHp        : String = ""
)