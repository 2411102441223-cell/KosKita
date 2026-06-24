package com.example.koskita.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirebasePengumumanRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("pengumuman")

    data class Pengumuman(
        val id     : String = "",
        val judul  : String = "",
        val isi    : String = "",
        val waktu  : String = "",
        val tipe   : String = "INFO"
    )

    fun listenAll(onUpdate: (List<Pengumuman>) -> Unit) =
        col.orderBy("waktu", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Pengumuman::class.java)
                } ?: emptyList()
                onUpdate(list)
            }

    fun tambah(
        pengumuman: Pengumuman,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        col.document(pengumuman.id).set(pengumuman)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal") }
    }

    fun hapus(id: String) {
        col.document(id).delete()
    }
}