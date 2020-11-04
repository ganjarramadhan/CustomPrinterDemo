package com.tabsquare.printer.custom

import android.content.Context
import android.graphics.Bitmap
import com.tabsquare.printer.core.CorePrinter
import com.tabsquare.printer.core.CoreTemplate
import com.tabsquare.printer.core.constant.PrinterConstant
import com.tabsquare.printer.util.PdfConverter
import com.tabsquare.printer.util.PrinterStatus
import it.custom.printer.api.android.CustomAndroidAPI
import it.custom.printer.api.android.CustomException
import it.custom.printer.api.android.CustomException.ERR_DATABARCODE
import it.custom.printer.api.android.CustomException.ERR_DATANOTAVAILABLE
import it.custom.printer.api.android.CustomException.ERR_DEVICENOTRECOGNIZED
import it.custom.printer.api.android.CustomException.ERR_DEVICENOTSUPPORTED
import it.custom.printer.api.android.CustomException.ERR_GENERIC
import it.custom.printer.api.android.CustomException.ERR_INITCOMMUNICATIONERROR
import it.custom.printer.api.android.CustomException.ERR_PRINTERCOMMUNICATIONERROR
import it.custom.printer.api.android.CustomException.ERR_PRINTERNOTCONNECTED
import it.custom.printer.api.android.CustomException.ERR_UNSUPPORTEDFUNCTION
import it.custom.printer.api.android.CustomException.ERR_WRONGPARAMETERVALUE
import it.custom.printer.api.android.CustomException.ERR_WRONGPICTURE
import it.custom.printer.api.android.CustomException.ERR_WRONGPRINTERFONT
import it.custom.printer.api.android.CustomPrinter
import it.custom.printer.api.android.PrinterFont
import java.io.File
import timber.log.Timber

