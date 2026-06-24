package com.example.koskita.data

import android.util.Log
import com.example.koskita.model.Notifikasi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

object FirebaseNotifikasiRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("notifikasi")
    private const val TAG = "FirebaseNotifRepo"

    fun tambah(
        notif    : Notifikasi,
        onSuccess: () -> Unit = {},
        onError  : (String) -> Unit = {}
    ) {
        col.document(notif.id).set(notif)
            .addOnSuccessListener {
                Log.d(TAG, "Notifikasi berhasil: ${notif.judul}")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "Gagal: ${it.message}")
                onError(it.message ?: "Gagal")
            }
    }

    fun tandaiDibaca(id: String) {
        col.document(id).update("dibaca", true)
    }

    fun tandaiSemuaDibaca(userId: String) {
        col.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { docs ->
                docs.forEach { it.reference.update("dibaca", true) }
            }
    }

    fun hapus(id: String) {
        col.document(id).delete()
    }

    fun hapusSemua(userId: String) {
        col.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { docs ->
                docs.forEach { it.reference.delete() }
            }
    }

    fun listenByUser(
        userId  : String,
        onUpdate: (List<Notifikasi>) -> Unit
    ) = col.whereEqualTo("userId", userId)
        .orderBy("waktu", Query.Direction.DESCENDING)
        .addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e(TAG, "Listen error: ${error.message}")
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull {
                it.toObject(Notifikasi::class.java)
            } ?: emptyList()
            onUpdate(list)
        }

    fun getBelumDibacaCount(
        userId  : String,
        onResult: (Int) -> Unit
    ) {
        col.whereEqualTo("userId", userId)
            .whereEqualTo("dibaca", false)
            .get()
            .addOnSuccessListener { onResult(it.size()) }
            .addOnFailureListener { onResult(0) }
    }

    private fun buatWaktu() =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id")).format(Date())

    // ── Kirim notif langsung ke userId ──
    fun kirimNotifTagihanBaru(
        userId     : String,
        nomorKamar : String,
        bulan      : String,
        nominal    : String
    ) {
        tambah(Notifikasi(
            id     = UUID.randomUUID().toString(),
            judul  = "💰 Tagihan Baru",
            isi    = "Tagihan bulan $bulan Kamar $nomorKamar sebesar $nominal telah dibuat.",
            waktu  = buatWaktu(),
            tipe   = "TAGIHAN",
            dibaca = false,
            userId = userId
        ))
    }

    fun kirimNotifTagihanLunas(
        userId     : String,
        nomorKamar : String,
        bulan      : String
    ) {
        tambah(Notifikasi(
            id     = UUID.randomUUID().toString(),
            judul  = "✅ Tagihan Lunas",
            isi    = "Pembayaran bulan $bulan Kamar $nomorKamar telah dikonfirmasi lunas.",
            waktu  = buatWaktu(),
            tipe   = "TAGIHAN",
            dibaca = false,
            userId = userId
        ))
    }

    fun kirimNotifRequestDiupdate(
        userId   : String,
        kategori : String,
        status   : String
    ) {
        tambah(Notifikasi(
            id     = UUID.randomUUID().toString(),
            judul  = "🔄 Status Request Diperbarui",
            isi    = "Request perbaikan $kategori diperbarui menjadi: $status.",
            waktu  = buatWaktu(),
            tipe   = "REQUEST",
            dibaca = false,
            userId = userId
        ))
    }

    fun kirimNotifPengumuman(
        userId : String,
        judul  : String,
        isi    : String
    ) {
        tambah(Notifikasi(
            id     = UUID.randomUUID().toString(),
            judul  = "📢 $judul",
            isi    = isi,
            waktu  = buatWaktu(),
            tipe   = "INFO",
            dibaca = false,
            userId = userId
        ))
    }

    // ── Helper: cari userId berdasarkan nama lalu kirim notif ──
    fun cariUserLaluKirimTagihanBaru(
        namaPenghuni: String,
        nomorKamar  : String,
        bulan       : String,
        nominal     : String
    ) {
        // Cari berdasarkan nama dulu
        db.collection("users")
            .whereEqualTo("nama", namaPenghuni)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    docs.forEach { doc ->
                        val userId = doc.getString("id") ?: doc.id
                        kirimNotifTagihanBaru(userId, nomorKamar, bulan, nominal)
                    }
                } else {
                    // Fallback: cari berdasarkan nomorKamar
                    db.collection("users")
                        .whereEqualTo("nomorKamar", nomorKamar)
                        .get()
                        .addOnSuccessListener { docs2 ->
                            docs2.forEach { doc ->
                                val userId = doc.getString("id") ?: doc.id
                                kirimNotifTagihanBaru(userId, nomorKamar, bulan, nominal)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Gagal cari user: ${it.message}")
            }
    }

    fun cariUserLaluKirimTagihanLunas(
        namaPenghuni: String,
        nomorKamar  : String,
        bulan       : String
    ) {
        db.collection("users")
            .whereEqualTo("nama", namaPenghuni)
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val userId = doc.getString("id") ?: doc.id
                    kirimNotifTagihanLunas(userId, nomorKamar, bulan)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Gagal cari user: ${it.message}")
            }
    }

    fun cariUserLaluKirimRequestUpdate(
        namaPenghuni: String,
        kategori    : String,
        status      : String
    ) {
        db.collection("users")
            .whereEqualTo("nama", namaPenghuni)
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val userId = doc.getString("id") ?: doc.id
                    kirimNotifRequestDiupdate(userId, kategori, status)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Gagal cari user: ${it.message}")
            }
    }
}