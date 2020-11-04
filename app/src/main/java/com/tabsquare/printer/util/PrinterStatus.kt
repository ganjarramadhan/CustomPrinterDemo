package com.tabsquare.printer.util

/**
 * Created by Ganjar Ramadhan.
 */
sealed class PrinterStatus<out T : Any> {

    class Success<out T : Any>(val data: T) : PrinterStatus<T>()

    class Error(val exception: Throwable) : PrinterStatus<Nothing>() {
        var code: Int = -1
            private set
        var message: String = ""
            private set

        init {
            message = exception.message ?: "Unknown Error"
        }

        constructor(code: Int?, message: String?) : this(Throwable(message)) {
            this.code = code ?: -1
            this.message = message ?: "Unknown Error"
        }
    }
}
