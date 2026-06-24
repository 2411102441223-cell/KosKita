package com.example.koskita

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.KamarAdapter
import com.example.koskita.data.FirebaseKamarRepository
import com.example.koskita.data.Kamar
import com.example.koskita.databinding.ActivityKelolaKamarBinding
import com.example.koskita.utils.LoadingDialog

class KelolaKamarActivity : BaseActivity() {

    private lateinit var binding : ActivityKelolaKamarBinding
    private lateinit var adapter : KamarAdapter
    private var isFirstLoad      = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnTambahKamar.setOnClickListener {
            startActivity(Intent(this, FormKamarActivity::class.java))
        }

        setupList()
        LoadingDialog.show(this, "Memuat data kamar...")
        listenData()
    }

    private fun setupList() {
        adapter = KamarAdapter(
            mutableListOf(),
            onEdit = { kamar ->
                val intent = Intent(this, FormKamarActivity::class.java)
                intent.putExtra("kamar_id", kamar.id)
                startActivity(intent)
            },
            onHapus = { kamar ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Kamar")
                    .setMessage("Hapus kamar ${kamar.nomorKamar}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        FirebaseKamarRepository.hapus(
                            id        = kamar.id,
                            onSuccess = {
                                Toast.makeText(this, "✅ Kamar dihapus!", Toast.LENGTH_SHORT).show()
                            },
                            onError = {
                                Toast.makeText(this, "❌ $it", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        binding.rvKamar.apply {
            layoutManager = LinearLayoutManager(this@KelolaKamarActivity)
            adapter = this@KelolaKamarActivity.adapter
        }
    }

    private fun listenData() {
        FirebaseKamarRepository.listenAll { list ->
            if (isFirstLoad) {
                LoadingDialog.dismiss()
                isFirstLoad = false
            }
            adapter.refresh(list.toMutableList())
            binding.tvTotalKamar.text  = list.size.toString()
            binding.tvKamarTerisi.text = list.count { it.status == "Terisi" }.toString()
            binding.tvKamarKosong.text = list.count { it.status == "Kosong" }.toString()

            // Empty state
            if (list.isEmpty()) {
                binding.tvEmptyKamar.visibility = View.VISIBLE
                binding.rvKamar.visibility      = View.GONE
            } else {
                binding.tvEmptyKamar.visibility = View.GONE
                binding.rvKamar.visibility      = View.VISIBLE
            }
        }
    }
}