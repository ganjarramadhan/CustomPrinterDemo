package com.tabsquare.printer.demo

import com.tabsquare.log.util.LogPrefs

class LoggerPrefs: LogPrefs {
    override fun getAppEnvironment(): String {
        return "DEVELOPMENT"
    }

    override fun getAppId(): String {
        return "PrinterTest"
    }

    override fun getAppName(): String {
        return "PrinterTest"
    }

    override fun getBrandMerchantKey(): String {
        return "PrinterTest"
    }

    override fun getCorrelationId(): String {
        return ""
    }

    override fun getMerchantKey(): String {
        return "PrinterTest"
    }

    override fun getSessionId(): String {
        return "PrinterTest"
    }

    override fun getTableNumber(): String {
        return "PrinterTest"
    }

    override fun getTerminalId(): String {
        return "PrinterTest"
    }

    override fun getVersionCode(): Long {
        return 1
    }

    override fun getVersionName(): String {
        return "PrinterTest"
    }
}