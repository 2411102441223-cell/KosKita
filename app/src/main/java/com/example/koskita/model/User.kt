package com.example.koskita.model

data class User(
    val id         : String = "",
    val nama       : String = "",
    val email      : String = "",
    val password   : String = "",
    val role       : String = "Penghuni",
    val nomorKamar : String = ""
)