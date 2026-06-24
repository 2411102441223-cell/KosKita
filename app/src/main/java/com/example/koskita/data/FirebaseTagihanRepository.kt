package com.example.koskita.data

import com.example.koskita.model.Tagihan
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseTagihanRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("tagihan")

    fun getAll(
        onSuccess: (List<Tagihan>) -> Unit,
        onError  : (String) -> Unit
    ) {
        col.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Tagihan::class.java) }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it.message ?: "Gagal ambil tagihan") }
    }

    fun tambah(
        tagihan  : Tagihan,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(tagihan.id).set(tagihan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal tambah tagihan") }
    }

    fun update(
        tagihan  : Tagihan,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(tagihan.id).set(tagihan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal update tagihan") }
    }

    // Realtime listener — Penghuni bisa lihat tagihan mereka
    fun listenByKamar(
        nomorKamar: String,
        onUpdate  : (List<Tagihan>) -> Unit
    ) = col.whereEqualTo("nomorKamar", nomorKamar)
        .addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { it.toObject(Tagihan::class.java) } ?: emptyList()
            onUpdate(list)
        }

    // Realtime listener — Pemilik bisa lihat semua tagihan
    fun listenAll(
        onUpdate: (List<Tagihan>) -> Unit
    ) = col.addSnapshotListener { snap, _ ->
        val list = snap?.documents?.mapNotNull { it.toObject(Tagihan::class.java) } ?: emptyList()
        onUpdate(list)
    }
}