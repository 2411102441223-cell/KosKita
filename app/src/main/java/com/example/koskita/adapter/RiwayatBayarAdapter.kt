package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.databinding.ItemRiwayatBayarBinding
import com.example.koskita.model.RiwayatBayar

class RiwayatBayarAdapter(private val list: MutableList<RiwayatBayar>) :
    RecyclerView.Adapter<RiwayatBayarAdapter.VH>() {

    inner class VH(val binding: ItemRiwayatBayarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRiwayatBayarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvBulanRiwayat.text   = item.bulan
            tvTanggalBayar.text   = "Dibayar: ${item.tanggalBayar}"
            tvNominalRiwayat.text = item.nominal
            tvMetodeBayar.text    = item.metode
            tvEmojiMetode.text    = item.emojiMetode
            tvStatusRiwayat.text  = item.status

            if (item.status == "Lunas") {
                tvStatusRiwayat.setBackgroundColor(0xFFEAF3DE.toInt())
                tvStatusRiwayat.setTextColor(0xFF3B6D11.toInt())
            } else {
                tvStatusRiwayat.setBackgroundColor(0xFFFAEEDA.toInt())
                tvStatusRiwayat.setTextColor(0xFF633806.toInt())
            }
        }
    }

    fun refresh(newList: MutableList<RiwayatBayar>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}