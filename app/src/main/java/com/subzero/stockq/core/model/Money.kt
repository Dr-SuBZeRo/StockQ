package com.subzero.stockq.core.model

import com.subzero.stockq.core.ui.components.formatIndianRupees
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Monetary utility for StockQ.
 * All monetary values in database and business calculations are stored as integer minor units (paise).
 * 1 Rupee = 100 paise.
 * This guarantees zero floating-point accumulation errors in inventory valuation and invoices.
 */
object Money {
    fun rupeesToPaise(rupees: Double): Long = (rupees * 100.0).roundToLong()

    fun paiseToRupees(paise: Long): Double = paise / 100.0

    fun paiseToRupeesString(paise: Long): String = String.format(Locale.US, "%.2f", paise / 100.0)

    fun formatPaise(paise: Long): String = formatIndianRupees(paiseToRupees(paise))

    /**
     * Calculates tax in paise based on base amount and percentage rate.
     * Rounding mode: Half-up (standard commercial rounding).
     */
    fun calculateTaxPaise(amountPaise: Long, taxRatePercent: Double): Long {
        if (amountPaise <= 0L || taxRatePercent <= 0.0) return 0L
        return ((amountPaise * taxRatePercent) / 100.0).roundToLong()
    }

    /**
     * Calculates discount in paise based on base amount and percentage rate.
     */
    fun calculateDiscountPaise(amountPaise: Long, discountPercent: Double): Long {
        if (amountPaise <= 0L || discountPercent <= 0.0) return 0L
        return ((amountPaise * discountPercent) / 100.0).roundToLong()
    }
}
