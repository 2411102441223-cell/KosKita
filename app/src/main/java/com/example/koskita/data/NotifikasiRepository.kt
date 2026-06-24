package com.example.koskita.data

import android.content.Context
import com.example.koskita.model.Notifikasi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiRepository(context: Context) {

    private val prefs = context.getSharedPreferences("koskita_db", Context.MODE_PRIVATE)
    private val gson  = Gson()
    private val KEY   = "daftar_notifikasi"

    fun getAll(): MutableList<Notifikasi> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<Notifikasi>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(list: MutableList<Notifikasi>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun tambah(notif: Notifikasi) {
        val list = getAll()
        list.add(0, notif)
        // Maksimal 50 notifikasi
        if (list.size > 50) list.removeAt(list.size - 1)
        save(list)
    }

    fun tandaiDibaca(id: String) {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(dibaca = true)
            save(list)
        }
    }

    fun tandaiSemuaDibaca() {
        val list = getAll().map { it.copy(dibaca = true) }.toMutableList()
        save(list)
    }

    fun hapus(id: String) {
        val list = getAll()
        list.removeAll { it.id == id }
        save(list)
    }

    fun hapusSemua() {
        save(mutableListOf())
    }

    fun getBelumDibaca(): Int = getAll().count { !it.dibaca }

    fun buatWaktu(): String =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id")).format(Date())

    // ── Helper untuk buat notifikasi standar ──
    fun notifTagihanBaru(nomorKamar: String, bulan: String, nominal: String) {
        tambah(Notifikasi(
            judul  = "💰 Tagihan Baru",
            isi    = "Tagihan bulan $bulan untuk Kamar $nomorKamar sebesar $nominal telah dibuat.",
            waktu  = buatWaktu(),
            tipe   = "TAGIHAN"
        ))
    }

    fun notifTagihanLunas(nomorKamar: String, bulan: String) {
        tambah(Notifikasi(
            judul  = "✅ Tagihan Lunas",
            isi    = "Pembayaran bulan $bulan Kamar $nomorKamar telah dikonfirmasi.",
            waktu  = buatWaktu(),
            tipe   = "TAGIHAN"
        ))
    }

    fun notifRequestDikirim(kategori: String) {
        tambah(Notifikasi(
            judul  = "🔧 Request Terkirim",
            isi    = "Request perbaikan kategori $kategori berhasil dikirim dan sedang menunggu penanganan.",
            waktu  = buatWaktu(),
            tipe   = "REQUEST"
        ))
    }

    fun notifRequestDiupdate(kategori: String, status: String) {
        tambah(Notifikasi(
            judul  = "🔄 Status Request Diperbarui",
            isi    = "Request perbaikan $kategori diperbarui menjadi: $status.",
            waktu  = buatWaktu(),
            tipe   = "REQUEST"
        ))
    }

    fun notifPengumuman(judul: String, isi: String) {
        tambah(Notifikasi(
            judul  = "📢 $judul",
            isi    = isi,
            waktu  = buatWaktu(),
            tipe   = "INFO"
        ))
    }

    fun initContohNotifikasi() {
        if (getAll().isEmpty()) {
            tambah(Notifikasi(
                judul = "🎉 Selamat Datang di KosKita!",
                isi   = "Terima kasih telah menggunakan KosKita. Kelola kos kamu dengan mudah!",
                waktu = buatWaktu(),
                tipe  = "INFO"
            ))
            tambah(Notifikasi(
                judul = "💰 Tagihan Mei 2026",
                isi   = "Tagihan bulan Mei 2026 sebesar Rp 650.000 sudah tersedia. Segera lakukan pembayaran.",
                waktu = buatWaktu(),
                tipe  = "TAGIHAN"
            ))
            tambah(Notifikasi(
                judul = "📢 Pengumuman Pemilik",
                isi   = "Perbaikan pompa air akan dilakukan pada hari Senin pukul 10.00-12.00 WIB.",
                waktu = buatWaktu(),
                tipe  = "INFO"
            ))
        }
    }
}