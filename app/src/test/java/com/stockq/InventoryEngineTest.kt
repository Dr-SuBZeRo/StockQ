package com.stockq

import com.stockq.core.database.entity.StockMovementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryEngineTest {

    data class MockProduct(
        val id: Long,
        val name: String,
        var currentStock: Double,
        val minStockThreshold: Double
    )

    data class MockStockMovement(
        val productId: Long,
        val movementType: StockMovementType,
        val quantityChange: Double,
        val balanceAfter: Double,
        val reason: String
    )

    class MockStockEngine {
        val movements = mutableListOf<MockStockMovement>()

        fun recordOpeningStock(product: MockProduct, openingQty: Double): Result<Unit> {
            if (openingQty < 0.0) return Result.failure(IllegalArgumentException("Opening stock cannot be negative"))
            product.currentStock = openingQty
            movements.add(
                MockStockMovement(
                    productId = product.id,
                    movementType = StockMovementType.OPENING_STOCK,
                    quantityChange = openingQty,
                    balanceAfter = openingQty,
                    reason = "Initial opening stock"
                )
            )
            return Result.success(Unit)
        }

        fun recordSaleIssue(product: MockProduct, issueQty: Double, notes: String = ""): Result<Unit> {
            if (issueQty <= 0.0) return Result.failure(IllegalArgumentException("Sale quantity must be positive"))
            if (product.currentStock < issueQty) {
                return Result.failure(IllegalStateException("Insufficient stock. Available: ${product.currentStock}, Requested: $issueQty"))
            }
            product.currentStock -= issueQty
            movements.add(
                MockStockMovement(
                    productId = product.id,
                    movementType = StockMovementType.SALE_ISSUE,
                    quantityChange = -issueQty,
                    balanceAfter = product.currentStock,
                    reason = notes
                )
            )
            return Result.success(Unit)
        }

        fun recordPhysicalAdjustment(product: MockProduct, countedStock: Double, reason: String = ""): Result<Unit> {
            if (countedStock < 0.0) return Result.failure(IllegalArgumentException("Counted stock cannot be negative"))
            val diff = countedStock - product.currentStock
            product.currentStock = countedStock
            movements.add(
                MockStockMovement(
                    productId = product.id,
                    movementType = StockMovementType.PHYSICAL_ADJUSTMENT,
                    quantityChange = diff,
                    balanceAfter = countedStock,
                    reason = reason
                )
            )
            return Result.success(Unit)
        }

        fun recordDamagedStock(product: MockProduct, damageQty: Double, reason: String = ""): Result<Unit> {
            if (damageQty <= 0.0) return Result.failure(IllegalArgumentException("Damaged quantity must be positive"))
            if (product.currentStock < damageQty) {
                return Result.failure(IllegalStateException("Cannot write off more damaged stock than on hand"))
            }
            product.currentStock -= damageQty
            movements.add(
                MockStockMovement(
                    productId = product.id,
                    movementType = StockMovementType.DAMAGED_WASTAGE,
                    quantityChange = -damageQty,
                    balanceAfter = product.currentStock,
                    reason = reason
                )
            )
            return Result.success(Unit)
        }

        fun recordCustomerReturn(product: MockProduct, returnQty: Double, reason: String = ""): Result<Unit> {
            if (returnQty <= 0.0) return Result.failure(IllegalArgumentException("Return quantity must be positive"))
            product.currentStock += returnQty
            movements.add(
                MockStockMovement(
                    productId = product.id,
                    movementType = StockMovementType.CUSTOMER_RETURN,
                    quantityChange = returnQty,
                    balanceAfter = product.currentStock,
                    reason = reason
                )
            )
            return Result.success(Unit)
        }
    }

    @Test
    fun openingStock_createsAuditableMovementAndSetsBalance() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Organic Basmati Rice 5kg", currentStock = 0.0, minStockThreshold = 10.0)

        val result = engine.recordOpeningStock(product, 50.0)
        assertTrue(result.isSuccess)
        assertEquals(50.0, product.currentStock, 0.001)
        assertEquals(1, engine.movements.size)
        assertEquals(StockMovementType.OPENING_STOCK, engine.movements[0].movementType)
        assertEquals(50.0, engine.movements[0].balanceAfter, 0.001)
    }

    @Test
    fun saleIssue_reducesStockAndMaintainsLedger() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Wheat Flour 10kg", currentStock = 25.0, minStockThreshold = 5.0)

        val result = engine.recordSaleIssue(product, 10.0, "Retail sale INV-101")
        assertTrue(result.isSuccess)
        assertEquals(15.0, product.currentStock, 0.001)

        val lastMovement = engine.movements.last()
        assertEquals(StockMovementType.SALE_ISSUE, lastMovement.movementType)
        assertEquals(-10.0, lastMovement.quantityChange, 0.001)
        assertEquals(15.0, lastMovement.balanceAfter, 0.001)
    }

    @Test
    fun saleIssue_rejectsNegativeStock() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Sunflower Oil 1L", currentStock = 5.0, minStockThreshold = 2.0)

        val result = engine.recordSaleIssue(product, 6.0)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(5.0, product.currentStock, 0.001) // stock must remain unchanged
        assertEquals(0, engine.movements.size) // no movement recorded
    }

    @Test
    fun physicalAdjustment_recordsBothSurplusAndDeficitCorrectly() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Refined Sugar 1kg", currentStock = 40.0, minStockThreshold = 10.0)

        // Physical count reveals 38 units (2 units deficit)
        val deficitResult = engine.recordPhysicalAdjustment(product, 38.0, "Monthly stock count")
        assertTrue(deficitResult.isSuccess)
        assertEquals(38.0, product.currentStock, 0.001)
        assertEquals(-2.0, engine.movements.last().quantityChange, 0.001)

        // Later recount reveals 41 units (3 units surplus)
        val surplusResult = engine.recordPhysicalAdjustment(product, 41.0, "Recount after misplaced carton located")
        assertTrue(surplusResult.isSuccess)
        assertEquals(41.0, product.currentStock, 0.001)
        assertEquals(3.0, engine.movements.last().quantityChange, 0.001)
        assertEquals(41.0, engine.movements.last().balanceAfter, 0.001)
    }

    @Test
    fun damagedWastage_reducesStockAndCannotExceedAvailable() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Tea Powder 500g", currentStock = 12.0, minStockThreshold = 4.0)

        val damageResult = engine.recordDamagedStock(product, 2.0, "Carton crushed in handling")
        assertTrue(damageResult.isSuccess)
        assertEquals(10.0, product.currentStock, 0.001)
        assertEquals(-2.0, engine.movements.last().quantityChange, 0.001)

        val excessiveDamage = engine.recordDamagedStock(product, 15.0, "Invalid write-off")
        assertFalse(excessiveDamage.isSuccess)
        assertEquals(10.0, product.currentStock, 0.001)
    }

    @Test
    fun customerReturn_replenishesStockAndRecordsMovement() {
        val engine = MockStockEngine()
        val product = MockProduct(id = 1L, name = "Mustard Oil 1L", currentStock = 8.0, minStockThreshold = 3.0)

        val returnResult = engine.recordCustomerReturn(product, 2.0, "Customer exchange")
        assertTrue(returnResult.isSuccess)
        assertEquals(10.0, product.currentStock, 0.001)
        assertEquals(2.0, engine.movements.last().quantityChange, 0.001)
        assertEquals(10.0, engine.movements.last().balanceAfter, 0.001)
    }
}

