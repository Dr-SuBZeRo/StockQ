package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.StockMovementWithProduct
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.model.Quantity
import kotlinx.coroutines.flow.Flow

data class StockReconciliation(
    val productId: Long,
    val cachedStock: Double,
    val ledgerStock: Double,
    val isConsistent: Boolean
)

/**
 * Auditable Inventory Ledger Engine.
 *
 * Guarantees:
 * 1. Every change in stock is recorded in an immutable StockMovement entry.
 * 2. Current stock is derived from or validated against the ledger.
 * 3. Atomic database transactions prevent stock drift or partial updates.
 * 4. Negative stock policy: Prohibits outward stock issues exceeding available physical stock.
 */
class StockLedgerRepository(private val database: StockQDatabase) {

    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()

    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>> =
        movementDao.getMovementsForProduct(productId)

    fun getRecentMovementsWithProduct(limit: Int = 50): Flow<List<StockMovementWithProduct>> =
        movementDao.getRecentMovementsWithProduct(limit)

    /**
     * Records opening stock when a product is first created or cataloged.
     */
    suspend fun recordOpeningStock(
        productId: Long,
        initialStock: Double,
        costPricePaise: Long,
        notes: String = "Initial opening stock"
    ): Result<StockMovement> = database.withTransaction {
        val initialStockMilli = Quantity.fromDouble(initialStock)
        if (initialStockMilli < 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Opening stock cannot be negative"))
        }

        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Product not found: $productId"))

        val movement = StockMovement(
            productId = productId,
            movementType = StockMovementType.OPENING_STOCK,
            quantityChangeMilli = initialStockMilli,
            balanceAfterMilli = initialStockMilli,
            referenceType = "INITIAL",
            unitCostPaise = costPricePaise,
            reasonOrNotes = notes,
            timestamp = System.currentTimeMillis()
        )

        val id = movementDao.insertMovement(movement)
        productDao.updateStock(productId, initialStockMilli)
        Result.success(movement.copy(id = id))
    }

    /**
     * Records outward stock issue (Sale / Outward delivery).
     * Strictly enforces negative-stock prevention.
     */
    suspend fun recordSaleIssue(
        productId: Long,
        quantity: Double,
        referenceId: Long? = null,
        batchId: Long? = null,
        notes: String = ""
    ): Result<StockMovement> = database.withTransaction {
        val qtyMilli = Quantity.fromDouble(quantity)
        if (qtyMilli <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Issue quantity must be greater than zero"))
        }

        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Product not found: $productId"))

        val currentStockMilli = product.currentStockMilli
        if (currentStockMilli < qtyMilli) {
            return@withTransaction Result.failure(
                IllegalStateException("Insufficient stock: Available ${product.currentStock} ${product.unit}, requested $quantity ${product.unit}")
            )
        }

        val newBalanceMilli = currentStockMilli - qtyMilli
        val movement = StockMovement(
            productId = productId,
            batchId = batchId,
            movementType = StockMovementType.SALE_ISSUE,
            quantityChangeMilli = -qtyMilli,
            balanceAfterMilli = newBalanceMilli,
            referenceType = "SALE",
            referenceId = referenceId,
            unitCostPaise = product.costPricePaise,
            reasonOrNotes = notes.ifBlank { "Sale outward" },
            timestamp = System.currentTimeMillis()
        )

        val id = movementDao.insertMovement(movement)
        productDao.updateStock(productId, newBalanceMilli)
        Result.success(movement.copy(id = id))
    }

    /**
     * Records physical inventory audit adjustment.
     * Computes the exact delta to reconcile physical count with ledger.
     */
    suspend fun recordPhysicalAdjustment(
        productId: Long,
        physicalCount: Double,
        reason: String
    ): Result<StockMovement> = database.withTransaction {
        val physicalCountMilli = Quantity.fromDouble(physicalCount)
        if (physicalCountMilli < 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Physical count cannot be negative"))
        }

        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Product not found: $productId"))

        val deltaMilli = physicalCountMilli - product.currentStockMilli
        val movement = StockMovement(
            productId = productId,
            movementType = StockMovementType.PHYSICAL_ADJUSTMENT,
            quantityChangeMilli = deltaMilli,
            balanceAfterMilli = physicalCountMilli,
            referenceType = "AUDIT",
            unitCostPaise = product.costPricePaise,
            reasonOrNotes = reason.ifBlank { "Physical audit adjustment" },
            timestamp = System.currentTimeMillis()
        )

        val id = movementDao.insertMovement(movement)
        productDao.updateStock(productId, physicalCountMilli)
        Result.success(movement.copy(id = id))
    }

    /**
     * Records damaged stock / spoilage / wastage.
     */
    suspend fun recordDamagedStock(
        productId: Long,
        quantity: Double,
        reason: String
    ): Result<StockMovement> = database.withTransaction {
        val qtyMilli = Quantity.fromDouble(quantity)
        if (qtyMilli <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Damaged quantity must be greater than zero"))
        }

        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Product not found: $productId"))

        if (product.currentStockMilli < qtyMilli) {
            return@withTransaction Result.failure(
                IllegalStateException("Cannot mark more stock damaged than currently in inventory ($quantity > ${product.currentStock})")
            )
        }

        val newBalanceMilli = product.currentStockMilli - qtyMilli
        val movement = StockMovement(
            productId = productId,
            movementType = StockMovementType.DAMAGED_WASTAGE,
            quantityChangeMilli = -qtyMilli,
            balanceAfterMilli = newBalanceMilli,
            referenceType = "DAMAGE",
            unitCostPaise = product.costPricePaise,
            reasonOrNotes = reason.ifBlank { "Damaged or expired inventory" },
            timestamp = System.currentTimeMillis()
        )

        val id = movementDao.insertMovement(movement)
        productDao.updateStock(productId, newBalanceMilli)
        Result.success(movement.copy(id = id))
    }

    /**
     * Records customer return (stock restored to inventory).
     */
    suspend fun recordCustomerReturn(
        productId: Long,
        quantity: Double,
        reason: String
    ): Result<StockMovement> = database.withTransaction {
        val qtyMilli = Quantity.fromDouble(quantity)
        if (qtyMilli <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Return quantity must be greater than zero"))
        }

        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Product not found: $productId"))

        val newBalanceMilli = product.currentStockMilli + qtyMilli
        val movement = StockMovement(
            productId = productId,
            movementType = StockMovementType.CUSTOMER_RETURN,
            quantityChangeMilli = qtyMilli,
            balanceAfterMilli = newBalanceMilli,
            referenceType = "CUSTOMER_RETURN",
            unitCostPaise = product.costPricePaise,
            reasonOrNotes = reason.ifBlank { "Customer product return" },
            timestamp = System.currentTimeMillis()
        )

        val id = movementDao.insertMovement(movement)
        productDao.updateStock(productId, newBalanceMilli)
        Result.success(movement.copy(id = id))
    }

    /**
     * Verifies mathematical consistency between the cached product balance and the ledger history.
     */
    suspend fun reconcileStock(productId: Long): StockReconciliation = database.withTransaction {
        val product = productDao.getProductDirect(productId)
            ?: return@withTransaction StockReconciliation(productId, 0.0, 0.0, false)
        val ledgerSumMilli = movementDao.calculateNetStockFromLedgerMilli(productId)
        val isConsistent = product.currentStockMilli == ledgerSumMilli
        StockReconciliation(
            productId = productId,
            cachedStock = product.currentStock,
            ledgerStock = Quantity.toDouble(ledgerSumMilli),
            isConsistent = isConsistent
        )
    }
}
