package com.tabsquare.printer

import androidx.appcompat.app.AppCompatActivity
import com.tabsquare.printer.core.CorePrinter
import com.tabsquare.printer.core.request.PrinterRequest
import com.tabsquare.printer.custom.TabsquareCustomPrinter
import com.tabsquare.printer.senor.SenorPrinter
import com.tabsquare.printer.templates.receipt.DefaultReceiptTemplate

object PrinterManager {

    fun createReceiptPrinter(
        activity: AppCompatActivity,
        printerRequest: PrinterRequest
    ): CorePrinter {
        val receiptTemplate = DefaultReceiptTemplate(printerRequest, maxChar = 41)
        // return TabsquareCustomPrinter(context = activity, receiptTemplate = receiptTemplate)
        return SenorPrinter(context = activity, receiptTemplate = receiptTemplate)
    }
}
