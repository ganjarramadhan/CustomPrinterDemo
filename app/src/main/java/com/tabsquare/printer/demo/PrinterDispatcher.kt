package com.tabsquare.printer.demo

import com.tabsquare.printer.util.dispatcher.PrinterDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class PrinterDispatcher: PrinterDispatcherProvider {
    override fun computation(): CoroutineContext {
        return Dispatchers.IO
    }

    override fun io(): CoroutineContext {
        return Dispatchers.IO
    }

    override fun ui(): CoroutineContext {
        return Dispatchers.Main
    }
}