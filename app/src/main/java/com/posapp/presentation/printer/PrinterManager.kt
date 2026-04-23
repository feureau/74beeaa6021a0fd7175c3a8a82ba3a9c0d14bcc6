package com.posapp.presentation.printer

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.posapp.domain.model.CartItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrinterManager(private val context: Context) {

    fun findPrinter(): BluetoothConnection? {
        val printers = BluetoothPrintersConnections().list
        return if (printers != null && printers.isNotEmpty()) {
            printers.first()
        } else {
            null
        }
    }

    fun printReceipt(
        storeName: String,
        items: List<CartItem>,
        total: Double,
        footer: String
    ) {
        val printer = findPrinter() ?: throw Exception("No printer found")
        printReceipt(printer, storeName, items, total, footer)
    }

    fun printReceipt(
        context: Context,
        storeName: String,
        items: List<CartItem>,
        total: Double,
        footer: String
    ) {
        val printer = findPrinter() ?: throw Exception("No printer found. Make sure the printer is paired and turned on.")
        printReceipt(printer, storeName, items, total, footer)
    }

    private fun printReceipt(
        printer: BluetoothConnection,
        storeName: String,
        items: List<CartItem>,
        total: Double,
        footer: String
    ) {
        try {
            val escPosPrinter = EscPosPrinter(printer, 203, 48f, 32)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
            val dateStr = dateFormat.format(Date())

            val receipt = buildString {
                appendLine("[C]$storeName")
                appendLine("[C]--------------------------------")
                appendLine("[L]$dateStr[R]")
                appendLine("")
                appendLine("[L]--------------------------------")
                appendLine("")

                for (item in items) {
                    val itemLine = "${item.menuItem.name} x${item.quantity}"
                    val priceLine = "Rp${item.totalPrice.toLong()}"
                    val spaces = 32 - itemLine.length - priceLine.length
                    appendLine("[L]$itemLine${" ".repeat(spaces.coerceAtLeast(1))}$priceLine")
                }

                appendLine("")
                appendLine("[L]--------------------------------")
                appendLine("")

                val totalLabel = "TOTAL"
                val totalStr = "Rp${total.toLong()}"
                val totalSpaces = 32 - totalLabel.length - totalStr.length
                appendLine("[L]$totalLabel${" ".repeat(totalSpaces.coerceAtLeast(1))}$totalStr")

                appendLine("")
                appendLine("[C]================================")
                appendLine("[C]$footer")
                appendLine("")
                appendLine("")
            }

            escPosPrinter.printFormattedTextAndCut(receipt)
            printer.disconnect()
        } catch (e: EscPosConnectionException) {
            throw Exception("Printer connection failed: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Print failed: ${e.message}")
        }
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPairedPrinters(): List<BluetoothConnection> {
        return BluetoothPrintersConnections().list?.toList() ?: emptyList()
    }
}

private fun String.repeat(times: Int): String {
    return (1..times).joinToString("") { this }
}