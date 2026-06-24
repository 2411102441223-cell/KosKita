package com.example.koskita.data

import android.util.Log
import com.example.koskita.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private const val TAG = "FirebaseUserRepo"

    fun register(
        nama       : String,
        email      : String,
        password   : String,
        role       : String,
        nomorKamar : String,
        onSuccess  : (User) -> Unit,
        onError    : (String) -> Unit
    ) {
        Log.d(TAG, "Mendaftarkan: nama=$nama, email=$email, role=$role")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    onError("UID tidak ditemukan")
                    return@addOnSuccessListener
                }
                val userData = hashMapOf(
                    "id"         to uid,
                    "nama"       to nama,
                    "email"      to email,
                    "password"   to "",
                    "role"       to role,
                    "nomorKamar" to nomorKamar
                )
                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val user = User(
                                    id         = uid,
                                    nama       = nama,
                                    email      = email,
                                    password   = "",
                                    role       = doc.getString("role") ?: role,
                                    nomorKamar = nomorKamar
                                )
                                onSuccess(user)
                            }
                            .addOnFailureListener {
                                val user = User(
                                    id         = uid,
                                    nama       = nama,
                                    email      = email,
                                    password   = "",
                                    role       = role,
                                    nomorKamar = nomorKamar
                                )
                                onSuccess(user)
                            }
                    }
                    .addOnFailureListener { onError(it.message ?: "Gagal simpan data") }
            }
            .addOnFailureListener { onError(it.message ?: "Registrasi gagal") }
    }

    fun login(
        email    : String,
        password : String,
        onSuccess: (User) -> Unit,
        onError  : (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    onError("UID tidak ditemukan")
                    return@addOnSuccessListener
                }
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = User(
                                id         = uid,
                                nama       = doc.getString("nama")       ?: "",
                                email      = doc.getString("email")      ?: email,
                                password   = "",
                                role       = doc.getString("role")       ?: "Penghuni",
                                nomorKamar = doc.getString("nomorKamar") ?: ""
                            )
                            onSuccess(user)
                        } else {
                            onError("Data user tidak ditemukan")
                        }
                    }
                    .addOnFailureListener { onError(it.message ?: "Gagal ambil data") }
            }
            .addOnFailureListener { onError(it.message ?: "Email atau password salah") }
    }

    fun logout() { auth.signOut() }

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onError  : () -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onError(); return }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = User(
                        id         = uid,
                        nama       = doc.getString("nama")       ?: "",
                        email      = doc.getString("email")      ?: "",
                        password   = "",
                        role       = doc.getString("role")       ?: "Penghuni",
                        nomorKamar = doc.getString("nomorKamar") ?: ""
                    )
                    onSuccess(user)
                } else {
                    onError()
                }
            }
            .addOnFailureListener { onError() }
    }

    fun isLoggedIn() = auth.currentUser != null

    fun updateProfil(
        uid        : String,
        nama       : String,
        email      : String,
        nomorKamar : String,
        onSuccess  : () -> Unit,
        onError    : (String) -> Unit
    ) {
        val data = mapOf(
            "nama"       to nama,
            "email"      to email,
            "nomorKamar" to nomorKamar
        )
        db.collection("users").document(uid).update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal update") }
    }

    // ── Ambil semua penghuni ──
    fun getAllPenghuni(
        onSuccess: (List<User>) -> Unit,
        onError  : (String) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("role", "Penghuni")
            .get()
            .addOnSuccessListener { docs ->
                val list = docs.documents.mapNotNull { doc ->
                    User(
                        id         = doc.getString("id")         ?: doc.id,
                        nama       = doc.getString("nama")       ?: "",
                        email      = doc.getString("email")      ?: "",
                        password   = "",
                        role       = "Penghuni",
                        nomorKamar = doc.getString("nomorKamar") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it.message ?: "Gagal ambil data penghuni") }
    }

    // ── Hapus akun penghuni dari Firestore ──
    // (hanya hapus data Firestore, Auth perlu admin SDK untuk hapus dari client)
    fun hapusAkunPenghuni(
        userId   : String,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        // Hapus dokumen user dari Firestore
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                // Hapus semua notifikasi milik user
                db.collection("notifikasi")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { docs ->
                        docs.forEach { it.reference.delete() }
                    }
                // Hapus semua request milik user
                db.collection("requests")
                    .whereEqualTo("namaPenghuni", userId)
                    .get()
                    .addOnSuccessListener { docs ->
                        docs.forEach { it.reference.delete() }
                    }
                Log.d(TAG, "Akun penghuni $userId berhasil dihapus dari Firestore")
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Gagal hapus akun") }
    }
}