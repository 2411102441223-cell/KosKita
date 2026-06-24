package com.example.koskita.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KamarRepository(context: Context) {

    private val prefs = context.getSharedPreferences("koskita_db", Context.MODE_PRIVATE)
    private val gson  = Gson()
    private val KEY   = "daftar_kamar"

    fun getAll(): MutableList<Kamar> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<Kamar>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(list: MutableList<Kamar>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun tambah(kamar: Kamar) {
        val list = getAll()
        list.add(kamar)
        save(list)
    }

    fun update(kamar: Kamar) {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == kamar.id }
        if (idx >= 0) { list[idx] = kamar; save(list) }
    }

    fun hapus(id: String) {
        val list = getAll()
        list.removeAll { it.id == id }
        save(list)
    }

    fun getById(id: String): Kamar? = getAll().find { it.id == id }
}