class TabsquareCustomPrinter(
    private val context: Context,
    private val receiptTemplate: CoreTemplate?
) : CorePrinter(context) {

    private var mPrinter: CustomPrinter? = null

    override suspend fun openConnection(): PrinterStatus<Int> {
        try {
            // Get the list of devices
            val usbDeviceList = CustomAndroidAPI.EnumUsbDevices(context)
            if (usbDeviceList == null || usbDeviceList.isEmpty()) {
                // Show Error
                return getPrinterException(ERROR_NOT_FOUND)
            } else {
                // Open and connect it
                mPrinter = CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList[0], context)
                isConnected = true
                return PrinterStatus.Success(STATUS_CONNECTED)
            }
        } catch (e: CustomException) {
            // Show Error
            return getPrinterException(getTabsquarePtinterErrorCode(e))
        } catch (e: Exception) {
            // Show Error
            return getPrinterException(e)
        }
    }

    override suspend fun getPrinterStatus(): PrinterStatus<Int> {
        if (!isConnected) {
            return getPrinterException(STATUS_DISCONNECTED)
        }

        try {
            // Get printer Status
            val printerStatus = mPrinter?.printerFullStatus
            val errorCode = if (printerStatus?.stsCUTERROR == true) {
                STATUS_AUTO_CUTTER_ERROR
            } else if (printerStatus?.stsNOPAPER == true) {
                STATUS_PAPER_EMPTY
            } else if (printerStatus?.stsNEARENDPAP == true) {
                STATUS_PAPER_NEAR_END
            } else if (printerStatus?.stsPAPERJAM == true) {
                STATUS_PAPER_FEED_ERROR
            } else {
                return getPrinterException(ERROR_UNKNOWN)
            }
            return PrinterStatus.Success(errorCode)
        } catch (e: CustomException) {
            return getPrinterException(getTabsquarePtinterErrorCode(e))
        } catch (e: java.lang.Exception) {
            return PrinterStatus.Error(e)
        }
    }

    override fun closeConnection() {
        Thread(Runnable {
            try {
//                mPrinter?.clearReadBuffer()
//                mPrinter?.scannerCleanBuffers()
                mPrinter?.close()
            } catch (e: CustomException) {
                // Show Error
                Timber.e(e, "Custom printer error when close connection")
            } catch (e: Exception) {
                Timber.e(e, "Custom printer UNKNOWN error when close connection")
            }
        })
    }

    override suspend fun printReceipt(): PrinterStatus<Int> {
        if (mPrinter == null) {
            return getPrinterException(STATUS_UNINITIALIZED)
        }

        try {
            receiptTemplate?.constructReceipt(this)
        } catch (e: java.lang.Exception) {
            return getPrinterException(e)
        }

        return PrinterStatus.Success(STATUS_SUCCESS_PRINT)
    }

    override suspend fun printPdf(file: File): PrinterStatus<Int> {
        if (mPrinter == null) {
            return getPrinterException(STATUS_UNINITIALIZED)
        }

        if (!isConnected) {
            return getPrinterException(STATUS_DISCONNECTED)
        }

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
        if (mPrinter == null) {
            return getPrinterException(STATUS_UNINITIALIZED)
        }

        if (!isConnected) {
            return getPrinterException(STATUS_DISCONNECTED)
        }

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
        appendCommand(text, PrinterConstant.FONT_NORMAL, alignment, false)
    }

    override fun appendNormalBoldText(text: String, alignment: Int) {
        appendCommand(text, PrinterConstant.FONT_NORMAL, alignment, true)
    }

    override fun appendBigLightText(text: String, alignment: Int) {
        appendCommand(text, PrinterConstant.FONT_BIG, alignment, false)
    }

    override fun appendBigBoldText(text: String, alignment: Int) {
        appendCommand(text, PrinterConstant.FONT_BIG, alignment, true)
    }

    override fun appendImage(bitmap: Bitmap) {
        val scaleBitmap = resize(bitmap, 480, 480)
        mPrinter?.printImage(
            scaleBitmap,
            CustomPrinter.IMAGE_ALIGN_TO_CENTER,
            CustomPrinter.IMAGE_SCALE_TO_WIDTH,
            scaleBitmap.width
        )
    }

    override fun appendQR(qrString: String) {
        mPrinter?.printBarcode2D(
            qrString,
            CustomPrinter.BARCODE_TYPE_QRCODE,
            CustomPrinter.BARCODE_ALIGN_TO_CENTER,
            200
        )
    }

    override fun appendLine(line: Int) {
        mPrinter?.feed(line)
    }

    override fun cutPaper() {
        try {
            mPrinter?.feed(3)

            val name = mPrinter?.printerName
            if (name.equals("Custom K80", false)) {
                // partial cut
                val intArray = IntArray(2)
                intArray[0] = 27
                intArray[1] = 109
                mPrinter?.writeData(intArray)
            } else {
                mPrinter?.cut(CustomPrinter.CUT_TOTAL)
                mPrinter?.eject(CustomPrinter.EJ_RETRACT)
                mPrinter?.present(40)
            }
        } catch (e: Exception) {
            Timber.e("Error when try to eject and present")
        }
    }

    private fun appendCommand(text: String, textSize: Int, alignment: Int, isBold: Boolean) {

        if (text.isNullOrEmpty()) return

        try {
            val printerFont = PrinterFont()

            val size = if (textSize == PrinterConstant.FONT_BIG) {
                PrinterFont.FONT_SIZE_X2
            } else {
                PrinterFont.FONT_SIZE_X1
            }

            printerFont.charFontType = PrinterFont.FONT_TYPE_A
            val name = mPrinter?.printerName
            if (name.equals("Custom K80", false)) {
                printerFont.lineSpacing = 32
            }
            printerFont.charWidth = size
            printerFont.charHeight = size
            printerFont.underline = false
            printerFont.italic = false
            printerFont.justification = PrinterFont.FONT_JUSTIFICATION_CENTER
            printerFont.internationalCharSet = PrinterFont.FONT_CS_DEFAULT
            printerFont.emphasized = isBold

            var finalText = text
            if (alignment == PrinterConstant.ALIGNMENT_RIGHT) {
                finalText = receiptTemplate?.textRightToCenter(text).orEmpty()
            } else if (alignment == PrinterConstant.ALIGNMENT_LEFT) {
                finalText = receiptTemplate?.textLeftToCenter(text).orEmpty()
            }

            mPrinter?.printText(finalText, printerFont)
        } catch (e: Exception) {
            Timber.e("Error when print text")
        }
    }

    private fun getTabsquarePtinterErrorCode(e: CustomException): Int {
        return when (e.GetErrorCode().toInt()) {
            ERR_GENERIC -> {
                ERROR_UNKNOWN
            }
            ERR_WRONGPICTURE -> {
                ERROR_PARAM
            }
            ERR_WRONGPARAMETERVALUE -> {
                ERROR_PARAM
            }
            ERR_PRINTERNOTCONNECTED -> {
                ERROR_DISCONNECTED
            }
            ERR_PRINTERCOMMUNICATIONERROR -> {
                ERROR_FAILURE
            }
            ERR_WRONGPRINTERFONT -> {
                ERROR_PARAM
            }
            ERR_DEVICENOTSUPPORTED -> {
                ERROR_UNSUPPORTED
            }
            ERR_DEVICENOTRECOGNIZED -> {
                ERROR_UNSUPPORTED
            }
            ERR_INITCOMMUNICATIONERROR -> {
                ERROR_FAILURE
            }
            ERR_UNSUPPORTEDFUNCTION -> {
                ERROR_UNSUPPORTED
            }
            ERR_DATABARCODE -> {
                ERROR_PARAM
            }
            ERR_DATANOTAVAILABLE -> {
                ERROR_PARAM
            }
            else -> {
                ERROR_UNKNOWN
            }
        }
    }
}
