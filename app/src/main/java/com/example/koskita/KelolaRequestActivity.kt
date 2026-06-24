package com.example.koskita

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.RequestAdapter
import com.example.koskita.data.FirebaseNotifikasiRepository
import com.example.koskita.data.FirebaseRequestRepository
import com.example.koskita.databinding.ActivityKelolaRequestBinding
import com.example.koskita.model.RequestItem

class KelolaRequestActivity : BaseActivity() {

    private lateinit var binding : ActivityKelolaRequestBinding
    private lateinit var adapter : RequestAdapter
    private var filterAktif      = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        setupAdapter()
        setupTab()
        listenData()
    }

    private fun setupAdapter() {
        adapter = RequestAdapter(mutableListOf()) { item ->
            showUpdateStatusDialog(item)
        }
        binding.rvKelolaRequest.apply {
            layoutManager = LinearLayoutManager(this@KelolaRequestActivity)
            adapter = this@KelolaRequestActivity.adapter
        }
    }

    private fun setupTab() {
        val tabs = mapOf(
            binding.tabSemua    to "Semua",
            binding.tabMenunggu to "Menunggu",
            binding.tabDiproses to "Diproses",
            binding.tabSelesai  to "Selesai"
        )
        tabs.forEach { (tv, label) ->
            tv.setOnClickListener {
                filterAktif = label
                tabs.keys.forEach { k ->
                    k.setBackgroundColor(0x00000000)
                    k.setTextColor(0xFFFAC775.toInt())
                    k.typeface = android.graphics.Typeface.DEFAULT
                }
                tv.setBackgroundColor(0xFFFFFFFF.toInt())
                tv.setTextColor(0xFF854F0B.toInt())
                listenData()
            }
        }
    }

    private fun listenData() {
        FirebaseRequestRepository.listenAll { allList ->
            val filtered = when (filterAktif) {
                "Menunggu" -> allList.filter { it.status == "Menunggu" }
                "Diproses" -> allList.filter { it.status == "Diproses" }
                "Selesai"  -> allList.filter { it.status == "Selesai"  }
                else       -> allList
            }
            adapter.refresh(filtered)

            if (filtered.isEmpty()) {
                binding.tvEmptyRequest.visibility  = View.VISIBLE
                binding.rvKelolaRequest.visibility = View.GONE
            } else {
                binding.tvEmptyRequest.visibility  = View.GONE
                binding.rvKelolaRequest.visibility = View.VISIBLE
            }
        }
    }

    private fun showUpdateStatusDialog(item: RequestItem) {
        val statusList = arrayOf("Menunggu", "Diproses", "Selesai")
        val currentIdx = statusList.indexOf(item.status)

        AlertDialog.Builder(this)
            .setTitle("Update Status — ${item.kategori}")
            .setSingleChoiceItems(statusList, currentIdx) { dialog, which ->
                val statusBaru = statusList[which]
                FirebaseRequestRepository.updateStatus(
                    id         = item.id,
                    statusBaru = statusBaru,
                    onSuccess  = {
                        Toast.makeText(this, "✅ Status diperbarui!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        FirebaseNotifikasiRepository.cariUserLaluKirimRequestUpdate(
                            namaPenghuni = item.namaPenghuni,
                            kategori     = item.kategori,
                            status       = statusBaru
                        )
                    },
                    onError = { Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}