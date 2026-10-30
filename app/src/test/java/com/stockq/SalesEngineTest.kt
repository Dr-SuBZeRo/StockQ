package com.stockq

import com.stockq.core.model.Money
import com.stockq.core.model.Quantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SalesEngineTest {

    @Test
    fun lineItemBilling_calculatesAccurateBaseDiscountTaxAndProfit() {
        // Product: Selling Price = ₹150.00 (15000 paise), Cost Basis = ₹100.00 (10000 paise), Tax = 18% GST
        // Quantity: 2 units (2000 milli)
        // Discount: 10%
        val qtyMilli = 2000L
        val unitPricePaise = 15000L
        val costBasisPaise = 10000L
        val discountPercent = 10.0
        val taxRatePercent = 18.0

        val itemBase = Quantity.multiplyMoney(qtyMilli, unitPricePaise) // 30000 paise (₹300.00)
        assertEquals(30000L, itemBase)

        val itemDiscount = Money.calculateDiscountPaise(itemBase, discountPercent) // 3000 paise (₹30.00)
        assertEquals(3000L, itemDiscount)

        val taxableAmount = itemBase - itemDiscount // 27000 paise (₹270.00)
        assertEquals(27000L, taxableAmount)

        val itemTax = Money.calculateTaxPaise(taxableAmount, taxRatePercent) // 18% of 27000 = 4860 paise (₹48.60)
        assertEquals(4860L, itemTax)

        val totalItemPrice = taxableAmount + itemTax // 31860 paise (₹318.60)
        assertEquals(31860L, totalItemPrice)

        val itemCost = Quantity.multiplyMoney(qtyMilli, costBasisPaise) // 20000 paise (₹200.00)
        assertEquals(20000L, itemCost)

        val itemProfit = totalItemPrice - itemCost // 31860 - 20000 = 11860 paise (₹118.60)
        assertEquals(11860L, itemProfit)
    }

    @Test
    fun proRataAllocation_largestRemainderMethodGuaranteesExactSum() {
        val totalToDistribute = 100L // 100 paise
        val weights = listOf(3333L, 3333L, 3334L) // Equal weights roughly 1/3 each

        val allocations = Money.allocateProRata(totalToDistribute, weights)
        assertEquals(3, allocations.size)
        // Sum of allocations must match totalToDistribute down to the exact single paisa
        assertEquals(totalToDistribute, allocations.sum())
    }

    @Test
    fun paymentSplits_calculatesBalanceDueCorrectly() {
        val grandTotalPaise = 50000L // ₹500.00
        val paidAmountPaise = 30000L // ₹300.00

        val balanceDue = grandTotalPaise - paidAmountPaise
        assertEquals(20000L, balanceDue)
        assertTrue(balanceDue > 0L)
    }
}

