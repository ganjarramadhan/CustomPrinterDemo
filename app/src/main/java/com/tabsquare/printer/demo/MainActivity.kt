package com.tabsquare.printer.demo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.tabsquare.printer.PrinterManager
import com.tabsquare.printer.core.request.DisplayConfig
import com.tabsquare.printer.core.request.OrderFooter
import com.tabsquare.printer.core.request.OrderHeader
import com.tabsquare.printer.core.request.OrderItem
import com.tabsquare.printer.core.request.PaymentDetail
import com.tabsquare.printer.core.request.PrinterRequest
import com.tabsquare.printer.core.request.QRDetail
import com.tabsquare.printer.core.request.Restaurant
import com.tabsquare.printer.core.request.Tax
import com.tabsquare.printer.util.PrinterStatus
import com.tabsquare.printer.util.formatDateAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "PrinterDemo", message, t)
            }
        })

        val btnPrint = findViewById<Button>(R.id.btnPrint)
        btnPrint.setOnClickListener {
            printReceipt()
        }
    }

    private fun printReceipt() {

        val restaurant = Restaurant(
            merchantKey = "123456789",
            name = "TS Cafe",
            address = "Kallang Ave",
            phone = "123456789",
            moreInfo = "More Info",
            topImage = BitmapFactory.decodeResource(resources, R.drawable.img_tabsquare_logo),
            bottomImage = null
        )

        val paymentDetail = PaymentDetail(
            acquirerBank = "UOB",
            paymentType = "VISA",
            isPrintPaymentDetail = true,
            stan = "STAN",
            merchantId = "123",
            terminalId = "T-123",
            bankDateTime = Date().formatDateAndTime(),
            txnRef = "REF-123",
            cardPan = "PAN",
            cardType = "VISA",
            authCode = "AUTH123",
            status = "APPROVED",
            id = "1234567890",
            result = "APPROVED",
            rrn = "RRN",
            surcharge = "NO",
            responseText = "",
            responseWidth = 0
        )

        val orderHeader = OrderHeader(
            invoiceNumber = "INV-000001",
            checkId = "CHECK#123",
            queueNo = "Q#12345",
            billNo = "",
            buzzer = "",
            date = Date(),
            orderType = "DINE IN",
            host = "K1",
            cashier = "K1",
            area = "Self Kiosk"
        )

        val isInclusive = false
        val taxes = arrayListOf<Tax>()
        taxes.add(
            Tax(
                name = "GST 7%",
                amount = 70.0,
                isInclusive = isInclusive
            )
        )

        val orderItems = arrayListOf<OrderItem>()
        arrayListOf("Item 1", "Item 2", "Item 3").forEach { it ->
            val modifiers = arrayListOf<OrderItem>()
            arrayListOf("Option 1", "Option 2", "Option 3").forEach { option ->
                val nestedModifiers = arrayListOf<OrderItem>()
                // arrayListOf("Nested Option 1", "Nested Option 2", "Nested Option 3").forEach { nestedOption ->
                //     val nestedModifier = OrderItem(
                //         customisationId = 1,
                //         skuId = 1,
                //         dishId = 1,
                //         dishName = nestedOption,
                //         skuName = "SKU $nestedOption",
                //         isPrintSku = false,
                //         quantity = 1,
                //         pricePerUnit = 10.0,
                //         discount = 0.0,
                //         tax = 7.0,
                //         totalPrice = 17.0,
                //         remarks = "",
                //         modifiers = null
                //     )
                //     nestedModifiers.add(nestedModifier)
                //     Timber.i("Nested Option: ${nestedModifier.dishName}")
                //     Timber.i("Nested Modifier Size: ${nestedModifiers.size}")
                // }

                val modifier = OrderItem(
                    customisationId = 1,
                    skuId = 1,
                    dishId = 1,
                    dishName = option,
                    skuName = "SKU $option",
                    isPrintSku = false,
                    quantity = 1,
                    pricePerUnit = 10.0,
                    discount = 0.0,
                    tax = 7.0,
                    totalPrice = 17.0,
                    remarks = "",
                    modifiers = nestedModifiers
                )
                Timber.i("Nested Modifiers to Add to Modifier Size: ${nestedModifiers.size}")
                modifiers.add(modifier)
            }

            val orderItem = OrderItem(
                customisationId = null,
                skuId = 1,
                dishId = 1,
                dishName = it,
                skuName = "SKU $it",
                isPrintSku = true,
                quantity = 1,
                pricePerUnit = 10.0,
                discount = 0.0,
                tax = 7.0,
                totalPrice = 17.0,
                remarks = "",
                modifiers = modifiers
            )
            orderItems.add(orderItem)
        }

        val orderFooter = OrderFooter(
            subtotal = 300.0,
            discount = 10.0,
            cashBack = 0.0,
            serviceCharge = 10.0,
            rounding = 5.0,
            taxes = taxes,
            totalTax = 70.0,
            grandTotal = 350.0
        )

        val qrDetail = QRDetail(
            header = "Thanks for dining with us!",
            body = "https://www.tabsquare.ai/",
            footer = ""
        )

        val customerDetail = null
        // CustomerDetail(
        //     name = "Tabsquare Support",
        //     address = "Kallang Ave",
        //     tin = null,
        //     businessType = null,
        //     printCustomerNameAsOrderNo = null
        // )

        val displayConfig = DisplayConfig(
            useQueueNumberLabel = false,
            groupSameOption = false,
            queueNumber = "Queue No.",
            buzzerNumber = "Buzzer No.",
            officialReceipt = false,
            thankYou = "Thank You",
            pleaseProceedPay = "Please proceed to pay at counter"
        )

        val printerRequest = PrinterRequest(
            confirmedOrder = true,
            message = "",
            printCopyMode = 0,
            restaurant = restaurant,
            paymentDetail = paymentDetail,
            orderHeader = orderHeader,
            orderItems = orderItems,
            orderFooter = orderFooter,
            customerDetail = customerDetail,
            qrDetail = qrDetail,
            isRemoveDecimal = false,
            countryId = 1,
            displayConfig = displayConfig,
            snCode = null,
            minCode = null,
            printerTarget = null,
            takeAwayInfo = null
        )

        printReceipt(printerRequest)
    }

    private fun printReceipt(printerRequest: PrinterRequest) {

        GlobalScope.launch(Dispatchers.Main) {
            val etPrinterCount = findViewById<EditText>(R.id.etNumberOfCopy)
            val etInterval = findViewById<EditText>(R.id.etIntervalSecond)
            val etLog = findViewById<EditText>(R.id.etLog)
            val btnPrint = findViewById<Button>(R.id.btnPrint)

            val printerCount = 1//etPrinterCount.text.toString().toInt()
            val printerInterval = 10000L //etInterval.text.toString().toLong() * 1000
            var counter = 1

            btnPrint.isEnabled = false

            while (counter <= printerCount) {
                printerRequest.orderHeader?.queueNo = "Queue #$counter"
                val printer =
                    PrinterManager.createReceiptPrinter(this@MainActivity, printerRequest)

                when (val connectionStatus = withContext(Dispatchers.IO) { printer.openConnection() }) {
                    is PrinterStatus.Success -> {
                        // if connection success, then print
                        when (val printingStatus = withContext(Dispatchers.IO) { printer.printReceipt() }) {
                            is PrinterStatus.Success -> {
                                printer.closeConnection()
                                val messageLog = "Printer: Attempt #$counter - Success Printing"
                                Timber.e(messageLog)
                                etLog.setText(messageLog)
                                counter++
                                delay(printerInterval)
                            }
                            is PrinterStatus.Error -> {
                                printer.closeConnection()
                                val messageLog = "Printer: Attempt #$counter -  Fail Printing ${printingStatus.code} - ${printingStatus.message}"
                                Timber.e(messageLog)
                                etLog.setText(messageLog)
                                counter++
                                delay(printerInterval)
                            }
                        }
                    }
                    is PrinterStatus.Error -> {
                        printer.closeConnection()
                        val messageLog = "Printer: Attempt #$counter -  Fail Connecting ${connectionStatus.code} - ${connectionStatus.message}"
                        Timber.e(messageLog)
                        etLog.setText(messageLog)
                        counter++
                        delay(printerInterval)
                    }
                }
            }

            btnPrint.isEnabled = true
        }
    }
}