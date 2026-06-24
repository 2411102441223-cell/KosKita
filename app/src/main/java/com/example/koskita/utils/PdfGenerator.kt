package com.example.koskita.utils

import android.content.Context
import android.os.Environment
import com.example.koskita.model.Tagihan
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateTagihanPDF(
        context : Context,
        list    : List<Tagihan>,
        judul   : String = "Laporan Tagihan KosKita"
    ): File? {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val tgl     = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file    = File(dir, "Tagihan_KosKita_$tgl.pdf")
            val stream  = FileOutputStream(file)
            val doc     = Document(PageSize.A4, 36f, 36f, 54f, 36f)
            val writer  = PdfWriter.getInstance(doc, stream)

            doc.open()

            // ── Warna brand ──
            val amber  = BaseColor(133, 79, 11)
            val amberL = BaseColor(250, 238, 218)
            val white  = BaseColor(255, 255, 255)
            val gray   = BaseColor(136, 135, 128)
            val dark   = BaseColor(44, 44, 42)

            // ── Font ──
            val fontTitle  = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD,  white)
            val fontSub    = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, amberL)
            val fontHeader = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD,  white)
            val fontNormal = Font(Font.FontFamily.HELVETICA, 9f,  Font.NORMAL, dark)
            val fontBold   = Font(Font.FontFamily.HELVETICA, 9f,  Font.BOLD,   dark)
            val fontAmber  = Font(Font.FontFamily.HELVETICA, 9f,  Font.BOLD,   amber)
            val fontGray   = Font(Font.FontFamily.HELVETICA, 8f,  Font.NORMAL, gray)

            // ── Header ──
            val headerTable = PdfPTable(1)
            headerTable.widthPercentage = 100f
            val headerCell = PdfPCell().apply {
                backgroundColor      = amber
                border               = Rectangle.NO_BORDER
                paddingTop           = 16f
                paddingBottom        = 16f
                paddingLeft          = 20f
                horizontalAlignment  = Element.ALIGN_LEFT
            }
            headerCell.addElement(Paragraph(judul, fontTitle))
            headerCell.addElement(Paragraph(
                "Dicetak: ${SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id")).format(Date())}",
                fontSub
            ))
            headerTable.addCell(headerCell)
            doc.add(headerTable)
            doc.add(Chunk.NEWLINE)

            // ── Summary ──
            val totalTagihan  = list.size
            val sudahBayar    = list.count { it.status == "Sudah Bayar" }
            val belumBayar    = list.count { it.status == "Belum Bayar" }
            val totalNominal  = list.sumOf { it.nominal }
            val totalLunas    = list.filter { it.status == "Sudah Bayar" }.sumOf { it.nominal }

            val summaryTable = PdfPTable(3)
            summaryTable.widthPercentage = 100f
            summaryTable.setWidths(floatArrayOf(1f, 1f, 1f))

            fun summaryCell(label: String, value: String): PdfPCell {
                val cell = PdfPCell().apply {
                    backgroundColor = amberL
                    border          = Rectangle.BOX
                    borderColor     = amber
                    paddingTop      = 10f
                    paddingBottom   = 10f
                    horizontalAlignment = Element.ALIGN_CENTER
                }
                cell.addElement(Paragraph(value, Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, amber)))
                cell.addElement(Paragraph(label, fontGray))
                return cell
            }

            summaryTable.addCell(summaryCell("Total Tagihan", totalTagihan.toString()))
            summaryTable.addCell(summaryCell("Sudah Bayar",   sudahBayar.toString()))
            summaryTable.addCell(summaryCell("Belum Bayar",   belumBayar.toString()))
            doc.add(summaryTable)
            doc.add(Chunk.NEWLINE)

            // ── Info total nominal ──
            val infoTable = PdfPTable(2)
            infoTable.widthPercentage = 100f
            infoTable.setWidths(floatArrayOf(1f, 1f))

            fun infoCell(label: String, value: String, color: BaseColor = dark): PdfPCell {
                val cell = PdfPCell().apply {
                    border      = Rectangle.BOX
                    borderColor = BaseColor(245, 196, 179)
                    paddingTop  = 8f; paddingBottom = 8f; paddingLeft = 12f
                }
                cell.addElement(Paragraph(label, fontGray))
                cell.addElement(Paragraph(value, Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, color)))
                return cell
            }

            infoTable.addCell(infoCell("Total Nominal", "Rp ${String.format("%,d", totalNominal).replace(',', '.')}"))
            infoTable.addCell(infoCell("Total Terkumpul", "Rp ${String.format("%,d", totalLunas).replace(',', '.')}", amber))
            doc.add(infoTable)
            doc.add(Chunk.NEWLINE)

            // ── Tabel Data ──
            val table = PdfPTable(6)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(0.5f, 1.5f, 1f, 1.2f, 1f, 1f))

            fun headerCell(text: String): PdfPCell {
                val cell = PdfPCell(Phrase(text, fontHeader)).apply {
                    backgroundColor     = amber
                    border              = Rectangle.BOX
                    paddingTop          = 8f
                    paddingBottom       = 8f
                    paddingLeft         = 6f
                    horizontalAlignment = Element.ALIGN_CENTER
                }
                return cell
            }

            listOf("No", "Penghuni", "Kamar", "Bulan", "Nominal", "Status")
                .forEach { table.addCell(headerCell(it)) }

            list.forEachIndexed { i, tagihan ->
                val isEven   = i % 2 == 0
                val rowColor = if (isEven) white else BaseColor(255, 253, 247)

                fun dataCell(text: String, align: Int = Element.ALIGN_LEFT): PdfPCell {
                    return PdfPCell(Phrase(text, fontNormal)).apply {
                        backgroundColor     = rowColor
                        border              = Rectangle.BOX
                        borderColor         = BaseColor(245, 196, 179)
                        paddingTop          = 6f; paddingBottom = 6f; paddingLeft = 6f
                        horizontalAlignment = align
                    }
                }

                val statusFont = if (tagihan.status == "Sudah Bayar")
                    Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor(59, 109, 17))
                else
                    Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor(163, 45, 45))

                table.addCell(dataCell("${i + 1}", Element.ALIGN_CENTER))
                table.addCell(dataCell(tagihan.namaPenghuni))
                table.addCell(dataCell(tagihan.nomorKamar, Element.ALIGN_CENTER))
                table.addCell(dataCell("${tagihan.bulan} ${tagihan.tahun}"))
                table.addCell(dataCell("Rp ${String.format("%,d", tagihan.nominal).replace(',', '.')}"))

                val statusCell = PdfPCell(Phrase(tagihan.status, statusFont)).apply {
                    backgroundColor     = rowColor
                    border              = Rectangle.BOX
                    borderColor         = BaseColor(245, 196, 179)
                    paddingTop          = 6f; paddingBottom = 6f
                    horizontalAlignment = Element.ALIGN_CENTER
                }
                table.addCell(statusCell)
            }

            doc.add(table)
            doc.add(Chunk.NEWLINE)

            // ── Footer ──
            val footerPara = Paragraph(
                "KosKita — Aplikasi Manajemen Kos Digital  |  Dokumen ini digenerate otomatis",
                Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, gray)
            )
            footerPara.alignment = Element.ALIGN_CENTER
            doc.add(footerPara)

            doc.close()
            stream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}