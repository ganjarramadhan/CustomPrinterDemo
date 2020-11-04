package com.tabsquare.printer.core.request

import java.util.Date

data class OrderHeader(
    var invoiceNumber: String?,
    var checkId: String?,
    var queueNo: String?,
    var billNo: String?,
    var buzzer: String?,
    var date: Date?,
    var orderType: String,
    var host: String?,
    var cashier: String?,
    var area: String?
)
