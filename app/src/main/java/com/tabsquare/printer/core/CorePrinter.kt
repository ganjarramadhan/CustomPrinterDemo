package com.tabsquare.printer.core

import android.content.Context
import android.graphics.Bitmap
import com.tabsquare.printer.core.listener.ReceiptConstructListener
import com.tabsquare.printer.util.PrinterStatus
import java.io.File

abstract class CorePrinter(private val context: Context) : ReceiptConstructListener {

    protected var isConnected = false

    abstract suspend fun openConnection(): PrinterStatus<Int>
    abstract suspend fun getPrinterStatus(): PrinterStatus<Int>
    abstract fun closeConnection()
    abstract suspend fun printReceipt(): PrinterStatus<Int>
    abstract suspend fun printPdf(file: File): PrinterStatus<Int>
    abstract suspend fun printText(text: String): PrinterStatus<Int>
    abstract fun getPrintingTemplate(): CoreTemplate?

    companion object {
        const val STATUS_AVAILABLE = 0
        const val STATUS_INITIALIZED = 1
        const val STATUS_UNINITIALIZED = 2
        const val STATUS_CONNECTED = 3
        const val STATUS_DISCONNECTED = 4
        const val STATUS_OFFLINE = 5
        const val STATUS_NO_RESPONSE = 6
        const val STATUS_COVER_OPEN = 7
        const val STATUS_PAPER_EMPTY = 8
        const val STATUS_PAPER_FEED_ERROR = 9
        const val STATUS_NOT_RECOVER_ERROR = 10
        const val STATUS_AUTO_RECOVER_ERROR = 11
        const val STATUS_HEAD_OVERHEAT = 12
        const val STATUS_MOTOR_OVERHEAT = 13
        const val STATUS_BATTERY_OVERHEAT = 14
        const val STATUS_WRONG_PAPER = 15
        const val STATUS_BATTERY_0 = 16
        const val STATUS_PAPER_NEAR_END = 17
        const val STATUS_BATTERY_LOW = 18
        const val STATUS_MECHANICAL_ERROR = 19
        const val STATUS_AUTO_CUTTER_ERROR = 20
        const val STATUS_UNKNOWN = 21
        const val STATUS_PRINTING = 22
        const val STATUS_SUCCESS_PRINT = 23
        const val STATUS_FAIL_PRINT = 24

        const val ERROR_CONNECT = 400
        const val ERROR_PARAM = 401
        const val ERROR_TIMEOUT = 402
        const val ERROR_MEMORY = 403
        const val ERROR_ILLEGAL = 404
        const val ERROR_PROCESSING = 405
        const val ERROR_NOT_FOUND = 406
        const val ERROR_IN_USE = 407
        const val ERROR_TYPE_INVALID = 408
        const val ERROR_DISCONNECTED = 409
        const val ERROR_ALREADY_OPEN = 410
        const val ERROR_ALREADY_USED = 411
        const val ERROR_BOX_COUNT_OVER = 412
        const val ERROR_BOX_CLIENT_OVER = 413
        const val ERROR_UNSUPPORTED = 414
        const val ERROR_FAILURE = 415
        const val ERROR_UNKNOWN = 416

        private fun getErrorMessage(errorCode: Int): String {
            return when (errorCode) {
                ERROR_CONNECT -> {
                    "Error when try to connect to printer"
                }
                ERROR_PARAM -> {
                    "Bad Param sent to printer command"
                }
                ERROR_TIMEOUT -> {
                    "Timeout"
                }
                ERROR_MEMORY -> {
                    "Error Memory"
                }
                ERROR_ILLEGAL -> {
                    "Illegal State, please make sure using printer api properly"
                }
                ERROR_PROCESSING -> {
                    "Error when processing data"
                }
                ERROR_NOT_FOUND -> {
                    "Printer not found!"
                }
                ERROR_IN_USE -> {
                    "Printer is in use"
                }
                ERROR_TYPE_INVALID -> {
                    "Printer type is invalid"
                }
                ERROR_DISCONNECTED -> {
                    "Error when try to disconnect from device"
                }
                ERROR_ALREADY_OPEN -> {
                    "Connection already open"
                }
                ERROR_ALREADY_USED -> {
                    "Printer already used"
                }
                ERROR_BOX_COUNT_OVER -> {
                    "Error box count over"
                }
                ERROR_BOX_CLIENT_OVER -> {
                    "Error box client over"
                }
                ERROR_UNSUPPORTED -> {
                    "Error printer unsupported"
                }
                ERROR_FAILURE -> {
                    "Failure"
                }

                STATUS_AVAILABLE -> {
                    "Printer Available"
                }
                STATUS_INITIALIZED -> {
                    "PRinter Initialized"
                }
                STATUS_UNINITIALIZED -> {
                    "Printer Uninitialized"
                }
                STATUS_CONNECTED -> {
                    "Printer Connected"
                }
                STATUS_DISCONNECTED -> {
                    "Printer Disconnected"
                }
                STATUS_OFFLINE -> {
                    "Printer Offline"
                }
                STATUS_NO_RESPONSE -> {
                    "No Response from Printer"
                }
                STATUS_COVER_OPEN -> {
                    "Printer Cover Open"
                }
                STATUS_PAPER_EMPTY -> {
                    "Paper Empty"
                }
                STATUS_PAPER_FEED_ERROR -> {
                    "Paper Feed Error"
                }
                STATUS_NOT_RECOVER_ERROR -> {
                    "Not Recover Error"
                }
                STATUS_AUTO_RECOVER_ERROR -> {
                    "Auto Recover Error"
                }
                STATUS_HEAD_OVERHEAT -> {
                    "Head Overheat"
                }
                STATUS_MOTOR_OVERHEAT -> {
                    "Motor Overheat"
                }
                STATUS_BATTERY_OVERHEAT -> {
                    "Battery Overheat"
                }
                STATUS_WRONG_PAPER -> {
                    "Wrong Paper"
                }
                STATUS_BATTERY_0 -> {
                    "Battery Empty"
                }
                STATUS_PAPER_NEAR_END -> {
                    "Paper Near End"
                }
                STATUS_BATTERY_LOW -> {
                    "Battery Low"
                }
                STATUS_MECHANICAL_ERROR -> {
                    "Mechanical Error"
                }
                STATUS_AUTO_CUTTER_ERROR -> {
                    "Auto Cut Error"
                }
                STATUS_PRINTING -> {
                    "Printing"
                }
                STATUS_SUCCESS_PRINT -> {
                    "Print Success"
                }
                STATUS_FAIL_PRINT -> {
                    "Print Failed"
                }
                else -> {
                    "Status Unknown"
                }
            }
        }
    }

    fun getPrinterException(errorCode: Int): PrinterStatus.Error {
        return PrinterStatus.Error(errorCode, getErrorMessage(errorCode))
    }

    fun getPrinterException(exception: Exception): PrinterStatus.Error {
        return PrinterStatus.Error(exception)
    }

    fun resize(imageSrc: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image: Bitmap = imageSrc
        return if (image.height > maxHeight && image.width > maxWidth) {
            val width: Int = image.width
            val height: Int = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }
}
