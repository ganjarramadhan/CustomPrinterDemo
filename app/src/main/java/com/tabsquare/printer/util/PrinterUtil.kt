package com.tabsquare.printer.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

fun Double.formatCurrency(removeDecimal: Boolean = false): String {

    // get currency instance
    val currencyFormat: NumberFormat = DecimalFormat.getCurrencyInstance(Locale.US) as NumberFormat
    if (removeDecimal) {
        currencyFormat.maximumFractionDigits = 0
        currencyFormat.roundingMode = RoundingMode.DOWN
    }

    // return the result
    return currencyFormat.format(this)
}

fun Date.formatDateOnly(): String {
    val simpleDateFormat = SimpleDateFormat("dd/MM/yyy", Locale.US)
    return simpleDateFormat.format(this)
}

fun Date.formatTimeOnly(): String {
    val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.US)
    return simpleDateFormat.format(this)
}

fun Date.formatDate(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return simpleDateFormat.format(this)
}

fun Date.formatDateAndTime(): String {
    val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.US)
    return simpleDateFormat.format(this)
}

fun String.replaceEndWith(textToAppendInEnd: String): String {
    val sourceLength = length
    val lengthToAppend = textToAppendInEnd.length
    val indexStart = sourceLength - lengthToAppend - 1
    return replaceRange(indexStart, sourceLength - 1, textToAppendInEnd)
}

fun String.replaceEndWithInPaymentDetail(textToAppendInEnd: String, textWhenEmpty: String): String {
    val labelLength = 18
    val sourceLength = length
    var lengthToAppend = textToAppendInEnd.length
    val spaceToAppend = sourceLength - labelLength - 1 // -1 is for "\n"

    if (spaceToAppend >= lengthToAppend) {
        var finalText = textToAppendInEnd
        if (textToAppendInEnd.isEmpty()) {
            lengthToAppend = textWhenEmpty.length
            finalText = textWhenEmpty
        }
        val indexStart = sourceLength - lengthToAppend - 1
        return replaceRange(indexStart, sourceLength - 1, finalText)
    } else {
        var result = ""
        var whiteSpaceLabel = ""
        for (l in 0 until labelLength) {
            whiteSpaceLabel += " "
        }

        val rowCount = ceil(lengthToAppend.toDouble() / spaceToAppend.toDouble()).toInt()
        for (i in 0 until rowCount) {
            val replacementIndexStart = (i * spaceToAppend)
            var replacementIndexEnd = (i * spaceToAppend) + spaceToAppend
            if (replacementIndexEnd >= textToAppendInEnd.length) {
                replacementIndexEnd = textToAppendInEnd.length
            }
            val replacementText =
                textToAppendInEnd.substring(replacementIndexStart, replacementIndexEnd)
            val replacementLength = replacementText.length
            val indexStart = sourceLength - replacementLength - 1
            if (i == 0) {
                result += replaceRange(indexStart, sourceLength - 1, replacementText)
            } else {
                val resultWithLabel = replaceRange(indexStart, sourceLength - 1, replacementText)
                val resultWithoutLabel =
                    resultWithLabel.replaceRange(0, labelLength, whiteSpaceLabel)
                result += resultWithoutLabel
            }
        }
        return result
    }
}