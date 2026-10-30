package com.stockq

import com.stockq.core.database.dao.SupplierProductQuote
import com.stockq.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplierComparisonTest {

    @Test
    fun supplierQuotes_ranksLowestEffectiveCostFirst() {
        val quotes = listOf(
            SupplierProductQuote(
                purchaseItemId = 1L,
                purchaseId = 101L,
                supplierId = 1L,
                supplierName = "Sunrise Agro Traders",
                productId = 5L,
                productName = "Basmati Rice 5kg",
                productUnit = "bag",
                unitCostPaise = 38000L,
                discountPercent = 0.0,
                taxRatePercent = 5.0,
                transportAllocationPaise = 1000L,
                effectiveUnitCostPaise = 40900L, // ₹409.00
                purchaseDate = 1700000000000L
            ),
            SupplierProductQuote(
                purchaseItemId = 2L,
                purchaseId = 102L,
                supplierId = 2L,
                supplierName = "Apex Wholesale Corp",
                productId = 5L,
                productName = "Basmati Rice 5kg",
                productUnit = "bag",
                unitCostPaise = 36000L,
                discountPercent = 2.0,
                taxRatePercent = 5.0,
                transportAllocationPaise = 500L,
                effectiveUnitCostPaise = 37550L, // ₹375.50
                purchaseDate = 1700500000000L
            ),
            SupplierProductQuote(
                purchaseItemId = 3L,
                purchaseId = 103L,
                supplierId = 3L,
                supplierName = "Metro Global Foods",
                productId = 5L,
                productName = "Basmati Rice 5kg",
                productUnit = "bag",
                unitCostPaise = 39000L,
                discountPercent = 5.0,
                taxRatePercent = 5.0,
                transportAllocationPaise = 1500L,
                effectiveUnitCostPaise = 40455L, // ₹404.55
                purchaseDate = 1701000000000L
            )
        )

        val rankedQuotes = quotes.sortedBy { it.effectiveUnitCostPaise }

        // Rank #1: Apex Wholesale Corp @ 37550 paise (₹375.50)
        assertEquals("Apex Wholesale Corp", rankedQuotes[0].supplierName)
        assertEquals(37550L, rankedQuotes[0].effectiveUnitCostPaise)

        // Rank #2: Metro Global Foods @ 40455 paise (₹404.55)
        assertEquals("Metro Global Foods", rankedQuotes[1].supplierName)
        assertEquals(40455L, rankedQuotes[1].effectiveUnitCostPaise)

        // Rank #3: Sunrise Agro Traders @ 40900 paise (₹409.00)
        assertEquals("Sunrise Agro Traders", rankedQuotes[2].supplierName)
        assertEquals(40900L, rankedQuotes[2].effectiveUnitCostPaise)

        // Verify savings calculation:
        val bestPrice = rankedQuotes[0].effectiveUnitCostPaise
        val diffVsSunrise = rankedQuotes[2].effectiveUnitCostPaise - bestPrice // 40900 - 37550 = 3350 paise (₹33.50/unit)
        assertEquals(3350L, diffVsSunrise)
        assertEquals("₹ 33.50", Money.formatPaise(diffVsSunrise))
    }

    @Test
    fun quoteDifference_accuratelyComputesPercentageSpread() {
        val lowestRatePaise = 10000L // ₹100.00
        val higherRatePaise = 12000L // ₹120.00

        val diffPaise = higherRatePaise - lowestRatePaise
        val percentHigher = (diffPaise.toDouble() / lowestRatePaise.toDouble()) * 100.0

        assertEquals(2000L, diffPaise)
        assertEquals(20.0, percentHigher, 0.001)
    }
}

