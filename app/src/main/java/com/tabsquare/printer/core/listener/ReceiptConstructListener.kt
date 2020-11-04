package com.tabsquare.printer.core.listener

import android.graphics.Bitmap

interface ReceiptConstructListener {
    fun appendNormalLightText(text: String, alignment: Int)
    fun appendNormalBoldText(text: String, alignment: Int)
    fun appendBigLightText(text: String, alignment: Int)
    fun appendBigBoldText(text: String, alignment: Int)
    fun appendImage(bitmap: Bitmap)
    fun appendQR(qrString: String)
    fun appendLine(line: Int)
    fun cutPaper()
}
