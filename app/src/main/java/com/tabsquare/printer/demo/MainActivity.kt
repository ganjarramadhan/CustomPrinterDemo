package com.tabsquare.printer.demo

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.datadog.android.DatadogSite
import com.epson.epos2.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tabsquare.log.TabsquareLogImpl
import com.tabsquare.printer.PrinterManagerImp
import com.tabsquare.printer.core.constant.PrinterStatus
import com.tabsquare.printer.core.constant.PrinterType
import com.tabsquare.printer.core.request.*
import com.tabsquare.printer.core.state.PrintState
import com.tabsquare.printer.templates.receipt.DefaultReceiptTemplate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var spinPrinter: Spinner
    private lateinit var etPrinterTarget: EditText

    private val dispatcher = PrinterDispatcher()
    private val translator = PrinterTranslator()
    private val prefs = LoggerPrefs()
    private val logger = TabsquareLogImpl(prefs)
    private val printerManager = PrinterManagerImp(dispatcher, translator, logger)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "PrinterDemo", message, t)
            }
        })

        initDog()

        val tvTitle: TextView = findViewById(R.id.tvTitle)
        tvTitle.text = "Test Epson: SDK ${Log.getSdkVersion()}"

        etPrinterTarget = findViewById(R.id.etPrinterTarget)
        etPrinterTarget.visibility = View.GONE

        spinPrinter = findViewById(R.id.spinPrinter)
        val adapter = ArrayAdapter<String>(this, R.layout.item_spin_printer)
        adapter.addAll(printerManager.getOutletPrinterVendor().joinToString { it.displayName })
        spinPrinter.adapter = adapter
        spinPrinter.visibility = View.GONE

        val btnPrint = findViewById<Button>(R.id.btnPrint)
        btnPrint.setOnClickListener {
//            val selectedPrinter = spinPrinter.selectedItemPosition
//            if (selectedPrinter > 0) printReceipt()
            // val intent = Intent(this, EpsonPrinterFinder::class.java)
            // startActivity(intent)
            printReceipt()
        }

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
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
            bankDateTime = "2022-11-23",
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
            officialReceipt = false,
        )

        val printerRequest = PrinterRequest(
            confirmedOrder = true,
            message = "",
            restaurant = restaurant,
            paymentDetail = paymentDetail,
            orderHeader = orderHeader,
            orderItems = orderItems,
            orderFooter = orderFooter,
            customerDetail = customerDetail,
            qrDetail = qrDetail,
            displayConfig = displayConfig,
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
                val printerTarget = PrinterTarget(
                    PrinterType.EPSON_TM_M30_BT,
                    "BT:00:01:90:84:CA:12"
                )
                val printingStatus = printerManager.printReceipt(
                    context = this@MainActivity,
                    request = printerRequest,
                    printerTarget = printerTarget,
                    template = DefaultReceiptTemplate()
                )
                printingStatus.collectLatest {
                    Timber.d("Collect Printer State :: ${when (it){ is PrintState.Success -> {it.status.message} is PrintState.Error -> { it.status.message } else -> {"UNKNOWN ERROR"} }}")
                    when (it) {
                        is PrintState.Success -> {
                            if (it.status == PrinterStatus.STATUS_COMPLETED_PRINT) {
                                withContext(Dispatchers.Main) {
                                    Timber.i("Success to print")
                                }
                            }
                        }
                        is PrintState.Error -> {
                            withContext(Dispatchers.Main) {
                                Timber.e("Failed to print")
                            }
                        }
                        else -> {}
                    }
                }
                counter++
                delay(printerInterval)
            }

            btnPrint.isEnabled = true
        }
    }

    private fun initDog() {
        val clientToken = "pub350ce8ebd242c30a6bf8cb5be17c372f"

        logger.initDataDog(
            this,
            DatadogSite.EU1,
            clientToken,
            "069e83a7-a1b8-4c6b-8b95-7fb55dba1ba0",
            BuildConfig.DEBUG
        )
        logger.addDataDogTag("terminal_id", "TEST")
        logger.addDataDogTag("brand_merchant_key", "TEST")
    }
}