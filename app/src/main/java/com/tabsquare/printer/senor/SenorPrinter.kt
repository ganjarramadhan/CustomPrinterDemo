package com.tabsquare.printer.senor

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat.getSystemService
import com.rt.printerlibrary.bean.UsbConfigBean
import com.rt.printerlibrary.cmd.EscCmd
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.UsbInterface
import com.rt.printerlibrary.enumerate.BarcodeStringPosition
import com.rt.printerlibrary.enumerate.BarcodeType
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.printer.ThermalPrinter
import com.rt.printerlibrary.setting.BarcodeSetting
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.setting.TextSetting
import com.tabsquare.printer.core.CorePrinter
import com.tabsquare.printer.core.CoreTemplate
import com.tabsquare.printer.core.constant.PrinterConstant
import com.tabsquare.printer.util.PdfConverter
import com.tabsquare.printer.util.PrinterStatus
import timber.log.Timber
import java.io.File

class SenorPrinter(
    private val context: Context,
    private val receiptTemplate: CoreTemplate?
) : CorePrinter(context) {

    private var senorVendorId = arrayListOf("1659", "4070", "8137")
    private val rtPrinter = ThermalPrinter()
    private val printerInterface = UsbInterface()
    private val charsetName = "UTF-8"

    override suspend fun openConnection(): PrinterStatus<Int> {
        try {
            val usbManager = getSystemService(context, UsbManager::class.java)
            val deviceList = usbManager?.deviceList
            val senorDevices = deviceList?.values?.filter {
                senorVendorId.contains(it.vendorId.toString())
            }

            if (senorDevices.isNullOrEmpty()) {
                return PrinterStatus.Error(STATUS_NOT_FOUND, "Printer Not Found")
            } else {
                val usbDevice = senorDevices[0]
                val usbConfigBean = UsbConfigBean(context, usbDevice, getPermissionIntent())
                printerInterface.configObject = usbConfigBean
                rtPrinter.setPrinterInterface(printerInterface)
                return if (usbManager.hasPermission(usbConfigBean.usbDevice)) {
                    rtPrinter.connect(usbConfigBean)
                    isConnected = true
                    PrinterStatus.Success(STATUS_CONNECTED)
                } else {
                    usbManager.requestPermission(
                        usbConfigBean.usbDevice,
                        usbConfigBean.pendingIntent
                    )
                    PrinterStatus.Error(STATUS_DISCONNECTED, "Need permission")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error when try to connect to printer")
            return PrinterStatus.Error(STATUS_DISCONNECTED, e.localizedMessage)
        }
    }

    override suspend fun getPrinterStatus(): PrinterStatus<Int> {
        return PrinterStatus.Success(STATUS_AVAILABLE)
    }

    override fun closeConnection() {
        if (rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect()
        }
    }

    override suspend fun printReceipt(): PrinterStatus<Int> {
        try {
            receiptTemplate?.constructReceipt(this)
        } catch (e: java.lang.Exception) {
            return getPrinterException(e)
        }
        return PrinterStatus.Success(STATUS_SUCCESS_PRINT)
    }

    override suspend fun printPdf(file: File): PrinterStatus<Int> {
        try {
            // inserting pdf here
            val pdfConverter = PdfConverter(context, file)
            val bmp = pdfConverter.bitmap
            if (bmp != null) {
                appendImage(bmp)
                appendLine(3)
                cutPaper()
            } else {
                return PrinterStatus.Success(STATUS_FAIL_PRINT)
            }
        } catch (e: java.lang.Exception) {
            return getPrinterException(e)
        }
        return PrinterStatus.Success(STATUS_SUCCESS_PRINT)
    }

    override suspend fun printText(text: String): PrinterStatus<Int> {
        try {
            // inserting text here
            appendNormalLightText(text, PrinterConstant.ALIGNMENT_CENTER)
            appendLine(3)
            cutPaper()
        } catch (e: java.lang.Exception) {
            return getPrinterException(e)
        }

        return PrinterStatus.Success(STATUS_SUCCESS_PRINT)
    }

    override fun getPrintingTemplate(): CoreTemplate? {
        return receiptTemplate
    }

    override fun appendNormalLightText(text: String, alignment: Int) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = charsetName

        // text setting
        val textSetting = TextSetting()
        textSetting.align = getAlignment(alignment)
        textSetting.bold = SettingEnum.Disable
        textSetting.underline = SettingEnum.Disable
        textSetting.isAntiWhite = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Disable
        textSetting.doubleWidth = SettingEnum.Disable

        escCmd.append(escCmd.getTextCmd(textSetting, text))
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendNormalBoldText(text: String, alignment: Int) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = charsetName

        // text setting
        val textSetting = TextSetting()
        textSetting.align = getAlignment(alignment)
        textSetting.bold = SettingEnum.Enable
        textSetting.underline = SettingEnum.Disable
        textSetting.isAntiWhite = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Disable
        textSetting.doubleWidth = SettingEnum.Disable

        escCmd.append(escCmd.getTextCmd(textSetting, text))
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendBigLightText(text: String, alignment: Int) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = charsetName

        // text setting
        val textSetting = TextSetting()
        textSetting.align = getAlignment(alignment)
        textSetting.bold = SettingEnum.Disable
        textSetting.underline = SettingEnum.Disable
        textSetting.isAntiWhite = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Enable
        textSetting.doubleWidth = SettingEnum.Enable

        escCmd.append(escCmd.getTextCmd(textSetting, text))
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendBigBoldText(text: String, alignment: Int) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = charsetName

        // text setting
        val textSetting = TextSetting()
        textSetting.align = getAlignment(alignment)
        textSetting.bold = SettingEnum.Enable
        textSetting.underline = SettingEnum.Disable
        textSetting.isAntiWhite = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Enable
        textSetting.doubleWidth = SettingEnum.Enable

        escCmd.append(escCmd.getTextCmd(textSetting, text))
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendImage(bitmap: Bitmap) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)

        val commonSetting = CommonSetting()
        commonSetting.align = CommonEnum.ALIGN_MIDDLE
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting))

        val bitmapSetting = BitmapSetting()
        bitmapSetting.bmpPrintMode = BmpPrintMode.MODE_MULTI_COLOR

        var bmpPrintWidth = bitmap.width
        if (bmpPrintWidth > 72) {
            bmpPrintWidth = 72
        }
        bitmapSetting.bimtapLimitWidth = bmpPrintWidth * 8
        try {
            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, bitmap))
        } catch (e: SdkException) {
            e.printStackTrace()
        }
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendQR(qrString: String) {
        val cmdFactory: CmdFactory = EscFactory()
        val escCmd = cmdFactory.create()
        escCmd.append(escCmd.headerCmd)

        val barcodeSetting = BarcodeSetting()
        barcodeSetting.barcodeStringPosition = BarcodeStringPosition.BELOW_BARCODE
        barcodeSetting.heightInDot = 72 //accept value:1~255
        barcodeSetting.barcodeWidth = 3 //accept value:2~6
        barcodeSetting.qrcodeDotSize = 5 //accept value: Esc(1~15), Tsc(1~10)

        try {
            escCmd.append(escCmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, qrString))
        } catch (e: SdkException) {
            e.printStackTrace()
        }
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)

        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun appendLine(line: Int) {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.chartsetName = charsetName

        for (i in 0 until line){
            // text setting
            val textSetting = TextSetting()
            textSetting.align = getAlignment(CommonEnum.ALIGN_LEFT)
            textSetting.bold = SettingEnum.Disable
            textSetting.underline = SettingEnum.Disable
            textSetting.isAntiWhite = SettingEnum.Disable
            textSetting.doubleHeight = SettingEnum.Disable
            textSetting.doubleWidth = SettingEnum.Disable

            escCmd.append(escCmd.getTextCmd(textSetting, "<br>"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.headerCmd)
            escCmd.append(escCmd.lfcrCmd)
        }
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    override fun cutPaper() {
        val escCmd = EscCmd()
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.halfCutCmd)
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        rtPrinter.writeMsgAsync(escCmd.appendCmds)
    }

    private fun getPermissionIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context.applicationInfo.packageName),
            0
        )
    }

    private fun getBreakLine(length: Int): String {
        var result = ""
        for (i in 0 until length){
            result += "\n"
        }
        return result
    }

    private fun getAlignment(alignment: Int): Int {
        return when(alignment) {
            PrinterConstant.ALIGNMENT_CENTER -> {
                CommonEnum.ALIGN_MIDDLE
            }
            PrinterConstant.ALIGNMENT_RIGHT -> {
                CommonEnum.ALIGN_RIGHT
            }
            else -> {
                CommonEnum.ALIGN_LEFT
            }
        }
    }
}