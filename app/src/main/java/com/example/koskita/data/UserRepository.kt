package com.example.koskita.data

import android.content.Context
import com.example.koskita.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserRepository(context: Context) {

    private val prefs     = context.getSharedPreferences("koskita_db", Context.MODE_PRIVATE)
    private val gson      = Gson()
    private val KEY       = "daftar_user"
    private val KEY_LOGIN = "logged_in_user"

    fun getAll(): MutableList<User> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<User>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(list: MutableList<User>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun tambah(user: User) {
        val list = getAll()
        list.add(user)
        save(list)
    }

    fun update(user: User) {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == user.id }
        if (idx >= 0) {
            list[idx] = user
            save(list)
        }
    }

    fun login(email: String, password: String): User? {
        return getAll().find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }
    }

    fun saveSession(user: User) {
        prefs.edit().putString(KEY_LOGIN, gson.toJson(user)).apply()
    }

    fun getSession(): User? {
        val json = prefs.getString(KEY_LOGIN, null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_LOGIN).apply()
    }

    fun isLoggedIn(): Boolean = getSession() != null

    fun updateProfil(
        userId      : String,
        namaBaru    : String,
        emailBaru   : String,
        nomorKamar  : String
    ): Boolean {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == userId }
        if (idx < 0) return false

        // Cek email tidak duplikat
        val emailExist = list.any {
            it.email.equals(emailBaru, ignoreCase = true) && it.id != userId
        }
        if (emailExist) return false

        list[idx] = list[idx].copy(
            nama       = namaBaru,
            email      = emailBaru,
            nomorKamar = nomorKamar
        )
        save(list)

        // Update session juga
        val session = getSession()
        if (session?.id == userId) {
            saveSession(list[idx])
        }
        return true
    }

    fun gantiPassword(
        userId         : String,
        passwordLama   : String,
        passwordBaru   : String
    ): Boolean {
        val list = getAll()
        val idx  = list.indexOfFirst { it.id == userId }
        if (idx < 0) return false
        if (list[idx].password != passwordLama) return false

        list[idx] = list[idx].copy(password = passwordBaru)
        save(list)

        val session = getSession()
        if (session?.id == userId) {
            saveSession(list[idx])
        }
        return true
    }

    fun initDefaultUsers() {
        if (getAll().isEmpty()) {
            tambah(User(
                nama     = "Pak Budi",
                email    = "pemilik@koskita.com",
                password = "pemilik123",
                role     = "Pemilik"
            ))
            tambah(User(
                nama       = "Rizky Ananda",
                email      = "penghuni@koskita.com",
                password   = "penghuni123",
                role       = "Penghuni",
                nomorKamar = "2B"
            ))
        }
    }
}