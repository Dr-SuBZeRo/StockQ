package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.Purchase
import com.stockq.core.database.entity.PurchaseItem
import com.stockq.core.database.entity.PurchaseStatus
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.model.Money
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToLong

data class PurchaseDraftItemInput(
    val productId: Long,
    val quantity: Double,
    val unitCostPaise: Long,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0
)

class PurchaseRepository(private val database: StockQDatabase) {

    private val purchaseDao = database.purchaseDao()
    private val itemDao = database.purchaseItemDao()
    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()

    fun getAllPurchases(): Flow<List<Purchase>> = purchaseDao.getAllPurchases()

    fun getAllPurchasesWithSupplier(): Flow<List<com.stockq.core.database.dao.PurchaseWithSupplier>> =
        purchaseDao.getAllPurchasesWithSupplier()

    fun getPurchaseById(id: Long): Flow<Purchase?> = purchaseDao.getPurchaseById(id)

    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItem>> = itemDao.getItemsForPurchase(purchaseId)

    /**
     * Creates a draft purchase and saves line items with exact financial calculations and transport allocation.
     */
    suspend fun createPurchase(
        supplierId: Long,
        invoiceNumber: String,
        purchaseDate: Long,
        items: List<PurchaseDraftItemInput>,
        discountPaise: Long = 0L,
        transportChargesPaise: Long = 0L,
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (items.isEmpty()) {
            return@withTransaction Result.failure(IllegalArgumentException("Purchase must contain at least one line item"))
        }

        // 1. Calculate line item totals
        var subtotalPaise = 0L
        var totalItemDiscountPaise = 0L
        var totalTaxPaise = 0L
        val calculatedItems = mutableListOf<PurchaseItem>()

        for (item in items) {
            if (item.quantity <= 0.0) {
                return@withTransaction Result.failure(IllegalArgumentException("Quantity must be greater than zero"))
            }
            if (item.unitCostPaise < 0L) {
                return@withTransaction Result.failure(IllegalArgumentException("Unit cost cannot be negative"))
            }

            val itemBase = (item.quantity * item.unitCostPaise).roundToLong()
            val itemDiscount = Money.calculateDiscountPaise(itemBase, item.discountPercent)
            val taxableAmount = (itemBase - itemDiscount).coerceAtLeast(0L)
            val itemTax = Money.calculateTaxPaise(taxableAmount, item.taxRatePercent)

            subtotalPaise += itemBase
            totalItemDiscountPaise += itemDiscount
            totalTaxPaise += itemTax

            calculatedItems.add(
                PurchaseItem(
                    purchaseId = 0L, // will assign after purchase insert
                    productId = item.productId,
                    quantity = item.quantity,
                    unitCostPaise = item.unitCostPaise,
                    discountPercent = item.discountPercent,
                    taxRatePercent = item.taxRatePercent,
                    transportAllocationPaise = 0L, // allocated below
                    effectiveUnitCostPaise = 0L,
                    totalItemCostPaise = taxableAmount + itemTax
                )
            )
        }

        // 2. Pro-rata transport charges allocation across items by value
        val allocatedItems = calculatedItems.map { itm ->
            val ratio = if (subtotalPaise > 0L) (itm.quantity * itm.unitCostPaise) / subtotalPaise.toDouble() else 0.0
            val transportAlloc = (transportChargesPaise * ratio).roundToLong()
            val totalWithTransport = itm.totalItemCostPaise + transportAlloc
            val effectiveUnit = if (itm.quantity > 0.0) (totalWithTransport / itm.quantity).roundToLong() else itm.unitCostPaise

            itm.copy(
                transportAllocationPaise = transportAlloc,
                effectiveUnitCostPaise = effectiveUnit,
                totalItemCostPaise = totalWithTransport
            )
        }

        val totalEffectiveDiscount = totalItemDiscountPaise + discountPaise
        val grandTotalPaise = (subtotalPaise - totalEffectiveDiscount).coerceAtLeast(0L) + totalTaxPaise + transportChargesPaise

        // 3. Insert Purchase
        val purchase = Purchase(
            supplierId = supplierId,
            invoiceNumber = invoiceNumber.trim(),
            purchaseDate = purchaseDate,
            subtotalPaise = subtotalPaise,
            discountPaise = totalEffectiveDiscount,
            taxPaise = totalTaxPaise,
            transportChargesPaise = transportChargesPaise,
            totalAmountPaise = grandTotalPaise,
            status = PurchaseStatus.DRAFT,
            notes = notes.trim()
        )

        val purchaseId = purchaseDao.insertPurchase(purchase)

        // 4. Insert Line Items
        itemDao.insertItems(allocatedItems.map { it.copy(purchaseId = purchaseId) })

        Result.success(purchaseId)
    }

