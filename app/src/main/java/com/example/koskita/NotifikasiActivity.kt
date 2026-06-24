package com.example.koskita

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koskita.adapter.NotifikasiAdapter
import com.example.koskita.data.FirebaseNotifikasiRepository
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.data.UserRepository
import com.example.koskita.databinding.ActivityNotifikasiBinding

class NotifikasiActivity : BaseActivity() {

    private lateinit var binding  : ActivityNotifikasiBinding
    private lateinit var userRepo : UserRepository
    private lateinit var adapter  : NotifikasiAdapter
    private var userId            = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityNotifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnBacaSemua.setOnClickListener {
            if (userId.isNotEmpty()) {
                FirebaseNotifikasiRepository.tandaiSemuaDibaca(userId)
            }
        }

        binding.btnHapusSemua.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Semua Notifikasi")
                .setMessage("Yakin ingin menghapus semua notifikasi?")
                .setPositiveButton("Hapus") { _, _ ->
                    if (userId.isNotEmpty()) {
                        FirebaseNotifikasiRepository.hapusSemua(userId)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        setupAdapter()
        ambilUserIdLaluListen()
    }

    private fun setupAdapter() {
        adapter = NotifikasiAdapter(
            mutableListOf(),
            onKlik  = { notif -> FirebaseNotifikasiRepository.tandaiDibaca(notif.id) },
            onHapus = { notif -> FirebaseNotifikasiRepository.hapus(notif.id) }
        )
        binding.rvNotifikasi.apply {
            layoutManager = LinearLayoutManager(this@NotifikasiActivity)
            adapter       = this@NotifikasiActivity.adapter
        }
    }

    private fun ambilUserIdLaluListen() {
        FirebaseUserRepository.getCurrentUser(
            onSuccess = { user ->
                userId = user.id
                listenNotifikasi()
            },
            onError = {
                val session = userRepo.getSession()
                userId = session?.id ?: ""
                if (userId.isNotEmpty()) listenNotifikasi()
            }
        )
    }

    private fun listenNotifikasi() {
        FirebaseNotifikasiRepository.listenByUser(userId) { list ->
            adapter.refresh(list)
            val belumDibaca = list.count { !it.dibaca }

            if (belumDibaca > 0) {
                binding.tvBelumDibaca.visibility = View.VISIBLE
                binding.tvBelumDibaca.text       = "$belumDibaca notifikasi belum dibaca"
            } else {
                binding.tvBelumDibaca.visibility = View.GONE
            }

            // Empty state
            if (list.isEmpty()) {
                binding.tvEmptyNotif.visibility = View.VISIBLE
                binding.rvNotifikasi.visibility = View.GONE
            } else {
                binding.tvEmptyNotif.visibility = View.GONE
                binding.rvNotifikasi.visibility = View.VISIBLE
            }
        }
    }
}