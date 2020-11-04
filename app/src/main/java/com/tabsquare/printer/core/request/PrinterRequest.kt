package com.tabsquare.printer.core.request

data class PrinterRequest(
    var confirmedOrder: Boolean?,
    var printCopyMode: Int? = null,
    var message: String?,
    var restaurant: Restaurant?,
    var paymentDetail: PaymentDetail?,
    var orderHeader: OrderHeader?,
    var orderItems: List<OrderItem>,
    var orderFooter: OrderFooter?,
    var customerDetail: CustomerDetail?,
    var qrDetail: QRDetail?,
    var isRemoveDecimal: Boolean = false,
    var countryId: Int = 0,
    var snCode: String?,
    var minCode: String?,
    var printerTarget: PrinterTarget?,
    var takeAwayInfo: TakeAwayInfo? = null,
    var displayConfig: DisplayConfig? = null
)
