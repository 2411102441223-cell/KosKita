package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.databinding.ItemInfoBinding
import com.example.koskita.model.InfoKos
import com.example.koskita.model.TipeInfo

class InfoAdapter(
    private val list: MutableList<InfoKos>
) : RecyclerView.Adapter<InfoAdapter.VH>() {

    inner class VH(val binding: ItemInfoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvJudul.text = item.judul
            tvSub.text   = item.sub
            tvWaktu.text = item.waktu

            val dotColor = when (item.tipe) {
                TipeInfo.PERINGATAN -> 0xFFEF9F27.toInt()
                TipeInfo.SUKSES     -> 0xFF5DCAA5.toInt()
                else                -> 0xFF185FA5.toInt()
            }
            viewDot.setBackgroundColor(dotColor)
        }
    }

    fun refresh(newList: MutableList<InfoKos>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}