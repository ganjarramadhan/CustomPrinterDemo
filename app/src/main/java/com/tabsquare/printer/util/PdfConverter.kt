package com.tabsquare.printer.util

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import java.io.File

class PdfConverter(
    context: Context,
    file: File
) {

    var width: Int = 0
    var height: Int = 0
    var bitmap: Bitmap? = null

    init {
        val pdfiumCore = PdfiumCore(context)
        val parcelFileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfDocument = pdfiumCore.newDocument(parcelFileDescriptor)

        pdfiumCore.openPage(pdfDocument, 0)
        val scale = 1.25

        width = (pdfiumCore.getPageWidth(pdfDocument, 0) / scale).toInt()
        height = (pdfiumCore.getPageHeight(pdfDocument, 0) / scale).toInt()
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, 0, 0, 0, width, height)
        pdfiumCore.closeDocument(pdfDocument)
    }
}
