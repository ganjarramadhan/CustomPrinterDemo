package com.tabsquare.printer.core.request

data class TakeAwayInfo(
    var asItem: Boolean = true,
    var takeAwayChargeId: Int = -1,
    var takeAwayAmount: Double = 0.0
)
