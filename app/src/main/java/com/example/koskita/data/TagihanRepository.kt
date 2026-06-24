package com.example.koskita.data

import android.content.Context
import com.example.koskita.model.Tagihan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TagihanRepository(context: Context) {

    private val prefs = context.getSharedPreferences("koskita_db", Context.MODE_PRIVATE)
    private val gson  = Gson()
    private val KEY   = "daftar_tagihan"

    fun getAll(): MutableList<Tagihan> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<Tagihan>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(list: MutableList<Tagihan>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun tambah(tagihan: Tagihan) {
        val list = getAll()
        list.add(tagihan)
        save(list)
    }

    fun update(tagihan: Tagihan) {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == tagihan.id }
        if (idx >= 0) { list[idx] = tagihan; save(list) }
    }

    fun getByKamar(kamarId: String): List<Tagihan> =
        getAll().filter { it.kamarId == kamarId }

    fun getBelumBayar(): List<Tagihan> =
        getAll().filter { it.status == "Belum Bayar" }

    fun sudahAdaTagihanBulanIni(kamarId: String): Boolean {
        val cal   = Calendar.getInstance()
        val bulan = SimpleDateFormat("MMMM", Locale("id")).format(cal.time)
        val tahun = cal.get(Calendar.YEAR)
        return getAll().any {
            it.kamarId == kamarId && it.bulan == bulan && it.tahun == tahun
        }
    }

    fun generateTagihanBulanan(kamarRepository: KamarRepository): Int {
        val kamarList  = kamarRepository.getAll().filter { it.status == "Terisi" }
        val cal        = Calendar.getInstance()
        val bulan      = SimpleDateFormat("MMMM", Locale("id")).format(cal.time)
        val tahun      = cal.get(Calendar.YEAR)
        val tglBuat    = SimpleDateFormat("d MMM yyyy", Locale("id")).format(cal.time)
        var jumlahBaru = 0

        kamarList.forEach { kamar ->
            if (!sudahAdaTagihanBulanIni(kamar.id)) {
                tambah(Tagihan(
                    kamarId       = kamar.id,
                    nomorKamar    = kamar.nomorKamar,
                    namaPenghuni  = kamar.namaPenghuni,
                    bulan         = bulan,
                    tahun         = tahun,
                    nominal       = kamar.harga,
                    status        = "Belum Bayar",
                    tanggalBuat   = tglBuat
                ))
                jumlahBaru++
            }
        }
        return jumlahBaru
    }
}