package com.example.koskita.data

import android.content.Context
import com.example.koskita.model.RequestItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RequestRepository(context: Context) {

    private val prefs = context.getSharedPreferences("koskita_db", Context.MODE_PRIVATE)
    private val gson  = Gson()
    private val KEY   = "daftar_request"

    fun getAll(): MutableList<RequestItem> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<RequestItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(list: MutableList<RequestItem>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun tambah(item: RequestItem) {
        val list = getAll()
        list.add(0, item)
        save(list)
    }

    fun updateStatus(id: String, statusBaru: String) {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = statusBaru)
            save(list)
        }
    }

    fun hapus(id: String) {
        val list = getAll()
        list.removeAll { it.id == id }
        save(list)
    }

    fun getMenunggu()  = getAll().filter { it.status == "Menunggu"  }
    fun getDiproses()  = getAll().filter { it.status == "Diproses"  }
    fun getSelesai()   = getAll().filter { it.status == "Selesai"   }
}