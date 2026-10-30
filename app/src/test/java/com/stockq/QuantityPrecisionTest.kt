package com.stockq

import com.stockq.core.model.Quantity
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityPrecisionTest {

    @Test
    fun fromDouble_handlesExactScaledConversion() {
        assertEquals(1000L, Quantity.fromDouble(1.0))
        assertEquals(1500L, Quantity.fromDouble(1.5))
        assertEquals(250L, Quantity.fromDouble(0.25))
        assertEquals(1L, Quantity.fromDouble(0.001))
        assertEquals(0L, Quantity.fromDouble(0.0))
    }

    @Test
    fun toDouble_handlesExactConversion() {
        assertEquals(1.0, Quantity.toDouble(1000L), 0.0001)
        assertEquals(1.5, Quantity.toDouble(1500L), 0.0001)
        assertEquals(0.25, Quantity.toDouble(250L), 0.0001)
        assertEquals(0.001, Quantity.toDouble(1L), 0.0001)
    }

    @Test
    fun parse_handlesFormats() {
        assertEquals(2500L, Quantity.parse("2.5"))
        assertEquals(10000L, Quantity.parse("10"))
        assertEquals(750L, Quantity.parse("0.750"))
        assertEquals(0L, Quantity.parse("invalid"))
    }

    @Test
    fun toDisplay_trimsTrailingZerosCleanly() {
        assertEquals("5", Quantity.toDisplay(5000L))
        assertEquals("5.5", Quantity.toDisplay(5500L))
        assertEquals("5.25", Quantity.toDisplay(5250L))
        assertEquals("0.125", Quantity.toDisplay(125L))
    }

    @Test
    fun multiplyMoney_computesExactPaise() {
        // 2.500 kg @ ₹40.00/kg (4000 paise) = 10000 paise (₹100.00)
        val result = Quantity.multiplyMoney(2500L, 4000L)
        assertEquals(10000L, result)

        // 1.333 units @ ₹10.00 (1000 paise) = (1333 * 1000 + 500) / 1000 = 1333 paise
        val fractionalResult = Quantity.multiplyMoney(1333L, 1000L)
        assertEquals(1333L, fractionalResult)
    }

    @Test
    fun repeatedDeductions_exhibitsZeroFloatingPointDrift() {
        var stockMilli = 1000000L // 1,000.000 units
        val deductionMilli = 333L // 0.333 units

        for (i in 1..1000) {
            stockMilli -= deductionMilli
        }

        // 1,000,000 - (1000 * 333) = 667,000 exactly
        assertEquals(667000L, stockMilli)
        assertEquals(667.0, Quantity.toDouble(stockMilli), 0.0001)
    }
}

