package com.stockq

import com.stockq.core.database.entity.PurchaseStatus
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

class PurchaseManagementTest {

    data class MockPurchaseItem(
        val productId: Long,
        val quantity: Double,
        val unitCostPaise: Long,
        val discountPercent: Double,
        val taxRatePercent: Double,
        var transportAllocationPaise: Long = 0L,
        var effectiveUnitCostPaise: Long = 0L,
        var totalItemCostPaise: Long = 0L
    )

    data class MockPurchase(
        val id: Long,
        val supplierId: Long,
        val invoiceNumber: String,
        val items: List<MockPurchaseItem>,
        val discountPaise: Long,
        val transportChargesPaise: Long,
        var subtotalPaise: Long = 0L,
        var taxPaise: Long = 0L,
        var totalAmountPaise: Long = 0L,
        var status: PurchaseStatus = PurchaseStatus.DRAFT
    )

    class MockPurchaseProcessor {
        fun calculateDraftTotals(purchase: MockPurchase): MockPurchase {
            var subtotal = 0L
            var totalItemDiscount = 0L
            var totalTax = 0L

            for (item in purchase.items) {
                val itemBase = (item.quantity * item.unitCostPaise).roundToLong()
                val itemDiscount = Money.calculateDiscountPaise(itemBase, item.discountPercent)
                val taxable = (itemBase - itemDiscount).coerceAtLeast(0L)
                val itemTax = Money.calculateTaxPaise(taxable, item.taxRatePercent)

                subtotal += itemBase
                totalItemDiscount += itemDiscount
                totalTax += itemTax
                item.totalItemCostPaise = taxable + itemTax
            }

            // Pro-rata transport charges allocation by item value
            for (item in purchase.items) {
                val ratio = if (subtotal > 0L) (item.quantity * item.unitCostPaise) / subtotal.toDouble() else 0.0
                val alloc = (purchase.transportChargesPaise * ratio).roundToLong()
                item.transportAllocationPaise = alloc
                item.totalItemCostPaise += alloc
                item.effectiveUnitCostPaise = if (item.quantity > 0.0) {
                    (item.totalItemCostPaise / item.quantity).roundToLong()
                } else item.unitCostPaise
            }

            val totalEffectiveDiscount = totalItemDiscount + purchase.discountPaise
            val grandTotal = (subtotal - totalEffectiveDiscount).coerceAtLeast(0L) + totalTax + purchase.transportChargesPaise
            purchase.subtotalPaise = subtotal
            purchase.taxPaise = totalTax
            purchase.totalAmountPaise = grandTotal
            return purchase
        }

        fun confirmPurchase(
            purchase: MockPurchase,
            productStockMap: MutableMap<Long, Double>
        ): Result<Unit> {
            if (purchase.status != PurchaseStatus.DRAFT) {
                return Result.failure(IllegalStateException("Purchase is already ${purchase.status}"))
            }

            for (item in purchase.items) {
                val current = productStockMap.getOrDefault(item.productId, 0.0)
                productStockMap[item.productId] = current + item.quantity
            }

            purchase.status = PurchaseStatus.CONFIRMED
            return Result.success(Unit)
        }

        fun cancelPurchase(
            purchase: MockPurchase,
            productStockMap: MutableMap<Long, Double>
        ): Result<Unit> {
            if (purchase.status == PurchaseStatus.CANCELLED) {
                return Result.failure(IllegalStateException("Already cancelled"))
            }

            if (purchase.status == PurchaseStatus.CONFIRMED) {
                // Check if stock has been consumed
                for (item in purchase.items) {
                    val current = productStockMap.getOrDefault(item.productId, 0.0)
                    if (current < item.quantity) {
                        return Result.failure(IllegalStateException("Cannot cancel: Stock was already consumed"))
                    }
                }

                // Reverse stock
                for (item in purchase.items) {
                    val current = productStockMap.getValue(item.productId)
                    productStockMap[item.productId] = current - item.quantity
                }
            }

            purchase.status = PurchaseStatus.CANCELLED
            return Result.success(Unit)
        }
    }

