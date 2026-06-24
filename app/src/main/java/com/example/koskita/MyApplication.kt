package com.example.koskita

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.koskita.data.UserRepository
import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Clear session lokal kalau Firebase sudah logout
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            UserRepository(this).clearSession()
        }

        // Apply dark mode
        val prefs: SharedPreferences =
            getSharedPreferences("koskita_settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}