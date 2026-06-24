package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.databinding.ItemTagihanBinding
import com.example.koskita.model.Tagihan

class TagihanAdapter(
    private val list       : MutableList<Tagihan>,
    private val onKonfirmasi: (Tagihan) -> Unit
) : RecyclerView.Adapter<TagihanAdapter.VH>() {

    inner class VH(val binding: ItemTagihanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTagihanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvNamaTagihan.text  = item.namaPenghuni
            tvKamarTagihan.text = "Kamar ${item.nomorKamar}"
            tvBulanTagihan.text = "${item.bulan} ${item.tahun}"
            tvNominalTagihan.text = "Rp ${String.format("%,d", item.nominal).replace(',', '.')}"

            if (item.status == "Sudah Bayar") {
                tvStatusTagihan.text = "✓ Lunas"
                tvStatusTagihan.setBackgroundColor(0xFFEAF3DE.toInt())
                tvStatusTagihan.setTextColor(0xFF3B6D11.toInt())
                btnKonfirmasi.visibility = View.GONE
            } else {
                tvStatusTagihan.text = "Belum Bayar"
                tvStatusTagihan.setBackgroundColor(0xFFFCEBEB.toInt())
                tvStatusTagihan.setTextColor(0xFFA32D2D.toInt())
                btnKonfirmasi.visibility = View.VISIBLE
                btnKonfirmasi.setOnClickListener { onKonfirmasi(item) }
            }
        }
    }

    fun refresh(newList: MutableList<Tagihan>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}