    @Test
    fun calculateDraftTotals_correctlyAllocatesProRataTransportAndEffectiveUnitCost() {
        val processor = MockPurchaseProcessor()

        // 20 units of Product 1 @ ₹100.00 (10000 paise), 5% tax, 0% discount
        val item1 = MockPurchaseItem(productId = 1L, quantity = 20.0, unitCostPaise = 10000L, discountPercent = 0.0, taxRatePercent = 5.0)
        // 10 units of Product 2 @ ₹300.00 (30000 paise), 12% tax, 10% discount
        val item2 = MockPurchaseItem(productId = 2L, quantity = 10.0, unitCostPaise = 30000L, discountPercent = 10.0, taxRatePercent = 12.0)

        // Freight charges: ₹500.00 (50000 paise)
        val purchase = MockPurchase(
            id = 1L,
            supplierId = 10L,
            invoiceNumber = "INV-2026-001",
            items = listOf(item1, item2),
            discountPaise = 0L,
            transportChargesPaise = 50000L
        )

        val computed = processor.calculateDraftTotals(purchase)

        // Item 1 base: 20 * 10000 = 200,000 paise (40% of total 500,000 subtotal)
        // Item 2 base: 10 * 30000 = 300,000 paise (60% of total 500,000 subtotal)
        assertEquals(500_000L, computed.subtotalPaise)

        // Transport allocation:
        // Item 1 alloc = 40% of 50000 = 20,000 paise (₹200.00)
        // Item 2 alloc = 60% of 50000 = 30,000 paise (₹300.00)
        assertEquals(20000L, item1.transportAllocationPaise)
        assertEquals(30000L, item2.transportAllocationPaise)

        // Effective unit cost for Item 1:
        // Taxable = 200,000, Tax 5% = 10,000, Transport = 20,000 -> Total = 230,000 paise
        // Effective unit = 230,000 / 20 = 11,500 paise (₹115.00/unit vs base ₹100.00)
        assertEquals(11500L, item1.effectiveUnitCostPaise)

        // Grand Total: (500,000 - 0) + Tax (10,000 + 32,400) + Freight (50,000) - Item 2 discount (30,000)
        // Verified exact sum
        val expectedGrandTotal = (item1.totalItemCostPaise + item2.totalItemCostPaise)
        assertEquals(expectedGrandTotal, computed.totalAmountPaise)
    }

    @Test
    fun confirmPurchase_updatesStockAndTransitionsToConfirmed() {
        val processor = MockPurchaseProcessor()
        val item1 = MockPurchaseItem(productId = 1L, quantity = 50.0, unitCostPaise = 10000L, discountPercent = 0.0, taxRatePercent = 0.0)
        val purchase = MockPurchase(id = 2L, supplierId = 10L, invoiceNumber = "INV-02", items = listOf(item1), discountPaise = 0L, transportChargesPaise = 0L)
        processor.calculateDraftTotals(purchase)

        val stockMap = mutableMapOf(1L to 15.0) // current stock 15.0

        val confirmResult = processor.confirmPurchase(purchase, stockMap)
        assertTrue(confirmResult.isSuccess)
        assertEquals(PurchaseStatus.CONFIRMED, purchase.status)
        assertEquals(65.0, stockMap[1L]!!, 0.001) // 15 + 50 = 65

        // Cannot double confirm
        val secondConfirm = processor.confirmPurchase(purchase, stockMap)
        assertFalse(secondConfirm.isSuccess)
    }

    @Test
    fun cancelPurchase_reversesStockAndRejectsIfStockConsumed() {
        val processor = MockPurchaseProcessor()
        val item1 = MockPurchaseItem(productId = 1L, quantity = 30.0, unitCostPaise = 10000L, discountPercent = 0.0, taxRatePercent = 0.0)
        val purchase = MockPurchase(id = 3L, supplierId = 10L, invoiceNumber = "INV-03", items = listOf(item1), discountPaise = 0L, transportChargesPaise = 0L)
        processor.calculateDraftTotals(purchase)

        val stockMap = mutableMapOf(1L to 0.0)
        processor.confirmPurchase(purchase, stockMap)
        assertEquals(30.0, stockMap[1L]!!, 0.001)

        // Case A: Stock was partially sold, now only 20 left (< 30) -> cancellation should be blocked!
        stockMap[1L] = 20.0
        val blockedCancel = processor.cancelPurchase(purchase, stockMap)
        assertFalse(blockedCancel.isSuccess)
        assertTrue(blockedCancel.exceptionOrNull() is IllegalStateException)
        assertEquals(20.0, stockMap[1L]!!, 0.001) // untouched

        // Case B: Stock is fully available (e.g. 35.0) -> cancellation succeeds and 30 is reversed!
        stockMap[1L] = 35.0
        val successCancel = processor.cancelPurchase(purchase, stockMap)
        assertTrue(successCancel.isSuccess)
        assertEquals(PurchaseStatus.CANCELLED, purchase.status)
        assertEquals(5.0, stockMap[1L]!!, 0.001) // 35 - 30 = 5
    }
}

