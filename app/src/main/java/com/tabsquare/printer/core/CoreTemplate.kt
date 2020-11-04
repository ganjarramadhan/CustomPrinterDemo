package com.tabsquare.printer.core

import android.graphics.Bitmap
import com.tabsquare.printer.core.constant.PrinterConstant
import com.tabsquare.printer.core.constant.PrinterConstant.ADVOCADO
import com.tabsquare.printer.core.constant.PrinterConstant.ALIGNMENT_RIGHT
import com.tabsquare.printer.core.constant.PrinterConstant.CASHAC
import com.tabsquare.printer.core.constant.PrinterConstant.FONT_BIG
import com.tabsquare.printer.core.constant.PrinterConstant.FONT_NORMAL
import com.tabsquare.printer.core.constant.PrinterConstant.WINDCAVE
import com.tabsquare.printer.core.listener.ReceiptConstructListener
import com.tabsquare.printer.core.request.OrderItem
import com.tabsquare.printer.core.request.PrinterRequest
import com.tabsquare.printer.core.request.Restaurant
import com.tabsquare.printer.util.formatCurrency
import com.tabsquare.printer.util.formatDateOnly
import com.tabsquare.printer.util.formatTimeOnly
import com.tabsquare.printer.util.replaceEndWith
import com.tabsquare.printer.util.replaceEndWithInPaymentDetail
import java.util.Locale
import org.apache.commons.text.WordUtils
import timber.log.Timber

