package com.example.koskita.model

data class InfoKos(val judul: String, val sub: String, val waktu: String, val tipe: TipeInfo)
enum class TipeInfo { PERINGATAN, SUKSES, INFO }