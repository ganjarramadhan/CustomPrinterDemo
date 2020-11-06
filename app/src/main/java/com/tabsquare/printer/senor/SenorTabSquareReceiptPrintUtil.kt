package com.tabsquare.printer.senor
/*

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.webkit.URLUtil
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.TextSetting
import com.tabsquare.core.repository.entity.BillEntity
import com.tabsquare.core.repository.entity.EmenuSetting
import com.tabsquare.core.repository.entity.PaymentMethodEntity
import com.tabsquare.core.util.extension.formatCurrency
import com.tabsquare.core.util.extension.formatDate
import com.tabsquare.core.util.extension.getOrderType
import com.tabsquare.core.util.extension.toTabSquareUriFile
import com.tabsquare.core.util.preferences.AppsPreferences
import com.tabsquare.core.util.printer.base.ReceiptPrinter
import java.io.File
import java.util.*

class SenorTabSquareReceiptPrintUtil(private val activity: AppCompatActivity,
                                     private val bill: BillEntity?,
                                     private val paymentMethod: PaymentMethodEntity?,
                                     private val appsPreferences: AppsPreferences,
                                     private val settings: List<EmenuSetting>,
                                     private val printer: RTPrinter<*>) : ReceiptPrinter(activity) {

    private val escFac = EscFactory()
    private val escCmd = escFac.create()

    var isRemoveDecimal = false
    var isConfirmOrderFailed = false
    var isQueueMode = false

    init {
        escCmd.chartsetName = "UTF-8"
    }

    fun printReceipt() {

        // check confirm order failed
        printErrorConfirmOrderMessage()

        // header
        addRestaurantName()
        addRestaurantAddress()
        addOrderNumber()

        // order detail
        addLineDivider()
        addOrderDetailHeader()
        addLineDivider()

        // add order items
        addOrderedItems()
        addLineDivider()

        // add bill detail
        addSubTotal()
        addGrandTotal()

        // add footer
        addFooterText()

        // close and cut
        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.lfcrCmd)

        // check confirm order failed
        printErrorConfirmOrderMessage()

        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.lfcrCmd)

        escCmd.append(escCmd.headerCmd)
        escCmd.append(escCmd.allCutCmd)

        printer.writeMsg(escCmd.appendCmds)

    }

    private fun printErrorConfirmOrderMessage() {
        if (isConfirmOrderFailed) {
            addStarDivider()
            addNoteText("Your order is not sent to POS.")
            addNoteText("Please bring this receipt to cashier to get your order proceed. ")
            addNoteText("We are sorry for the disappointment caused.")
            addStarDivider()
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.lfcrCmd)
        }
    }

    private fun addRestaurantName() {
        // check image
        val restaurantSetting = bill?.billInfo?.restaurant
        restaurantSetting?.restaurantImage.let {
            val bitmapSetting = BitmapSetting()
            bitmapSetting.bimtapLimitWidth = 48//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
            val fileName = URLUtil.guessFileName(it, null, null)
            val file = File(fileName.toTabSquareUriFile())
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(fileName.toTabSquareUriFile())
                escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)))
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.lfcrCmd)
            }
        }

        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Enable
        textSetting.doubleWidth = SettingEnum.Enable
        textSetting.escFontType = ESCFontTypeEnum.FONT_A_12x24

        escCmd.append(escCmd.getTextCmd(textSetting, restaurantSetting?.name ?: "Restaurant Name"))
        escCmd.append(escCmd.lfcrCmd)
    }

    private fun addRestaurantAddress() {
        val restaurantSetting = bill?.billInfo?.restaurant

        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16
        escCmd.append(escCmd.getTextCmd(textSetting, restaurantSetting?.description
                ?: "Restaurant Address"))
        escCmd.append(escCmd.lfcrCmd)

        if (restaurantSetting?.phone?.isNotBlank() == true) {
            escCmd.append(escCmd.getTextCmd(textSetting, restaurantSetting.phone
                    ?: "Restaurant Phone"))
            escCmd.append(escCmd.lfcrCmd)
        }

        if (restaurantSetting?.moreInfo?.isNotBlank() == true) {
            escCmd.append(escCmd.getTextCmd(textSetting, restaurantSetting.moreInfo
                    ?: "Restaurant Info"))
            escCmd.append(escCmd.lfcrCmd)
        }

    }

    private fun addOrderNumber() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.doubleHeight = SettingEnum.Enable
        textSetting.doubleWidth = SettingEnum.Enable
        textSetting.escFontType = ESCFontTypeEnum.FONT_A_12x24

        escCmd.append(escCmd.lfcrCmd)
        if (isQueueMode) {
            escCmd.append(escCmd.getTextCmd(textSetting, "Queue Number - ${bill?.queueNo}"))
        } else {
            escCmd.append(escCmd.getTextCmd(textSetting, "Order Number - ${bill?.queueNo}"))
        }
        escCmd.append(escCmd.lfcrCmd)
    }

    private fun addLineDivider() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Disable
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"))
        escCmd.append(escCmd.lfcrCmd)
    }

    private fun addStarDivider() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Disable
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.getTextCmd(textSetting, "************************************************"))
        escCmd.append(escCmd.lfcrCmd)
    }

    private fun addOrderDetailHeader() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_LEFT
        textSetting.bold = SettingEnum.Disable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        escCmd.append(escCmd.getTextCmd(textSetting, "Order Type: ${appsPreferences.orderType.getOrderType()}"))
        escCmd.append(escCmd.lfcrCmd)
        escCmd.append(escCmd.getTextCmd(textSetting, "Date & Time: ${Date().formatDate()}"))

    }

    private fun addOrderedItems() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_BOTH_SIDES
        textSetting.bold = SettingEnum.Disable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        bill?.billInfo?.orderItems?.forEachIndexed { index, orderItem ->
            if (index > 0) escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, formatDishReceipt(orderItem)))
            orderItem.item?.customizationOptions?.forEach { option ->
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(textSetting, formatOption(option, orderItem.units
                        ?: 1)))
            }
            if (orderItem.item?.remarks?.isNotBlank() == true) {
                val specialRequest = orderItem.item?.remarks ?: ""
                escCmd.append(escCmd.lfcrCmd)
                escCmd.append(escCmd.getTextCmd(textSetting, "  ** $specialRequest"))
            }

        }
    }

    private fun addSubTotal() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_BOTH_SIDES
        textSetting.bold = SettingEnum.Disable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        val space = createSpace(15)

        val subtotal = bill?.billInfo?.invoiceSubtotal?.formatCurrency(activity, removeDecimal = isRemoveDecimal)
                ?: ""
        val labelSubtotal = formatWithSpacePostfix("Subtotal", 14)
        val labelPriceSubtotal = formatWithSpacePrefix(subtotal, 15)
        escCmd.append(escCmd.getTextCmd(textSetting, "$space$labelSubtotal:$labelPriceSubtotal"))
        escCmd.append(escCmd.lfcrCmd)

        // tax
        var taxLabel = bill?.billInfo?.taxRule?.name ?: ""
        val totalTax = bill?.billInfo?.totalTax ?: 0.0
        val tax = totalTax.formatCurrency(activity, removeDecimal = isRemoveDecimal)
        val isInclusiveGST = bill?.restaurant?.inclusiveGst ?: false
        if (isInclusiveGST) {
            taxLabel += " (Incl)"
        }
        if (totalTax > 0.0) {
            val taxLabelPrint = formatWithSpacePostfix(taxLabel, 14)
            val labelPriceTax = formatWithSpacePrefix(tax, 15)
            escCmd.append(escCmd.getTextCmd(textSetting, "$space$taxLabelPrint:$labelPriceTax"))
            escCmd.append(escCmd.lfcrCmd)
        }

        // discount
        val discount = bill?.billInfo?.totalDiscount ?: 0.0
        val discountLabel = discount.formatCurrency(activity, removeDecimal = isRemoveDecimal)
        if (discount > 0.0) {
            val discountLabelPrint = formatWithSpacePostfix("Discount", 14)
            val labelPriceDiscount = formatWithSpacePrefix(discountLabel, 15)
            escCmd.append(escCmd.getTextCmd(textSetting, "$space$discountLabelPrint:$labelPriceDiscount"))
            escCmd.append(escCmd.lfcrCmd)
        }

        // charge
        val charge = bill?.billInfo?.totalCharge ?: 0.0
        val chargeLabel = charge.formatCurrency(activity, removeDecimal = isRemoveDecimal)
        if (charge > 0.0) {
            val chargeLabelPrint = formatWithSpacePostfix("Charge", 14)
            val labelPriceCharge = formatWithSpacePrefix(chargeLabel, 15)
            escCmd.append(escCmd.getTextCmd(textSetting, "$space$chargeLabelPrint:$labelPriceCharge"))
            escCmd.append(escCmd.lfcrCmd)
        }

        // rounding
        val rounding = bill?.billInfo?.totalRounding ?: 0.0
        val roundingLabel = rounding.formatCurrency(activity, removeDecimal = isRemoveDecimal)
        if (rounding > 0.0) {
            val roundingLabelPrint = formatWithSpacePostfix("Rounding", 14)
            val labelPriceRounding = formatWithSpacePrefix(roundingLabel, 15)
            escCmd.append(escCmd.getTextCmd(textSetting, "$space$roundingLabelPrint:$labelPriceRounding"))
            escCmd.append(escCmd.lfcrCmd)
        }

    }

    private fun addGrandTotal() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_BOTH_SIDES
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        val space = createSpace(15)
        val grandTotalPrice = bill?.billInfo?.total?.formatCurrency(activity, removeDecimal = isRemoveDecimal)
        val grandLabelPrint = formatWithSpacePostfix("Grand Total", 14)
        val labelPriceGrandTotal = formatWithSpacePrefix(grandTotalPrice ?: "", 15)
        escCmd.append(escCmd.getTextCmd(textSetting, "$space$grandLabelPrint:$labelPriceGrandTotal"))

    }

    private fun addNoteText(text: String) {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        escCmd.append(escCmd.getTextCmd(textSetting, text))
    }

    private fun addFooterText() {
        val textSetting = TextSetting()
        textSetting.align = CommonEnum.ALIGN_MIDDLE
        textSetting.bold = SettingEnum.Enable
        textSetting.isEscSmallCharactor = SettingEnum.Disable
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16

        if (paymentMethod?.paymentType?.equals("CASHAC", true) == true) {
            addLineDivider()
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, "-- Please Proceed to the Cashier --"))
            escCmd.append(escCmd.getTextCmd(textSetting, "\n\n\n"))
            escCmd.append(escCmd.lfcrCmd)
        } else {
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, "-- Cashless Transaction Information --"))
            addLineDivider()

            val textSettingDetail = TextSetting()
            textSettingDetail.align = CommonEnum.ALIGN_LEFT
            textSettingDetail.bold = SettingEnum.Disable
            textSettingDetail.isEscSmallCharactor = SettingEnum.Disable
            textSettingDetail.escFontType = ESCFontTypeEnum.FONT_D_8x16

            val cashLessResponse = paymentMethod?.cashLessResponse

//            escCmd.append(escCmd.getTextCmd(textSettingDetail, "STAN\t\t: ${cashLessResponse?.stan}"))
//            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Merchant ID\t: ${cashLessResponse?.merchantId}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Terminal ID\t: ${cashLessResponse?.terminalId}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Trx Time\t\t: ${cashLessResponse?.bankDateTime}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Trx Ref\t\t: ${cashLessResponse?.txnRef?.trim()}"))
            escCmd.append(escCmd.lfcrCmd)
//            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Card PAN: ${cashLessResponse?.cardPan}"))
//            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Card Type\t: ${cashLessResponse?.cardType}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Auth Code\t: ${cashLessResponse?.authCode}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Status\t\t: ${cashLessResponse?.status}"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSettingDetail, "Result\t\t: ${cashLessResponse?.result}"))
//            escCmd.append(escCmd.lfcrCmd)
//            escCmd.append(escCmd.getTextCmd(textSettingDetail, "ID: ${cashLessResponse?.id}"))
            addLineDivider()

            escCmd.append(escCmd.getTextCmd(textSetting, "\n\n"))
            escCmd.append(escCmd.lfcrCmd)
        }

    }

}*/
