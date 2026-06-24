package com.example.koskita.model

data class Notifikasi(
    val id     : String  = "",
    val judul  : String  = "",
    val isi    : String  = "",
    val waktu  : String  = "",
    val tipe   : String  = "INFO",
    val dibaca : Boolean = false,
    val userId : String  = ""
)