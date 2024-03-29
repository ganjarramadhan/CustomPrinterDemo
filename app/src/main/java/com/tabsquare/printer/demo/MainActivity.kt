package com.tabsquare.printer.demo

import android.Manifest
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tabsquare.printer.PrinterManager
import com.tabsquare.printer.core.request.DisplayConfig
import com.tabsquare.printer.core.request.OrderFooter
import com.tabsquare.printer.core.request.OrderHeader
import com.tabsquare.printer.core.request.OrderItem
import com.tabsquare.printer.core.request.PaymentDetail
import com.tabsquare.printer.core.request.PrinterRequest
import com.tabsquare.printer.core.request.PrinterTarget
import com.tabsquare.printer.core.request.QRDetail
import com.tabsquare.printer.core.request.Restaurant
import com.tabsquare.printer.core.request.Tax
import com.tabsquare.printer.util.PrinterStatus
import com.tabsquare.printer.util.formatDateAndTime
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var spinPrinter: Spinner
    private lateinit var etPrinterTarget: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "PrinterDemo", message, t)
            }
        })

        etPrinterTarget = findViewById(R.id.etPrinterTarget)

        spinPrinter = findViewById(R.id.spinPrinter)
        val adapter = ArrayAdapter<String>(this, R.layout.item_spin_printer)
        adapter.addAll(PrinterManager.getPrinterList())
        spinPrinter.adapter = adapter

        val btnPrint = findViewById<Button>(R.id.btnPrint)
        btnPrint.setOnClickListener {
            val selectedPrinter = spinPrinter.selectedItemPosition
            if (selectedPrinter > 0) printReceipt()
            // val intent = Intent(this, EpsonPrinterFinder::class.java)
            // startActivity(intent)
        }

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?,
                ) { /* ... */
                }
            }).check()
    }

    private fun printReceipt() {

        val restaurant = Restaurant(
            merchantKey = "123456789",
            name = "TS Cafe",
            address = "Kallang Ave",
            phone = "123456789",
            moreInfo = "More Info",
            topImage = null,
            // topImage = BitmapFactory.decodeResource(resources, R.drawable.img_tabsquare_logo),
            bottomImage = null
        )

        val paymentDetail = PaymentDetail(
            acquirerBank = "UOB",
            paymentType = "VISA",
            isPrintPaymentDetail = false,
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
        arrayListOf("Item 1", "Item 2").forEach { it ->
            val modifiers = arrayListOf<OrderItem>()
            arrayListOf("Option 1", "Option 2").forEach { option ->
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
            grandTotal = 350.0,
            vouchers = emptyList()
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
            printerTarget = PrinterTarget(
                spinPrinter.selectedItemPosition,
                etPrinterTarget.text.toString()
            ),
            takeAwayInfo = null
        )

        printReceipt(printerRequest)
    }

    private fun printReceipt(printerRequest: PrinterRequest) {

        GlobalScope.launch(Dispatchers.Main) {
            val etPrinterCount = findViewById<EditText>(R.id.etNumberOfCopy)
            val etInterval = findViewById<EditText>(R.id.etIntervalSecond)
            val btnPrint = findViewById<Button>(R.id.btnPrint)

            val printerCount = etPrinterCount.text.toString().toInt()
            val printerInterval = etInterval.text.toString().toLong() * 1000
            var counter = 1

            btnPrint.isEnabled = false

            while (counter <= printerCount) {
                printerRequest.orderHeader?.queueNo = "Queue #$counter"
                val printer =
                    PrinterManager.createReceiptPrinter(this@MainActivity, printerRequest, 1)

                when (val connectionStatus =
                    withContext(Dispatchers.IO) { printer.openConnection() }) {
                    is PrinterStatus.Success -> {
                        // if connection success, then print
                        when (val printingStatus =
                            withContext(Dispatchers.IO) { printer.printReceipt() }) {
                            is PrinterStatus.Success -> {
                                printer.closeConnection()
                                Timber.e("Printer: Attempt #$counter - Success Printing")
                                delay(printerInterval)
                                val printerKitchen = PrinterManager.createKitchenPrinter(this@MainActivity, printerRequest)
                                when(val kitchenConnection = withContext(Dispatchers.IO) { printerKitchen.openConnection() }) {
                                    is PrinterStatus.Success -> {
                                        when(val kitchenReceipt = withContext(Dispatchers.IO) { printerKitchen.printReceipt() }) {
                                            is PrinterStatus.Success -> {
                                                Timber.d("Printer Kitchen Connect: Attempt #$counter - Success print kitchen")
                                                printerKitchen.closeConnection()
                                            }
                                            is PrinterStatus.Error -> {
                                                Timber.e(kitchenReceipt.exception, "Printer Kitchen Connect: Attempt #$counter - ${kitchenReceipt.message}")
                                                printerKitchen.closeConnection()
                                            }
                                        }
                                    }
                                    is PrinterStatus.Error -> {
                                        Timber.e(kitchenConnection.exception, "Printer Kitchen Connect: Attempt #$counter - ${kitchenConnection.message}")
                                    }
                                }
                                counter++
                                delay(printerInterval)
                            }
                            is PrinterStatus.Error -> {
                                printer.closeConnection()
                                Timber.e("Printer: Attempt #$counter -  Fail Printing ${printingStatus.code} - ${printingStatus.message}")
                                counter++
                                delay(printerInterval)
                            }
                        }
                    }
                    is PrinterStatus.Error -> {
                        printer.closeConnection()
                        Timber.d("Printer: Attempt #$counter -  Fail Connecting ${connectionStatus.code} - ${connectionStatus.message}")
                        counter++
                        delay(printerInterval)
                    }
                }
            }

            btnPrint.isEnabled = true
        }
    }
}