abstract class CoreTemplate(
    protected val printerRequest: PrinterRequest,
    protected val maxChar: Int
) {

    private var receiptConstructListener: ReceiptConstructListener? = null
    private var htmlJournalBuilder: MutableList<String> = arrayListOf()

    init {
        htmlJournalBuilder.add("<div align=\"center\" style=\"background:white;font-family:monaco;font-size:13px;\">")
    }

    open fun constructReceipt(receiptConstructListener: ReceiptConstructListener?) {
        this.receiptConstructListener = receiptConstructListener
    }

    /**
     * these two method are for appending normal text to printer command
     * @param text is the text to append
     * @param alignment please refer to PrinterConstant Object
     */

    open fun appendNormalLightText(text: String, alignment: Int) {
        htmlAppendAlign(text, FONT_NORMAL, alignment, false)
        receiptConstructListener?.appendNormalLightText(text, alignment)
    }

    open fun appendNormalBoldText(text: String, alignment: Int) {
        htmlAppendAlign(text, FONT_NORMAL, alignment, true)
        receiptConstructListener?.appendNormalBoldText(text, alignment)
    }

    open fun appendBigLightText(text: String, alignment: Int) {
        htmlAppendAlign(text, FONT_BIG, alignment, false)
        receiptConstructListener?.appendBigLightText(text, alignment)
    }

    open fun appendBigBoldText(text: String, alignment: Int) {
        htmlAppendAlign(text, FONT_BIG, alignment, true)
        receiptConstructListener?.appendBigBoldText(text, alignment)
    }

    open fun appendImage(bitmap: Bitmap) {
        receiptConstructListener?.appendImage(bitmap)
    }

    open fun appendQR(qrString: String) {
        receiptConstructListener?.appendQR(qrString)
    }

    open fun appendLine(line: Int) {
        htmlAppendLine(line)
        receiptConstructListener?.appendLine(line)
    }

    open fun cutPaper() {
        receiptConstructListener?.cutPaper()
    }

    fun buildHtml(): String {
        htmlJournalBuilder.add("</div>")
        Timber.i("HTML Size is ${htmlJournalBuilder.size}")
        var result = ""
        htmlJournalBuilder.forEach {
            result += it
        }
        return result
    }

    fun textRightToCenter(text: String): String {
        val space = maxChar - text.length
        val divider = getDivider(" ", space)
        return divider + text
    }

    fun textLeftToCenter(text: String): String {
        val space = maxChar - text.length
        val divider = getDivider(" ", space)
        return text + divider
    }

    private fun htmlAppendAlign(text: String, textSize: Int, alignment: Int, isBold: Boolean) {
        var finalText = text

        if (alignment == ALIGNMENT_RIGHT) {
            val space = maxChar - finalText.length
            val divider = getDivider(" ", space)
            finalText = divider + finalText
        }

        // prepare to format in html
        finalText = finalText.replace("\n", "<br/>")
        finalText = finalText.replace(" ", "&nbsp;")

        if (isBold) {
            htmlJournalBuilder.add("<p style=\"font-size:${getRealTextSize(textSize)}px;\"><strong>$finalText</strong></p>")
            Timber.i("<p style=\"font-size:${textSize}px;\"><b>$finalText</b></p>")
        } else {
            htmlJournalBuilder.add("<p style=\"font-size:${getRealTextSize(textSize)}px;\">$finalText</p>")
            Timber.i("<p style=\"font-size:${textSize}px;\">$finalText</p>")
        }
    }

    private fun getRealTextSize(size: Int): Int {
        return when (size) {
            FONT_BIG -> {
                26
            }
            FONT_NORMAL -> {
                13
            }
            else -> {
                13
            }
        }
    }

    private fun htmlAppendLine(line: Int) {
        for (i in 1 until line) {
            htmlJournalBuilder.add("<br />")
        }
    }

    fun getRestaurantName(): String {
        return printerRequest.restaurant?.name.orEmpty().toUpperCase(Locale.getDefault())
    }

    fun getQueueNumber(isOverride: Boolean): String {
        var queueNo = ""

        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE && isOverride) {
            if (isOfficialReceipt()) {
                queueNo += "OFFICIAL RECEIPT"
            } else {
                queueNo += "CLAIM CHIT\n"
                queueNo += "NOT OFFICIAL RECEIPT\n\n"
                queueNo += printQueue()
            }
        } else {
            queueNo += printQueue()
        }

        return queueNo
    }

    private fun printQueue(): String {
        var queueNo = ""
        if (printerRequest.displayConfig?.useQueueNumberLabel == true) {
            queueNo += (printerRequest.displayConfig?.queueNumber
                ?: "Queue No.") + "\n" + printerRequest.orderHeader?.queueNo.orEmpty()
        } else {
            queueNo += printerRequest.orderHeader?.queueNo.orEmpty()
        }

        if (printerRequest.customerDetail?.name?.isNotEmpty() == true) {
            queueNo += "\n${printerRequest.customerDetail?.name}"
        }

        if (printerRequest.orderHeader?.buzzer?.isNotEmpty() == true) {
            val buzzerLabel = printerRequest.displayConfig?.buzzerNumber ?: "Buzzer Number"
            queueNo += "\n$buzzerLabel - ${printerRequest.orderHeader?.buzzer}"
        }
        return queueNo
    }

    fun getOrderHeader(): String {
        var orderHeaderToPrint = ""

        // host
        val currentDateTime = printerRequest.orderHeader?.date
        printerRequest.orderHeader?.host?.let {
            val host = "Host: KIOSK $it"
            val todayDate = currentDateTime?.formatDateOnly().orEmpty()
            val dividerHostDate = maxChar - host.length - todayDate.length
            val hostDatePrint = "$host${getDivider(" ", dividerHostDate)}$todayDate\n"
            orderHeaderToPrint += hostDatePrint
        }

        // cashier
        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE) {
            printerRequest.orderHeader?.cashier?.let {
                val cashier = "Cashier: KIOSK $it"
                val dividerCashier = maxChar - cashier.length
                val cashierPrint = "$cashier${getDivider(" ", dividerCashier)}\n"
                orderHeaderToPrint += cashierPrint
            }
        }

        // order number and time
        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE) {
            val currentTime = printerRequest.orderHeader?.date?.formatTimeOnly().orEmpty()
            val dividerTimeDate = maxChar - currentTime.length
            val timePrint = "${getDivider(" ", dividerTimeDate)}$currentTime\n"
            orderHeaderToPrint += timePrint
        } else {
            printerRequest.orderHeader?.billNo?.let {
                val queueNo = it
                val currentTime = currentDateTime?.formatTimeOnly().orEmpty()
                val dividerQueueTimeDate = maxChar - queueNo.length - currentTime.length
                val queueTimePrint =
                    "$queueNo${getDivider(" ", dividerQueueTimeDate)}$currentTime\n"
                orderHeaderToPrint += queueTimePrint
            }
        }

        // check id
        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE) {
            printerRequest.orderHeader?.checkId?.let {
                val checkId = "Check #: $it"
                val dividerCheckId = maxChar - checkId.length
                val checkIdPrint = "$checkId${getDivider(" ", dividerCheckId)}\n"
                orderHeaderToPrint += checkIdPrint
            }
        } else {
            printerRequest.orderHeader?.checkId?.let {
                val checkId = it
                val dividerCheckId = maxChar - checkId.length
                val checkIdPrint = "${getDivider(" ", dividerCheckId)}$checkId\n"
                orderHeaderToPrint += checkIdPrint
            }
        }

        // invoice number
        printerRequest.orderHeader?.invoiceNumber?.let {
            if (it.isNotEmpty()) {
                val invoiceNumber = "Invoice #: $it"
                val dividerInvoiceNumber = maxChar - invoiceNumber.length
                val invoiceNumberPrint = "$invoiceNumber${getDivider(" ", dividerInvoiceNumber)}\n"
                orderHeaderToPrint += invoiceNumberPrint
            }
        }

        // order type
        printerRequest.orderHeader?.orderType?.let {
            val orderType = "Order Type: $it"
            val dividerOrderType = maxChar - orderType.length
            val orderTypePrint = "$orderType${getDivider(" ", dividerOrderType)}\n"
            orderHeaderToPrint += orderTypePrint
        }

        // area
        printerRequest.orderHeader?.area?.let {
            val area = "Area: $it"
            val dividerArea = maxChar - area.length
            val areaPrint = "$area${getDivider(" ", dividerArea)}\n"
            orderHeaderToPrint += areaPrint
        }

        return orderHeaderToPrint
    }

    fun getFooter(): String {
        var orderFooterToPrint = ""

        val takeAwayInfo = printerRequest.takeAwayInfo
        val takeAwayAmount = takeAwayInfo?.takeAwayAmount ?: 0.0

        // add subtotal
        printerRequest.orderFooter?.subtotal?.let {
            val subtotal = if (takeAwayInfo?.asItem == false && takeAwayAmount > 0.0) {
                it - takeAwayAmount
            } else {
                it
            }

            val subtotalLabel = "Subtotal"
            val subtotalValue = subtotal.formatCurrency(isRemoveDecimal())
            val dividerSubtotal = maxChar - subtotalValue.length - subtotalLabel.length
            val subtotalPrint = "$subtotalLabel${getDivider(" ", dividerSubtotal)}$subtotalValue\n"
            orderFooterToPrint += subtotalPrint
        }

        // take away charge if it is hidden / not as an item
        if (takeAwayInfo?.asItem == false && takeAwayAmount > 0.0) {
            val takeAwayChargeLabel = "${printerRequest.orderHeader?.orderType} Charge"
            val takeAwayChargeValue =
                takeAwayAmount.formatCurrency(isRemoveDecimal())
            val dividerSubtotal = maxChar - takeAwayChargeValue.length - takeAwayChargeLabel.length
            val takeAwayChargePrint =
                "$takeAwayChargeLabel${getDivider(" ", dividerSubtotal)}$takeAwayChargeValue\n"
            orderFooterToPrint += takeAwayChargePrint
        }

        // add tax
        var totalTax = 0.0
        printerRequest.orderFooter?.taxes?.forEach { tax ->
            if ((tax.amount ?: 0.0) > 0.0) {
                totalTax += tax.amount ?: 0.0

                var taxLabel = tax.name.orEmpty()
                val isInclusive = tax.isInclusive
                if (isInclusive) {
                    taxLabel += " (Inclusive)"
                }
                val taxAmount =
                    tax.amount?.formatCurrency(isRemoveDecimal()).orEmpty()
                val taxDivider = maxChar - taxLabel.length - taxAmount.length
                val taxToPrint = "$taxLabel${getDivider(" ", taxDivider)}$taxAmount\n"
                orderFooterToPrint += taxToPrint
            }
        }

        // add discount
        val discount = printerRequest.orderFooter?.discount ?: 0.0
        if (discount > 0.0) {
            val discountFormatted = discount.formatCurrency(isRemoveDecimal())
            val discountLabel = "Discount"
            val discountDivider = maxChar - discountFormatted.length - discountLabel.length
            val discountToPrint =
                "$discountLabel${getDivider(" ", discountDivider)}$discountFormatted\n"
            orderFooterToPrint += discountToPrint
        }

        // add service charge
        val serviceCharge = printerRequest.orderFooter?.serviceCharge ?: 0.0
        if (serviceCharge > 0.0) {
            val serviceChargeFormatted =
                serviceCharge.formatCurrency(isRemoveDecimal())
            val serviceChargeLabel = "Service Charge"
            val serviceChargeDivider =
                maxChar - serviceChargeFormatted.length - serviceChargeLabel.length
            val serviceChargeToPrint = "$serviceChargeLabel${getDivider(
                " ",
                serviceChargeDivider
            )}$serviceChargeFormatted\n"
            orderFooterToPrint += serviceChargeToPrint
        }

        // add rounding
        val rounding = printerRequest.orderFooter?.rounding ?: 0.0
        if (rounding != 0.0) {
            val roundingFormatted = rounding.formatCurrency(isRemoveDecimal())
            val roundingLabel = "Rounding"
            val roundingDivider = maxChar - roundingFormatted.length - roundingLabel.length
            val roundingToPrint =
                "$roundingLabel${getDivider(" ", roundingDivider)}$roundingFormatted\n"
            orderFooterToPrint += roundingToPrint
        }

        // cash back
        val cashBack = printerRequest.orderFooter?.cashBack ?: 0.0
        if (cashBack > 0.0) {
            val cashBackFormatted = "-${cashBack.formatCurrency(isRemoveDecimal())}"
            val cashBackLabel = "Cashback Redeemed"
            val cashBackDivider = maxChar - cashBackFormatted.length - cashBackLabel.length
            val cashBackToPrint =
                "$cashBackLabel${getDivider(" ", cashBackDivider)}$cashBackFormatted\n"
            orderFooterToPrint += cashBackToPrint
        }

        return orderFooterToPrint
    }

    fun getDivider(divider: String, length: Int): String {
        var line = divider
        for (i in 1 until length) {
            line += divider
        }
        return line
    }

    fun isRemoveDecimal(): Boolean {
        return printerRequest.isRemoveDecimal
    }

    fun getCountryId(): Int {
        return printerRequest.countryId
    }

    fun getItemSold(): Int {
        var itemSold = 0
        printerRequest.orderItems.forEach { it ->
            if (printerRequest.takeAwayInfo?.takeAwayChargeId != it.dishId) {
                itemSold += it.quantity
            }
        }
        return itemSold
    }

    fun getGrandTotal(): String {
        var grandTotalPrint = ""
        val cashBack = printerRequest.orderFooter?.cashBack ?: 0.0
        printerRequest.orderFooter?.grandTotal?.let {
            val grandTotalLabel = "${printerRequest.orderHeader?.orderType} Total"
            val grandTotalValue = (it - cashBack).formatCurrency(isRemoveDecimal())
            val dividerGrandTotal = maxChar - grandTotalValue.length - grandTotalLabel.length
            grandTotalPrint =
                "$grandTotalLabel${getDivider(" ", dividerGrandTotal)}$grandTotalValue\n"
        }
        return grandTotalPrint
    }

    fun getTenderMedia(): String {
        var tenderToPrint = ""
        val cashBack = printerRequest.orderFooter?.cashBack ?: 0.0
        val grandTotal = printerRequest.orderFooter?.grandTotal ?: 0.0
        if (cashBack < grandTotal) {
            // possible partial redeem
            val amountToPay = grandTotal - cashBack
            printerRequest.paymentDetail?.let {
                val tenderMedia =
                    if (it.acquirerBank.equals(ADVOCADO, true) ||
                        it.paymentType.equals(WINDCAVE, true) ||
                        it.paymentType.equals(CASHAC, true)
                    ) {
                        "${it.paymentType}"
                    } else {
                        if (it.cardPan.isNullOrEmpty()) {
                            "${it.paymentType}"
                        } else {
                            val pan = it.cardPan?.trim()
                            if (pan.isNullOrEmpty() || pan.equals("null", true)) {
                                "${it.paymentType}"
                            } else {
                                "${it.paymentType} (${it.cardPan?.trim()})"
                            }
                        }
                    }
                val grandTotalValue = amountToPay.formatCurrency(isRemoveDecimal())
                val dividerTender = maxChar - grandTotalValue.length - tenderMedia.length
                tenderToPrint = "$tenderMedia${getDivider(" ", dividerTender)}$grandTotalValue\n"
            }
        }

        return tenderToPrint
    }

    fun getCustomerCopy(): String {
        var customerCopy = ""

        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE && isOfficialReceipt()) {

            // print customer copy label
            customerCopy += "*** CUSTOMER COPY ***\n\n"

            // print customer copy detail
            val subtotal = printerRequest.orderFooter?.subtotal ?: 0.0
            val totalWithoutTax = if (printerRequest.orderFooter?.taxes?.size == 1) {
                val tax = printerRequest.orderFooter?.taxes?.get(0)
                if (tax?.isInclusive == true) {
                    subtotal - (tax.amount ?: 0.0)
                } else {
                    subtotal
                }
            } else {
                subtotal
            }

            // print trx detail
            var customerTrxDetail = ""
            customerTrxDetail += "TRANSACTION NO  :                        \n".replaceEndWith(
                printerRequest.orderHeader?.invoiceNumber.orEmpty()
            )
            customerTrxDetail += "VATABLE SALES   :                        \n".replaceEndWith(
                totalWithoutTax.formatCurrency(isRemoveDecimal())
            )
            customerTrxDetail += "VAT AMOUNT      :                        \n".replaceEndWith(
                getTotalTax().formatCurrency(isRemoveDecimal())
            )
            customerTrxDetail += "VAT-EXEMPT SALES:                        \n".replaceEndWith(
                0.0.formatCurrency(
                    isRemoveDecimal()
                )
            )
            customerTrxDetail += "ZERO RATED SALES:                        \n".replaceEndWith(
                0.0.formatCurrency(
                    isRemoveDecimal()
                )
            )

            // appending line
            customerCopy += "$customerTrxDetail\n"

            // print customer detail
            val customer = printerRequest.customerDetail
            var customerDetail = ""
            customerDetail += "CUSTOMER NAME   :                        \n".replaceEndWith(customer?.name.orEmpty())
            customerDetail += "ADDRESS         :                        \n".replaceEndWith(customer?.address.orEmpty())
            customerDetail += "TIN             :                        \n".replaceEndWith(customer?.tin.orEmpty())
            customerDetail += "BUSINESS STYLE  :                        \n".replaceEndWith(customer?.businessType.orEmpty())

            // appending line
            customerCopy += customerDetail
        }

        return customerCopy
    }

    fun getRestaurantAddressAndPhone(restaurant: Restaurant): String {
        var restaurantAddress = restaurant.address.orEmpty()

        if (getCountryId() == PrinterConstant.COUNTRY_PHILIPPINE) {
            restaurantAddress = restaurantAddress
                .replace("**sn**", printerRequest.snCode.orEmpty())
                .replace("**min**", printerRequest.minCode.orEmpty())
                .replace("\r\n", "\n")
                .replace("\r", "\n")
        }

        val wrappedText = WordUtils.wrap(restaurantAddress, maxChar)
        val longAddress =
            wrappedText.split((System.getProperty("line.separator") ?: "\n").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var formattedAddress = ""
        longAddress.forEach {
            formattedAddress += "$it\n"
        }

        var result = formattedAddress

        if (restaurant.phone?.isNotEmpty() == true) {
            result += restaurant.phone + "\n"
        }

        if (restaurant.moreInfo?.isNotEmpty() == true) {
            val wrappedMoreInfoText = WordUtils.wrap(restaurant.moreInfo, maxChar)
            val longMoreInfo = wrappedMoreInfoText.split((System.getProperty("line.separator") ?: "\n").toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            var formattedMoreInfo = ""
            longMoreInfo.forEach {
                formattedMoreInfo += "$it\n"
            }
            result += "\n" + formattedMoreInfo
        }

        return result
    }

    fun getOrderItemsDetail(isKitchenReceipt: Boolean): String {
        var orderItemsToPrint = ""
        printerRequest.orderItems.forEach { item ->
            if (printerRequest.takeAwayInfo?.takeAwayChargeId == item.dishId && printerRequest.takeAwayInfo?.asItem == false) {
                // do nothing
            } else {
                // order item printing
                orderItemsToPrint += formatOrderItemReceipt(item, isKitchenReceipt)

                // option mapping
                val newModifierList = arrayListOf<OrderItem>()
                if (printerRequest.displayConfig?.groupSameOption == true) {
                    // option printing
                    item.modifiers?.forEach { option ->
                        val existingModifier = newModifierList.find {
                            return@find option.modifiersId == it.modifiersId
                        }

                        if (existingModifier == null) {
                            // add new
                            newModifierList.add(option)
                        } else {
                            existingModifier.quantity += option.quantity
                        }
                    }

                    item.modifiers = newModifierList
                }

                // option printing
                item.modifiers?.forEach { option ->
                    orderItemsToPrint += formatOptionReceipt(
                        option,
                        item.quantity,
                        isKitchenReceipt
                    )

                    // combo / nested option printing
                    option.modifiers?.forEach { nestedOption ->
                        val itemPrefix = "${item.quantity} ".length
                        val optionPrefix = "-- ${option.quantity} * ".length
                        val spacePrefix = itemPrefix + optionPrefix
                        orderItemsToPrint += formatFollowingOptionReceipt(
                            nestedOption, multiplier = item.quantity * option.quantity,
                            spacePrefix = spacePrefix, removePrice = isKitchenReceipt
                        )
                    }
                }

                if (item.remarks?.isNotBlank() == true) {
                    val specialRequest = item.remarks.orEmpty()
                    orderItemsToPrint += "  ** $specialRequest\n"
                }
            }
        }
        return orderItemsToPrint
    }

    fun isOfficialReceipt(): Boolean {
        return printerRequest.displayConfig?.officialReceipt ?: false
    }

    private fun formatOrderItemReceipt(orderItem: OrderItem, removePrice: Boolean): String {
        val units: Int = orderItem.quantity
        val sku = if (orderItem.isPrintSku) {
            "[${orderItem.skuName}]"
        } else {
            ""
        }

        val amount = (orderItem.pricePerUnit ?: 0.0) * orderItem.quantity
        val subtotal = if (amount == 0.0 || removePrice) {
            ""
        } else {
            amount.formatCurrency(removeDecimal = isRemoveDecimal())
        }

        val dishToPrint = "${(orderItem.dishName ?: "")} $sku"
        val wrapperLength = maxChar - (units.toString().length + subtotal.length + 2)

        val wrappedText = WordUtils.wrap(dishToPrint, wrapperLength - 2)
        val longDishesName =
            wrappedText.split((System.getProperty("line.separator") ?: "\n").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var result = ""
        longDishesName.forEachIndexed { index: Int, dishNamePerLine: String ->
            var line = dishNamePerLine

            // remove space in beginning if any
            if (line.startsWith(" ", ignoreCase = true)) {
                line = line.removeRange(0, 1)
            }

            if (index == 0) {
                val spaceLength = wrapperLength - line.length - 1
                result += "$units $line ${getDivider(" ", spaceLength)} $subtotal\n"
            } else {
                val firstSpaceLength = units.toString().length
                val endSpaceLength = maxChar - firstSpaceLength - line.length - 2
                result += "${getDivider(" ", firstSpaceLength)} $line ${getDivider(
                    " ",
                    endSpaceLength
                )}\n"
            }
        }

        return result
    }

    private fun formatOptionReceipt(
        option: OrderItem,
        multiplier: Int,
        removePrice: Boolean
    ): String {
        val units: Int = option.quantity * multiplier
        val subtotal = option.totalPrice ?: 0.0
        val subtotalWithFormat = if (subtotal == 0.0 || removePrice) {
            ""
        } else {
            subtotal.formatCurrency(removeDecimal = isRemoveDecimal())
        }

        val sku = if (option.isPrintSku) {
            "[${option.skuName}]"
        } else {
            ""
        }

        val optionToPrint = "${(option.dishName ?: "")} $sku"
        val unitsLength = "${multiplier.toString().length} -- ${option.quantity} * ".length
        val maxOptionCharLength = maxChar - unitsLength - subtotalWithFormat.length - 3

        val wrappedText = WordUtils.wrap(optionToPrint, maxOptionCharLength)
        val lines =
            wrappedText.split((System.getProperty("line.separator") ?: "\n").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var result = ""
        lines.forEachIndexed { index: Int, optionPerLine: String ->
            var line = optionPerLine

            // remove space in beginning if any
            if (line.startsWith(" ", ignoreCase = true)) {
                line = line.removeRange(0, 1)
            }

            if (index == 0) {
                val name = "${getDivider(" ", multiplier.toString().length)} -- $units * $line"
                val spaceLength = maxChar - name.length - subtotalWithFormat.length - 2
                result += "$name ${getDivider(" ", spaceLength)} $subtotalWithFormat\n"
            } else {
                val firstSpaceLength =
                    "${getDivider(" ", multiplier.toString().length)} -- $units *".length
                val endSpaceLength = maxChar - firstSpaceLength - line.length - 2
                result += "${getDivider(" ", firstSpaceLength)} $line ${getDivider(
                    " ",
                    endSpaceLength
                )}\n"
            }
        }

        return result
    }

    private fun formatFollowingOptionReceipt(
        option: OrderItem,
        spacePrefix: Int,
        multiplier: Int,
        removePrice: Boolean
    ): String {
        val units: Int = option.quantity * multiplier
        val subtotal = option.totalPrice ?: 0.0
        val subtotalWithFormat = if (subtotal == 0.0 || removePrice) {
            ""
        } else {
            subtotal.formatCurrency(removeDecimal = isRemoveDecimal())
        }

        val sku = if (option.isPrintSku) {
            "[${option.skuName}]"
        } else {
            ""
        }

        val optionToPrint = "${(option.dishName ?: "")} $sku"
        val unitsLength = " -- ${option.quantity} * ".length
        val maxOptionCharLength =
            maxChar - spacePrefix - unitsLength - subtotalWithFormat.length - 3

        val wrappedText = WordUtils.wrap(optionToPrint, maxOptionCharLength)
        val lines =
            wrappedText.split((System.getProperty("line.separator") ?: "\n").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var result = ""
        lines.forEachIndexed { index: Int, optionPerLine: String ->
            var line = optionPerLine

            // remove space in beginning if any
            if (line.startsWith(" ", ignoreCase = true)) {
                line = line.removeRange(0, 1)
            }

            if (index == 0) {
                val name = "${getDivider(" ", spacePrefix)} -- $units * $line"
                val spaceLength = maxChar - name.length - subtotalWithFormat.length - 2
                result += "$name ${getDivider(" ", spaceLength)} $subtotalWithFormat\n"
            } else {
                val firstSpaceLength = "${getDivider(" ", spacePrefix)} -- $units *".length
                val endSpaceLength = maxChar - firstSpaceLength - line.length - 2
                result += "${getDivider(" ", firstSpaceLength)} $line ${getDivider(
                    " ",
                    endSpaceLength
                )}\n"
            }
        }

        return result
    }

    fun getCashAtCounterText(): String {
        var text = "-- ${printerRequest.displayConfig?.thankYou ?: "Thank You"} --"
        text += "\n-- ${printerRequest.displayConfig?.pleaseProceedPay ?: "Please proceed to pay at counter"} --"
        return text
    }

    fun getPaymentCardDetail(): String {
        val paymentMethod = printerRequest.paymentDetail
        val cashBack = printerRequest.orderFooter?.cashBack ?: 0.0
        val grandTotal = printerRequest.orderFooter?.grandTotal ?: 0.0
        val fullRedeem = cashBack >= grandTotal
        var result = ""
        if (!paymentMethod?.responseText.isNullOrEmpty()) {
            // paymentMethod?.paymentType?.equals(WINDCAVE, true) == true ||
            val dashDivider = getDivider("-", maxChar)
            val cashlessHeader = "-- Cashless Transaction Information --"
            val spaceCashlessHeader = ((maxChar - cashlessHeader.length) / 2)
            val cashlessHeaderPrint =
                "${getDivider(" ", spaceCashlessHeader)}$cashlessHeader${getDivider(
                    " ",
                    spaceCashlessHeader
                )}"
            result += "$cashlessHeaderPrint\n$dashDivider\n"

            if (paymentMethod?.responseWidth == null || paymentMethod.responseWidth == 0) {
                result += "Trx Id          :                        \n".replaceEndWithInPaymentDetail(
                    paymentMethod?.id.orEmpty(),
                    "n/a"
                )

                result += "Trx Ref         :                        \n".replaceEndWithInPaymentDetail(
                    paymentMethod?.txnRef.orEmpty().trim(),
                    "n/a"
                )

                val lines = paymentMethod?.responseText.orEmpty().lines()
                lines.forEach { line ->
                    val spaceDividerLength = (maxChar - line.length) / 2
                    // if (spaceDividerLength > 0) {
                    val spaceDivider = getDivider(" ", spaceDividerLength)
                    val response = "$spaceDivider$line$spaceDivider"
                    result += "$response\n" // paymentMethod?.responseText.orEmpty()
                    // }
                }
            } else {
                val chunkedReceipt =
                    paymentMethod.responseText?.chunked(paymentMethod.responseWidth ?: 0)
                chunkedReceipt?.forEach {
                    result += "$it\n"
                }
            }
            result += dashDivider
            result += "\n\n"
        } else if (paymentMethod?.isCardPayment() == true && !fullRedeem) {
            val dashDivider = getDivider("-", maxChar)
            val cashlessHeader = "-- Cashless Transaction Information --"
            val spaceCashlessHeader = ((maxChar - cashlessHeader.length) / 2)
            val cashlessHeaderPrint =
                "${getDivider(" ", spaceCashlessHeader)}$cashlessHeader${getDivider(
                    " ",
                    spaceCashlessHeader
                )}"
            result += "$cashlessHeaderPrint\n$dashDivider\n"
            result += "Merchant ID     :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.merchantId.orEmpty(),
                "n/a"
            )
            result += "Terminal ID     :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.terminalId.orEmpty(),
                "n/a"
            )
            result += "Trx Id          :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.id.orEmpty(),
                "n/a"
            )
            result += "Trx Time        :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.bankDateTime.orEmpty(),
                "n/a"
            )
            result += "Trx Ref         :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.txnRef.orEmpty().trim(),
                "n/a"
            )
            result += "RRN             :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.rrn.orEmpty(),
                "n/a"
            )
            result += "STAN            :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.stan.orEmpty(),
                "n/a"
            )
            result += "Card Type       :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.cardType.orEmpty(),
                "n/a"
            )
            result += "Card PAN        :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.cardPan.orEmpty().trim(),
                "-"
            )
            result += "Auth Code       :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.authCode.orEmpty(),
                "n/a"
            )
            result += "Status          :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.status.orEmpty(),
                "n/a"
            )
            result += "Surcharge       :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.surcharge.orEmpty(),
                "n/a"
            )
            result += "Result          :                        \n".replaceEndWithInPaymentDetail(
                paymentMethod.result.orEmpty(),
                "n/a"
            )
            result += dashDivider
            result += "\n\n"
        }
        return result
    }

    fun getQRHeader(): String {
        var qrDetail = ""
        val header = printerRequest.qrDetail?.header.orEmpty()
        if (header.isNotBlank()) {
            qrDetail += header
        }
        return qrDetail
    }

    fun getQRFooter(): String {
        var qrDetail = ""
        val footer = printerRequest.qrDetail?.footer.orEmpty()
        if (footer.isNotBlank()) {
            qrDetail += footer
        }
        return qrDetail
    }

    private fun getTotalTax(): Double {
        return printerRequest.orderFooter?.totalTax ?: 0.0
    }

    fun isPrintPaymentDetail(): Boolean {
        return printerRequest.paymentDetail?.isPrintPaymentDetail ?: false
    }

    fun isCashPayment(): Boolean {
        return printerRequest.paymentDetail?.paymentType.equals(PrinterConstant.CASHAC, true)
    }

    fun getCopyFooter(type: Int): String {
        return if (type == 1) {
            "-- Merchant Copy --"
        } else {
            "-- Customer Copy --"
        }
    }
}
