package com.tabsquare.printer.core.request

data class DisplayConfig(
    var useQueueNumberLabel: Boolean = false,
    var groupSameOption: Boolean = false,
    var queueNumber: String? = null,
    var buzzerNumber: String? = null,
    var officialReceipt: Boolean = false,
    var thankYou: String? = null,
    var pleaseProceedPay: String? = null
)
