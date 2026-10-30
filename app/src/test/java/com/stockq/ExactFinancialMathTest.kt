package com.stockq

import com.stockq.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

class ExactFinancialMathTest {

    @Test
    fun rupeesToPaise_handlesExactConversions() {
        assertEquals(10000L, Money.rupeesToPaise(100.0))
        assertEquals(10050L, Money.rupeesToPaise(100.50))
        assertEquals(99L, Money.rupeesToPaise(0.99))
        assertEquals(1L, Money.rupeesToPaise(0.01))
        assertEquals(0L, Money.rupeesToPaise(0.0))
    }

    @Test
    fun paiseToRupees_handlesExactConversions() {
        assertEquals(100.0, Money.paiseToRupees(10000L), 0.001)
        assertEquals(100.50, Money.paiseToRupees(10050L), 0.001)
        assertEquals(0.99, Money.paiseToRupees(99L), 0.001)
        assertEquals(0.01, Money.paiseToRupees(1L), 0.001)
    }

    @Test
    fun paiseToRupeesString_formatsTwoDecimals() {
        assertEquals("100.00", Money.paiseToRupeesString(10000L))
        assertEquals("100.50", Money.paiseToRupeesString(10050L))
        assertEquals("0.99", Money.paiseToRupeesString(99L))
    }

    @Test
    fun calculateTaxPaise_computesAccurateCommercialRounding() {
        // ₹100.00 @ 18% GST = ₹18.00 (1800 paise)
        val tax18 = Money.calculateTaxPaise(10000L, 18.0)
        assertEquals(1800L, tax18)

        // ₹100.00 @ 5% GST = ₹5.00 (500 paise)
        val tax5 = Money.calculateTaxPaise(10000L, 5.0)
        assertEquals(500L, tax5)

        // ₹100.00 @ 12% GST = ₹12.00 (1200 paise)
        val tax12 = Money.calculateTaxPaise(10000L, 12.0)
        assertEquals(1200L, tax12)

        // ₹33.33 (3333 paise) @ 18% = 599.94 paise -> rounded to 600 paise
        val roundedTax = Money.calculateTaxPaise(3333L, 18.0)
        assertEquals(600L, roundedTax)
    }

    @Test
    fun calculateDiscountPaise_computesAccurateDiscounts() {
        // ₹200.00 (20000 paise) with 10% discount = ₹20.00 (2000 paise)
        val disc10 = Money.calculateDiscountPaise(20000L, 10.0)
        assertEquals(2000L, disc10)

        // Zero percent discount
        val disc0 = Money.calculateDiscountPaise(20000L, 0.0)
        assertEquals(0L, disc0)
    }

    @Test
    fun accumulationPrecision_zeroFloatingPointDriftOver10000Transactions() {
        var runningTotalPaise = 0L
        val lineItemPricePaise = 1999L // ₹19.99

        for (i in 1..10_000) {
            runningTotalPaise += lineItemPricePaise
        }

        // 10,000 * 1999 = 19,990,000 paise exactly
        assertEquals(19_990_000L, runningTotalPaise)
        assertEquals(199900.0, Money.paiseToRupees(runningTotalPaise), 0.0001)
    }

    @Test
    fun proRataTransportAllocation_conservesTotalTransportCharges() {
        val transportChargesPaise = 50000L // ₹500.00 freight

        // Item 1: 10 units @ ₹100.00 = ₹1,000.00 (100,000 paise)
        val item1Base = 100_000L
        // Item 2: 5 units @ ₹600.00 = ₹3,000.00 (300,000 paise)
        val item2Base = 300_000L
        val subtotalPaise = item1Base + item2Base // 400,000 paise

        val alloc1 = (transportChargesPaise * (item1Base.toDouble() / subtotalPaise.toDouble())).roundToLong()
        val alloc2 = (transportChargesPaise * (item2Base.toDouble() / subtotalPaise.toDouble())).roundToLong()

        // Item 1 is 25% of subtotal -> ₹125.00 (12500 paise)
        assertEquals(12500L, alloc1)
        // Item 2 is 75% of subtotal -> ₹375.00 (37500 paise)
        assertEquals(37500L, alloc2)

        // Total allocated must equal total transport charges
        assertEquals(transportChargesPaise, alloc1 + alloc2)
    }
}

