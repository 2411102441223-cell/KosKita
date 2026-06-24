package com.example.koskita.data

import com.example.koskita.model.RequestItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirebaseRequestRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("requests")

    fun getAll(
        onSuccess: (List<RequestItem>) -> Unit,
        onError  : (String) -> Unit
    ) {
        col.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(RequestItem::class.java) }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it.message ?: "Gagal ambil data") }
    }

    fun tambah(
        item     : RequestItem,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(item.id).set(item)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal tambah request") }
    }

    fun updateStatus(
        id        : String,
        statusBaru: String,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        col.document(id).update("status", statusBaru)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal update status") }
    }

    fun hapus(
        id       : String,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        col.document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal hapus") }
    }

    // Realtime listener — semua request (untuk Pemilik)
    fun listenAll(onUpdate: (List<RequestItem>) -> Unit) =
        col.orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(RequestItem::class.java)
                } ?: emptyList()
                onUpdate(list)
            }

    // Realtime listener — request milik penghuni tertentu
    fun listenByUser(
        namaUser: String,
        onUpdate: (List<RequestItem>) -> Unit
    ) = col.whereEqualTo("namaPenghuni", namaUser)
        .addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull {
                it.toObject(RequestItem::class.java)
            } ?: emptyList()
            onUpdate(list)
        }
}