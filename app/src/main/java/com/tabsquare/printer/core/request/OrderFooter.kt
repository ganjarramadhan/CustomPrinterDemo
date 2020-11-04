package com.tabsquare.printer.core.request

data class OrderFooter(
    var subtotal: Double?,
    var discount: Double?,
    var serviceCharge: Double?,
    var rounding: Double?,
    var totalTax: Double?,
    var cashBack: Double?,
    var taxes: List<Tax>?,
    var grandTotal: Double?
)
