package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.data.Kamar
import com.example.koskita.databinding.ItemKamarBinding

class KamarAdapter(
    private val list   : MutableList<Kamar>,
    private val onEdit : (Kamar) -> Unit,
    private val onHapus: (Kamar) -> Unit
) : RecyclerView.Adapter<KamarAdapter.VH>() {

    inner class VH(val binding: ItemKamarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemKamarBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvNomorKamar.text   = item.nomorKamar
            tvLantaiKamar.text  = "Lantai ${item.lantai}"
            tvHargaKamar.text   = "Rp ${String.format("%,d", item.harga).replace(',', '.')}/bln"
            tvStatusKamar.text  = item.status
            tvNamaPenghuni.text = if (item.status == "Terisi") item.namaPenghuni else "Kamar kosong"

            if (item.status == "Terisi") {
                tvStatusKamar.setBackgroundColor(0xFFEAF3DE.toInt())
                tvStatusKamar.setTextColor(0xFF3B6D11.toInt())
            } else {
                tvStatusKamar.setBackgroundColor(0xFFFAEEDA.toInt())
                tvStatusKamar.setTextColor(0xFF633806.toInt())
            }

            btnEdit.setOnClickListener  { onEdit(item)  }
            btnHapus.setOnClickListener { onHapus(item) }
        }
    }

    fun refresh(newList: MutableList<Kamar>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}