package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.databinding.ItemNotifikasiBinding
import com.example.koskita.model.Notifikasi

class NotifikasiAdapter(
    private val list    : MutableList<Notifikasi>,
    private val onKlik  : (Notifikasi) -> Unit,
    private val onHapus : (Notifikasi) -> Unit
) : RecyclerView.Adapter<NotifikasiAdapter.VH>() {

    inner class VH(val binding: ItemNotifikasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNotifikasiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvJudulNotif.text = item.judul
            tvIsiNotif.text   = item.isi
            tvWaktuNotif.text = item.waktu

            // Dot merah kalau belum dibaca
            viewDotBelumDibaca.visibility =
                if (!item.dibaca) View.VISIBLE else View.INVISIBLE

            // Background berbeda kalau belum dibaca
            root.setBackgroundColor(
                if (!item.dibaca) 0xFFFFF8F0.toInt()
                else 0xFFFFFFFF.toInt()
            )

            // Warna judul berdasarkan tipe
            val warnaTipe = when (item.tipe) {
                "TAGIHAN" -> 0xFF854F0B.toInt()
                "REQUEST" -> 0xFF185FA5.toInt()
                "PENTING" -> 0xFFA32D2D.toInt()
                else      -> 0xFF2C2C2A.toInt()
            }
            tvJudulNotif.setTextColor(warnaTipe)

            root.setOnClickListener      { onKlik(item)  }
            btnHapusNotif.setOnClickListener { onHapus(item) }
        }
    }

    fun refresh(newList: List<Notifikasi>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}