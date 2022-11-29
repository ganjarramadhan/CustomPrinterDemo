package com.tabsquare.printer.demo

import com.tabsquare.printer.core.constant.Country
import com.tabsquare.printer.util.translator.TranslationItem

class PrinterTranslator: com.tabsquare.printer.util.translator.PrinterTranslator {
    override suspend fun getTranslation(item: TranslationItem, country: Country): String {
        return "N/A"
    }
}