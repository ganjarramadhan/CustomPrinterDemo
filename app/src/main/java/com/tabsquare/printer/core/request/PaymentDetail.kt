package com.tabsquare.printer.core.request

import com.tabsquare.printer.core.constant.PrinterConstant.ADVOCADO
import com.tabsquare.printer.core.constant.PrinterConstant.QR_GO_PAY
import com.tabsquare.printer.core.constant.PrinterConstant.QR_GRAB_PAY
import com.tabsquare.printer.core.constant.PrinterConstant.QR_NETS
import com.tabsquare.printer.core.constant.PrinterConstant.QR_STRIPE
import com.tabsquare.printer.core.constant.PrinterConstant.WINDCAVE

data class PaymentDetail(
    var acquirerBank: String?,
    var paymentType: String?,
    var isPrintPaymentDetail: Boolean?,
    var stan: String?,
    var merchantId: String?,
    var terminalId: String?,
    var bankDateTime: String?,
    var txnRef: String?,
    var cardPan: String?,
    var cardType: String?,
    var authCode: String?,
    var status: String?,
    var id: String?,
    var result: String?,
    var rrn: String?,
    var surcharge: String?,
    var responseText: String?,
    var responseWidth: Int?
) {

    fun isCardPayment(): Boolean {
        return !(paymentType?.equals(QR_GO_PAY, ignoreCase = true) == true ||
            paymentType?.equals(QR_GRAB_PAY, ignoreCase = true) == true ||
            paymentType?.equals(QR_NETS, ignoreCase = true) == true ||
            paymentType?.equals(QR_STRIPE, ignoreCase = true) == true ||
            acquirerBank?.equals(ADVOCADO, true) == true ||
            paymentType?.equals(WINDCAVE, ignoreCase = true) == true
            )
    }
}