    /**
     * Confirms a purchase in an ATOMIC transaction:
     * 1. Checks status == DRAFT (prevents double confirmation).
     * 2. For each purchase item, creates an immutable PURCHASE_RECEIPT stock movement.
     * 3. Updates Product.currentStock and updates cost basis to effective unit cost.
     * 4. Updates Purchase status to CONFIRMED.
     */
    suspend fun confirmPurchase(purchaseId: Long): Result<Unit> = database.withTransaction {
        val purchase = purchaseDao.getPurchaseDirect(purchaseId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Purchase #$purchaseId not found"))

        if (purchase.status != PurchaseStatus.DRAFT) {
            return@withTransaction Result.failure(IllegalStateException("Purchase #$purchaseId is already ${purchase.status}"))
        }

        // Double check against existing movements to prevent duplicate stock entry
        val existingMovements = movementDao.countMovementsForReference("PURCHASE", purchaseId)
        if (existingMovements > 0) {
            return@withTransaction Result.failure(IllegalStateException("Stock movements already exist for Purchase #$purchaseId"))
        }

        val items = itemDao.getItemsForPurchaseDirect(purchaseId)
        if (items.isEmpty()) {
            return@withTransaction Result.failure(IllegalStateException("Cannot confirm purchase without line items"))
        }

        val now = System.currentTimeMillis()

        for (item in items) {
            val product = productDao.getProductDirect(item.productId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Product #${item.productId} not found"))

            val newStock = product.currentStock + item.quantity
            val movement = StockMovement(
                productId = item.productId,
                movementType = StockMovementType.PURCHASE_RECEIPT,
                quantityChange = item.quantity,
                balanceAfter = newStock,
                referenceType = "PURCHASE",
                referenceId = purchaseId,
                unitCostPaise = item.effectiveUnitCostPaise,
                reasonOrNotes = "Inward invoice: ${purchase.invoiceNumber.ifBlank { "#$purchaseId" }}",
                timestamp = now
            )

            movementDao.insertMovement(movement)
            // Update stock and cost price on product
            productDao.updateStock(item.productId, newStock, now)
            if (item.effectiveUnitCostPaise > 0L) {
                productDao.updateProduct(product.copy(costPricePaise = item.effectiveUnitCostPaise, currentStock = newStock, updatedAt = now))
            }
        }

        purchaseDao.updateStatus(purchaseId, PurchaseStatus.CONFIRMED, confirmedAt = now, timestamp = now)
        Result.success(Unit)
    }

    /**
     * Cancels a purchase safely.
     * If the purchase was CONFIRMED, creates compensatory SUPPLIER_RETURN movements in an atomic transaction.
     */
    suspend fun cancelPurchase(purchaseId: Long, reason: String): Result<Unit> = database.withTransaction {
        val purchase = purchaseDao.getPurchaseDirect(purchaseId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Purchase #$purchaseId not found"))

        if (purchase.status == PurchaseStatus.CANCELLED) {
            return@withTransaction Result.failure(IllegalStateException("Purchase #$purchaseId is already cancelled"))
        }

        val now = System.currentTimeMillis()

        if (purchase.status == PurchaseStatus.CONFIRMED) {
            val items = itemDao.getItemsForPurchaseDirect(purchaseId)

            // Verify stock availability before reversal
            for (item in items) {
                val product = productDao.getProductDirect(item.productId)
                    ?: return@withTransaction Result.failure(IllegalArgumentException("Product #${item.productId} not found"))

                if (product.currentStock < item.quantity) {
                    return@withTransaction Result.failure(
                        IllegalStateException("Cannot cancel purchase: Stock for '${product.name}' was already consumed (${product.currentStock} < ${item.quantity})")
                    )
                }
            }

            // Execute reversal movements
            for (item in items) {
                val product = productDao.getProductDirect(item.productId)!!
                val newStock = product.currentStock - item.quantity

                val movement = StockMovement(
                    productId = item.productId,
                    movementType = StockMovementType.SUPPLIER_RETURN,
                    quantityChange = -item.quantity,
                    balanceAfter = newStock,
                    referenceType = "PURCHASE_CANCEL",
                    referenceId = purchaseId,
                    unitCostPaise = item.effectiveUnitCostPaise,
                    reasonOrNotes = "Cancellation reversal: ${reason.ifBlank { "Invoice cancelled" }}",
                    timestamp = now
                )

                movementDao.insertMovement(movement)
                productDao.updateStock(item.productId, newStock, now)
            }
        }

        purchaseDao.updateStatus(purchaseId, PurchaseStatus.CANCELLED, confirmedAt = null, timestamp = now)
        Result.success(Unit)
    }
}

