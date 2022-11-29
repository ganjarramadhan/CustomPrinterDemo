package com.tabsquare.printer.demo

import com.tabsquare.printer.core.constant.ConnectionType
import com.tabsquare.printer.core.constant.PrinterMode
import com.tabsquare.printer.core.constant.PrinterType
import com.tabsquare.printer.core.constant.PrinterVendor


/**
 * Printer filter
 *
 * @constructor Create empty Printer filter
 */
object PrinterFilter {

    /**
     * Get printer type index by vendor
     *
     * @param vendor
     * @param connectionType
     * @param mode
     * @return
     */
    fun getPrinterTypeIndexByVendor(
        vendor: PrinterVendor,
        connectionType: ConnectionType,
        mode: PrinterMode
    ): Int {
        val printers = PrinterType.getAll().filter { isModeValid(it, mode) }
        val selectedPrinter = printers.find {
            it.vendor == vendor && it.connectionType == connectionType
        }
        return printers.indexOf(selectedPrinter)
    }

    /**
     * Get printer type by index
     *
     * @param index
     * @param mode
     * @return
     */
    fun getPrinterTypeByIndex(index: Int, mode: PrinterMode): PrinterType {
        return PrinterType.getAll().filter { isModeValid(it, mode) }
            .elementAt(index)
    }

    /**
     * Get printer vendor index by type index
     *
     * @param index
     * @param mode
     * @return
     */
    fun getPrinterVendorIndexByTypeIndex(index: Int, mode: PrinterMode): Int {
        val printerVendorList =
            when (mode) {
                PrinterMode.KITCHEN -> PrinterVendor.getPrinterVendorKitchen()
                PrinterMode.OUTLET -> PrinterVendor.getPrinterVendorOutlet()
                else -> PrinterVendor.getPrinterVendorAll()
            }

        val printerType = PrinterType.getAll().filter { isModeValid(it, mode) }[index]

        return printerVendorList.indexOf(printerType.vendor)
    }

    /**
     * Get connection type index by type index
     *
     * @param index
     * @param mode
     * @return
     */
    fun getConnectionTypeIndexByTypeIndex(index: Int, mode: PrinterMode): Int {
        val printerType = PrinterType.getAll().filter { isModeValid(it, mode) }[index]
        val connectionType = PrinterType.getConnectionTypeAvailable(printerType.vendor)

        return connectionType.indexOf(printerType.connectionType)
    }

    private fun isModeValid(printerType: PrinterType, mode: PrinterMode): Boolean =
        printerType.vendor.mode == mode || printerType.vendor.mode == PrinterMode.ALL
}