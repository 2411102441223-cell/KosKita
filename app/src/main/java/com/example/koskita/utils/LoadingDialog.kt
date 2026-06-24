package com.example.koskita.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.example.koskita.R

object LoadingDialog {

    private var dialog: Dialog? = null

    fun show(context: Context, pesan: String = "Memuat data...") {
        dismiss()
        dialog = Dialog(context).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
            view.findViewById<TextView>(R.id.tvPesanLoading).text = pesan
            setContentView(view)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}