package com.stockq.core.model

import com.stockq.core.ui.components.formatIndianRupees
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

    fun parseRupeesToPaise(text: String): Long {
        val sanitized = text.trim().replace(",", "")
        val doubleVal = sanitized.toDoubleOrNull() ?: return 0L
        return rupeesToPaise(doubleVal)
    }

    fun paiseToRupees(paise: Long): Double = paise / 100.0

    fun paiseToRupeesString(paise: Long): String = String.format(Locale.US, "%.2f", paise / 100.0)

    fun formatPaise(paise: Long): String = formatIndianRupees(paiseToRupees(paise))

    fun formatRupees(paise: Long): String = formatIndianRupees(paiseToRupees(paise))

    /**
     * Calculates tax in paise based on base taxable amount and percentage rate.
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

    /**
     * Extracts base price from a tax-inclusive gross amount.
     * Base = Gross / (1 + Rate / 100)
     */
    fun extractBaseFromTaxInclusive(inclusiveAmountPaise: Long, taxRatePercent: Double): Long {
        if (inclusiveAmountPaise <= 0L || taxRatePercent <= 0.0) return inclusiveAmountPaise
        return ((inclusiveAmountPaise * 10000.0) / (100.0 + taxRatePercent) / 100.0).roundToLong()
    }

    /**
     * Extracts tax component from a tax-inclusive gross amount.
     * Tax = Inclusive - Base
     */
    fun extractTaxFromTaxInclusive(inclusiveAmountPaise: Long, taxRatePercent: Double): Long {
        if (inclusiveAmountPaise <= 0L || taxRatePercent <= 0.0) return 0L
        val base = extractBaseFromTaxInclusive(inclusiveAmountPaise, taxRatePercent)
        return inclusiveAmountPaise - base
    }

    /**
     * Deterministic Largest-Remainder Method (Hare-Niemeyer) for distributing an amount
     * (e.g. freight or invoice-level discount) across multiple line items by their weight.
     *
     * Guaranteed property:
     * allocatedList.sum() == totalToAllocatePaise exactly to 0 paise deviation.
     */
    fun allocateProRata(totalToAllocatePaise: Long, weights: List<Long>): List<Long> {
        if (weights.isEmpty()) return emptyList()
        if (totalToAllocatePaise == 0L) return List(weights.size) { 0L }

        val totalWeight = weights.sum()
        if (totalWeight <= 0L) {
            // Equal distribution if total weights are 0
            val base = totalToAllocatePaise / weights.size
            var remainder = totalToAllocatePaise % weights.size
            return weights.indices.map {
                base + if (remainder-- > 0) 1L else 0L
            }
        }

        data class RemainderEntry(val index: Int, val exactShare: Double, val integerShare: Long, val remainder: Double)

        val entries = weights.mapIndexed { index, weight ->
            val exact = (totalToAllocatePaise.toDouble() * weight) / totalWeight.toDouble()
            val intShare = exact.toLong()
            val rem = exact - intShare
            RemainderEntry(index, exact, intShare, rem)
        }

        val allocatedShares = LongArray(weights.size) { entries[it].integerShare }
        var unallocatedPaise = totalToAllocatePaise - allocatedShares.sum()

        // Distribute unallocated single paise to the entries with largest fractional remainders
        val sortedByRemainder = entries.sortedByDescending { it.remainder }
        for (entry in sortedByRemainder) {
            if (unallocatedPaise <= 0L) break
            allocatedShares[entry.index] += 1L
            unallocatedPaise -= 1L
        }

        return allocatedShares.toList()
    }
}
