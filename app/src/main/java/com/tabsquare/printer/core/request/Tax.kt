package com.tabsquare.printer.core.request

data class Tax(
    var name: String?,
    var amount: Double?,
    var isInclusive: Boolean
)
