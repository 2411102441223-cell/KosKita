package com.example.koskita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.koskita.data.FirebaseKamarRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.databinding.ActivityHapusPenghuniBinding
import com.example.koskita.databinding.ItemPenghuniBinding
import com.example.koskita.model.User
import com.example.koskita.utils.LoadingDialog

class HapusPenghuniActivity : BaseActivity() {

    private lateinit var binding : ActivityHapusPenghuniBinding
    private val listPenghuni     = mutableListOf<User>()
    private lateinit var adapter : PenghuniAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHapusPenghuniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupList()
        loadPenghuni()
    }

    private fun setupList() {
        adapter = PenghuniAdapter(listPenghuni) { user ->
            showKonfirmasiHapus(user)
        }
        binding.rvPenghuni.apply {
            layoutManager = LinearLayoutManager(this@HapusPenghuniActivity)
            adapter = this@HapusPenghuniActivity.adapter
        }
    }

    private fun loadPenghuni() {
        LoadingDialog.show(this, "Memuat daftar penghuni...")
        FirebaseUserRepository.getAllPenghuni(
            onSuccess = { list ->
                LoadingDialog.dismiss()
                listPenghuni.clear()
                listPenghuni.addAll(list)
                adapter.notifyDataSetChanged()

                if (list.isEmpty()) {
                    binding.tvEmptyPenghuni.visibility = View.VISIBLE
                    binding.rvPenghuni.visibility      = View.GONE
                } else {
                    binding.tvEmptyPenghuni.visibility = View.GONE
                    binding.rvPenghuni.visibility      = View.VISIBLE
                }
            },
            onError = { msg ->
                LoadingDialog.dismiss()
                Toast.makeText(this, "❌ $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showKonfirmasiHapus(user: User) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Hapus Akun Penghuni")
            .setMessage(
                "Yakin ingin menghapus akun:\n\n" +
                        "👤 ${user.nama}\n" +
                        "📧 ${user.email}\n" +
                        "🏠 Kamar ${user.nomorKamar}\n\n" +
                        "Data notifikasi dan request penghuni ini juga akan dihapus.\n" +
                        "Tindakan ini tidak bisa dibatalkan!"
            )
            .setPositiveButton("Hapus") { _, _ ->
                hapusAkun(user)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusAkun(user: User) {
        LoadingDialog.show(this, "Menghapus akun...")
        FirebaseUserRepository.hapusAkunPenghuni(
            userId    = user.id,
            onSuccess = {
                LoadingDialog.dismiss()

                // Update status kamar jadi Kosong
                if (user.nomorKamar.isNotEmpty()) {
                    FirebaseKamarRepository.getAll(
                        onSuccess = { kamarList ->
                            val kamar = kamarList.find { it.nomorKamar == user.nomorKamar }
                            kamar?.let {
                                val kamarUpdated = it.copy(
                                    status       = "Kosong",
                                    namaPenghuni = "",
                                    noHp         = ""
                                )
                                FirebaseKamarRepository.update(kamarUpdated, {}, {})
                            }
                        },
                        onError = {}
                    )
                }

                Toast.makeText(
                    this,
                    "✅ Akun ${user.nama} berhasil dihapus!",
                    Toast.LENGTH_LONG
                ).show()
                loadPenghuni() // Refresh list
            },
            onError = { msg ->
                LoadingDialog.dismiss()
                Toast.makeText(this, "❌ $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Adapter inline ──
    inner class PenghuniAdapter(
        private val list    : List<User>,
        private val onHapus : (User) -> Unit
    ) : RecyclerView.Adapter<PenghuniAdapter.VH>() {

        inner class VH(val binding: ItemPenghuniBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemPenghuniBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = list[position]
            with(holder.binding) {
                tvNamaPenghuniItem.text   = user.nama
                tvEmailPenghuniItem.text  = user.email
                tvKamarPenghuniItem.text  = if (user.nomorKamar.isNotEmpty())
                    "🏠 Kamar ${user.nomorKamar}" else "🏠 Belum ada kamar"
                btnHapusPenghuni.setOnClickListener { onHapus(user) }
            }
        }
    }
}