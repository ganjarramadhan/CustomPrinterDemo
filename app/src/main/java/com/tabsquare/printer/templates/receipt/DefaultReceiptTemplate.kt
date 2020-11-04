package com.tabsquare.printer.templates.receipt

import com.tabsquare.printer.core.CoreTemplate
import com.tabsquare.printer.core.constant.PrinterConstant.ALIGNMENT_CENTER
import com.tabsquare.printer.core.constant.PrinterConstant.ALIGNMENT_LEFT
import com.tabsquare.printer.core.constant.PrinterConstant.COUNTRY_PHILIPPINE
import com.tabsquare.printer.core.listener.ReceiptConstructListener
import com.tabsquare.printer.core.request.PrinterRequest

class DefaultReceiptTemplate(
    printerRequest: PrinterRequest,
    maxChar: Int
) : CoreTemplate(printerRequest, maxChar) {

    override fun constructReceipt(receiptConstructListener: ReceiptConstructListener?) {
        super.constructReceipt(receiptConstructListener)

        val numberOfCopy =
            if (printerRequest.printCopyMode == 0) {
                1
            } else if (printerRequest.printCopyMode == 1) {
                // always print copy
                2
            } else if (printerRequest.printCopyMode == 2 && printerRequest.confirmedOrder == false) {
                // print copy if confirm order fail
                2
            } else {
                1
            }

        for (i in 0 until numberOfCopy) {
            // check if confirm order success
            if (printerRequest.confirmedOrder == false) {
                appendNormalLightText(getDivider("*", maxChar) + "\n", ALIGNMENT_CENTER)
                appendBigBoldText("Problem when sending\n", ALIGNMENT_CENTER)
                appendBigBoldText("your order to kitchen.\n", ALIGNMENT_CENTER)
                appendBigBoldText("Please contact staff for\n", ALIGNMENT_CENTER)
                appendBigBoldText("further assistance\n", ALIGNMENT_CENTER)
                appendNormalLightText(getDivider("*", maxChar), ALIGNMENT_CENTER)
                appendLine(4)
            }

            // get restaurant top image
            printerRequest.restaurant?.topImage?.let {
                appendImage(it)
                appendLine(2)
            }

            // get restaurant name
            appendNormalBoldText(getRestaurantName(), ALIGNMENT_CENTER)

            // append line
            appendLine(2)

            // get restaurant address
            printerRequest.restaurant?.let {
                appendNormalLightText(getRestaurantAddressAndPhone(it), ALIGNMENT_CENTER)
            }

            // append line
            appendLine(3)

            // get queue no
            appendBigBoldText(getQueueNumber(true), ALIGNMENT_CENTER)

            // append line
            appendLine(3)

            // order header
            appendNormalLightText(getOrderHeader(), ALIGNMENT_LEFT)

            // appending line
            appendLine(1)

            // order detail
            val orderItemDetail = getOrderItemsDetail(isKitchenReceipt = false)
            appendNormalLightText(orderItemDetail, ALIGNMENT_LEFT)

            // appending line
            appendLine(1)

            // footer
            appendNormalLightText(getFooter(), ALIGNMENT_LEFT)

            // appending line
            appendLine(1)

            // add item sold
            val itemSoldToPrint = "Item Sold: ${getItemSold()}"
            appendNormalLightText(itemSoldToPrint, ALIGNMENT_CENTER)

            // appending line
            appendLine(2)

            // add grand total
            appendNormalBoldText(getGrandTotal(), ALIGNMENT_CENTER)

            if (isCashPayment() && getCountryId() == COUNTRY_PHILIPPINE) {
                // do not print tender media
            } else {
                // appending line
                appendLine(1)

                // add tender media
                appendNormalLightText(getTenderMedia(), ALIGNMENT_CENTER)
            }

            // add payment cashless detail if needed
            if (isCashPayment()) {
                // appending line
                appendLine(2)
                appendNormalLightText(getCashAtCounterText(), ALIGNMENT_CENTER)
            } else {
                if (isPrintPaymentDetail()) {
                    // appending line
                    appendLine(2)

                    val paymentCardDetail = getPaymentCardDetail()
                    appendNormalLightText(paymentCardDetail, ALIGNMENT_CENTER)
                }
            }

            // appending line
            appendLine(1)

            // add customer copy if needed
            if (getCustomerCopy().isNotEmpty()) {
                appendNormalLightText(getCustomerCopy(), ALIGNMENT_CENTER)

                // appending line
                appendLine(6)
            }

            // add QR Header
            val qrHeader = getQRHeader()
            if (qrHeader.isNotEmpty()) {
                appendNormalLightText(qrHeader, ALIGNMENT_CENTER)
                appendLine(2)
            }

            // add qr body
            val qrBody = printerRequest.qrDetail?.body
            if (!qrBody.isNullOrEmpty()) {
                appendQR(qrBody)
                appendLine(2)
            }

            // add qr footer
            val qrFooter = getQRFooter()
            if (qrFooter.isNotEmpty()) {
                appendNormalLightText(qrFooter, ALIGNMENT_CENTER)
                appendLine(2)
            }

            appendLine(2)

            // add bottom image
            printerRequest.restaurant?.bottomImage?.let {
                appendImage(it)
                appendLine(4)
            }

            // add merchant or customer copy
            if (numberOfCopy == 2) {
                if (i == 0) {
                    // merchant copy
                    appendNormalLightText(getCopyFooter(1), ALIGNMENT_CENTER)
                } else {
                    // customer copy
                    appendNormalLightText(getCopyFooter(2), ALIGNMENT_CENTER)
                }

                // appending line
                appendLine(4)
            }

            // print error message if any
            if (printerRequest.confirmedOrder == false) {
                if (!printerRequest.message.isNullOrEmpty()) {
                    val divider = getDivider("*", maxChar)
                    val error = "$divider\n${printerRequest.message}\n$divider"
                    appendNormalLightText(error, ALIGNMENT_CENTER)
                }

                // appending line
                appendLine(4)
            }

            // cut paper
            cutPaper()

            // add order number detachable
            if (getCountryId() == COUNTRY_PHILIPPINE && isOfficialReceipt()) {
                // appending line
                appendLine(2)

                appendBigBoldText(getQueueNumber(false), ALIGNMENT_CENTER)

                // appending line
                appendLine(4)

                // cut paper
                cutPaper()
            }

        }
    }
}
