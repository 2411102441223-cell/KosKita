package com.example.koskita.data

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseKamarRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("kamar")

    fun getAll(
        onSuccess: (List<Kamar>) -> Unit,
        onError  : (String) -> Unit
    ) {
        col.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Kamar::class.java) }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it.message ?: "Gagal ambil data kamar") }
    }

    fun tambah(
        kamar    : Kamar,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(kamar.id).set(kamar)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal tambah kamar") }
    }

    fun update(
        kamar    : Kamar,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(kamar.id).set(kamar)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal update kamar") }
    }

    fun hapus(
        id       : String,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal hapus kamar") }
    }

    fun listenAll(
        onUpdate: (List<Kamar>) -> Unit
    ) = col.addSnapshotListener { snap, _ ->
        val list = snap?.documents?.mapNotNull { it.toObject(Kamar::class.java) } ?: emptyList()
        onUpdate(list)
    }
}