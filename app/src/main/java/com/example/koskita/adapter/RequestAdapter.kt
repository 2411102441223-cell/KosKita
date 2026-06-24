package com.example.koskita.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.databinding.ItemRequestBinding
import com.example.koskita.model.RequestItem

class RequestAdapter(
    private val list        : MutableList<RequestItem>,
    private val onUpdateStatus: ((RequestItem) -> Unit)? = null
) : RecyclerView.Adapter<RequestAdapter.VH>() {

    inner class VH(val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvEmojiKategori.text    = item.emoji
            tvKategori.text         = item.kategori
            tvDeskripsiRequest.text = item.deskripsi
            tvTanggalRequest.text   = item.tanggal
            tvStatusRequest.text    = item.status

            val (bgColor, txtColor) = when (item.status) {
                "Selesai"  -> Pair(0xFFEAF3DE.toInt(), 0xFF3B6D11.toInt())
                "Diproses" -> Pair(0xFFE6F1FB.toInt(), 0xFF185FA5.toInt())
                else       -> Pair(0xFFFAEEDA.toInt(), 0xFF633806.toInt())
            }
            tvStatusRequest.setBackgroundColor(bgColor)
            tvStatusRequest.setTextColor(txtColor)

            onUpdateStatus?.let { listener ->
                root.setOnLongClickListener {
                    listener(item)
                    true
                }
            }
        }
    }

    fun refresh(newList: List<RequestItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}