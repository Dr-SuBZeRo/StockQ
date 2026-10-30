package com.stockq.core.model

import java.util.Locale
import kotlin.math.roundToLong

/**
 * Fixed-precision Quantity utility for StockQ.
 * All persisted quantities in the database and stock ledger are stored as scaled integer milli-units (Long).
 * 1 standard unit = 1,000 milli-units (scale = 3 decimal places).
 * Examples:
 *   - 1 pc = 1,000 milli-units
 *   - 0.5 kg = 500 milli-units
 *   - 2.750 litres = 2,750 milli-units
 *   - 10 boxes = 10,000 milli-units
 *
 * This completely eliminates floating-point representation and rounding drift across inventory ledger transactions.
 */
object Quantity {
    const val SCALE = 1000L

    /** Converts standard unit double (e.g. 1.5) to scaled integer milli-units (e.g. 1500L). */
    fun fromDouble(units: Double): Long = (units * SCALE).roundToLong()

    /** Converts scaled integer milli-units to double for display calculations. */
    fun toDouble(milliUnits: Long): Double = milliUnits.toDouble() / SCALE

    /** Parses user input string (e.g. "1.5" or "10") to scaled integer milli-units. */
    fun parse(text: String): Long {
        val sanitized = text.trim().replace(",", "")
        val doubleVal = sanitized.toDoubleOrNull() ?: return 0L
        return fromDouble(doubleVal)
    }

    /**
     * Formats scaled integer milli-units cleanly:
     * 1000 -> "1"
     * 1500 -> "1.5"
     * 1250 -> "1.25"
     * 1125 -> "1.125"
     */
    fun toDisplay(milliUnits: Long): String {
        val qty = toDouble(milliUnits)
        return when {
            milliUnits % SCALE == 0L -> String.format(Locale.US, "%.0f", qty)
            milliUnits % 100L == 0L -> String.format(Locale.US, "%.1f", qty)
            milliUnits % 10L == 0L -> String.format(Locale.US, "%.2f", qty)
            else -> String.format(Locale.US, "%.3f", qty)
        }
    }

    /** Formats scaled integer milli-units with unit suffix (e.g. "1.5 kg"). */
    fun format(milliUnits: Long, unit: String = ""): String {
        val display = toDisplay(milliUnits)
        return if (unit.isBlank()) display else "$display $unit"
    }

    /**
     * Exact monetary multiplication:
     * Calculates total cost/price in paise by multiplying scaled quantity milli-units by unit price paise.
     * Total = (QuantityMilli * UnitPricePaise) / 1000 (with commercial rounding).
     */
    fun multiplyMoney(quantityMilli: Long, unitPricePaise: Long): Long {
        if (quantityMilli <= 0L || unitPricePaise <= 0L) return 0L
        return ((quantityMilli * unitPricePaise).toDouble() / SCALE).roundToLong()
    }
}

