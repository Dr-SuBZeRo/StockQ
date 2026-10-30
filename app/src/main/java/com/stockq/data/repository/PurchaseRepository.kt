package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.Batch
import com.stockq.core.database.entity.PaymentStatus
import com.stockq.core.database.entity.Purchase
import com.stockq.core.database.entity.PurchaseItem
import com.stockq.core.database.entity.PurchasePayment
import com.stockq.core.database.entity.PurchaseStatus
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.model.Money
import com.stockq.core.model.Quantity
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToLong

data class PurchaseDraftItemInput(
    val productId: Long,
    val quantity: Double,
    val unitCostPaise: Long,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val batchNumber: String? = null,
    val expiryDate: Long? = null
)

class PurchaseRepository(private val database: StockQDatabase) {

    private val purchaseDao = database.purchaseDao()
    private val itemDao = database.purchaseItemDao()
    private val paymentDao = database.purchasePaymentDao()
    private val batchDao = database.batchDao()
    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()

    fun getAllPurchases(): Flow<List<Purchase>> = purchaseDao.getAllPurchases()

    fun getAllPurchasesWithSupplier(): Flow<List<com.stockq.core.database.dao.PurchaseWithSupplier>> =
        purchaseDao.getAllPurchasesWithSupplier()

    fun getPurchaseById(id: Long): Flow<Purchase?> = purchaseDao.getPurchaseById(id)

    suspend fun getPurchaseDirect(id: Long): Purchase? = purchaseDao.getPurchaseDirect(id)

    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItem>> = itemDao.getItemsForPurchase(purchaseId)

    fun getPaymentsForPurchase(purchaseId: Long): Flow<List<PurchasePayment>> = paymentDao.getPaymentsForPurchase(purchaseId)

    fun getPendingSupplierDuesPaise(): Flow<Long> = purchaseDao.getPendingSupplierDuesPaise()

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
        val itemWeights = mutableListOf<Long>()

        for (item in items) {
            val quantityMilli = Quantity.fromDouble(item.quantity)
            if (quantityMilli <= 0L) {
                return@withTransaction Result.failure(IllegalArgumentException("Quantity must be greater than zero"))
            }
            if (item.unitCostPaise < 0L) {
                return@withTransaction Result.failure(IllegalArgumentException("Unit cost cannot be negative"))
            }

            val itemBase = Quantity.multiplyMoney(quantityMilli, item.unitCostPaise)
            val itemDiscount = Money.calculateDiscountPaise(itemBase, item.discountPercent)
            val taxableAmount = (itemBase - itemDiscount).coerceAtLeast(0L)
            val itemTax = Money.calculateTaxPaise(taxableAmount, item.taxRatePercent)

            subtotalPaise += itemBase
            totalItemDiscountPaise += itemDiscount
            totalTaxPaise += itemTax
            itemWeights.add(itemBase)

            calculatedItems.add(
                PurchaseItem(
                    purchaseId = 0L,
                    productId = item.productId,
                    batchNumber = item.batchNumber?.trim()?.ifBlank { null },
                    expiryDate = item.expiryDate,
                    quantityMilli = quantityMilli,
                    unitCostPaise = item.unitCostPaise,
                    discountPercent = item.discountPercent,
                    taxRatePercent = item.taxRatePercent,
                    transportAllocationPaise = 0L,
                    effectiveUnitCostPaise = 0L,
                    totalItemCostPaise = taxableAmount + itemTax
                )
            )
        }

        // 2. Pro-rata transport charges allocation across items by value using largest remainder method
        val transportAllocations = if (transportChargesPaise > 0L) {
            Money.allocateProRata(transportChargesPaise, itemWeights)
        } else {
            List(calculatedItems.size) { 0L }
        }

        val allocatedItems = calculatedItems.mapIndexed { index, itm ->
            val transportAlloc = transportAllocations[index]
            val totalWithTransport = itm.totalItemCostPaise + transportAlloc
            val effectiveUnit = if (itm.quantityMilli > 0L) {
                (totalWithTransport * 1000L) / itm.quantityMilli
            } else {
                itm.unitCostPaise
            }

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
            paymentStatus = PaymentStatus.PENDING,
            paidAmountPaise = 0L,
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
     * 3. Creates Batch record if batch/expiry is specified.
     * 4. Updates Product.currentStockMilli and updates cost basis to effective unit cost.
     * 5. Updates Purchase status to CONFIRMED.
     */
    suspend fun confirmPurchase(purchaseId: Long): Result<Unit> = database.withTransaction {
        val purchase = purchaseDao.getPurchaseDirect(purchaseId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Purchase #$purchaseId not found"))

        if (purchase.status != PurchaseStatus.DRAFT) {
            return@withTransaction Result.failure(IllegalStateException("Purchase #$purchaseId is already ${purchase.status}"))
        }

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

            // Create batch if batchNumber or expiryDate is present
            var batchId: Long? = null
            if (!item.batchNumber.isNullOrBlank() && item.expiryDate != null) {
                batchId = batchDao.insertBatch(
                    Batch(
                        productId = item.productId,
                        batchNumber = item.batchNumber.trim(),
                        expiryDate = item.expiryDate,
                        currentStockMilli = item.quantityMilli,
                        purchaseItemId = item.id,
                        purchasePricePaise = item.effectiveUnitCostPaise
                    )
                )
            }

            val newStockMilli = product.currentStockMilli + item.quantityMilli
            val movement = StockMovement(
                productId = item.productId,
                batchId = batchId,
                movementType = StockMovementType.PURCHASE_RECEIPT,
                quantityChangeMilli = item.quantityMilli,
                balanceAfterMilli = newStockMilli,
                referenceType = "PURCHASE",
                referenceId = purchaseId,
                unitCostPaise = item.effectiveUnitCostPaise,
                reasonOrNotes = "Inward invoice: ${purchase.invoiceNumber.ifBlank { "#$purchaseId" }}",
                timestamp = now
            )

            movementDao.insertMovement(movement)
            productDao.updateStock(item.productId, newStockMilli, now)
            if (item.effectiveUnitCostPaise > 0L) {
                productDao.updateProduct(
                    product.copy(
                        costPricePaise = item.effectiveUnitCostPaise,
                        currentStockMilli = newStockMilli,
                        updatedAt = now
                    )
                )
            }
        }

        purchaseDao.updateStatus(purchaseId, PurchaseStatus.CONFIRMED, confirmedAt = now, timestamp = now)
        Result.success(Unit)
    }

    /**
     * Records a payment to supplier for a confirmed purchase.
     */
    suspend fun recordPurchasePayment(
        purchaseId: Long,
        amountPaise: Long,
        paymentMode: String = "CASH",
        referenceNumber: String = "",
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (amountPaise <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Payment amount must be greater than zero"))
        }

        val purchase = purchaseDao.getPurchaseDirect(purchaseId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Purchase #$purchaseId not found"))

        val newPaidTotal = purchase.paidAmountPaise + amountPaise
        val paymentStatus = when {
            newPaidTotal >= purchase.totalAmountPaise -> PaymentStatus.PAID
            newPaidTotal > 0L -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }

        val paymentId = paymentDao.insertPayment(
            PurchasePayment(
                purchaseId = purchaseId,
                supplierId = purchase.supplierId,
                amountPaise = amountPaise,
                paymentDate = System.currentTimeMillis(),
                paymentMode = paymentMode,
                referenceNumber = referenceNumber.trim(),
                notes = notes.trim()
            )
        )

        purchaseDao.updatePayment(purchaseId, newPaidTotal, paymentStatus)
        Result.success(paymentId)
    }

    /**
     * Cancels a purchase safely.
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

            for (item in items) {
                val product = productDao.getProductDirect(item.productId)
                    ?: return@withTransaction Result.failure(IllegalArgumentException("Product #${item.productId} not found"))

                if (product.currentStockMilli < item.quantityMilli) {
                    return@withTransaction Result.failure(
                        IllegalStateException("Cannot cancel purchase: Stock for '${product.name}' was already consumed (${product.currentStock} < ${item.quantity})")
                    )
                }
            }

            for (item in items) {
                val product = productDao.getProductDirect(item.productId)!!
                val newStockMilli = product.currentStockMilli - item.quantityMilli

                val movement = StockMovement(
                    productId = item.productId,
                    movementType = StockMovementType.SUPPLIER_RETURN,
                    quantityChangeMilli = -item.quantityMilli,
                    balanceAfterMilli = newStockMilli,
                    referenceType = "PURCHASE_CANCEL",
                    referenceId = purchaseId,
                    unitCostPaise = item.effectiveUnitCostPaise,
                    reasonOrNotes = "Cancellation reversal: ${reason.ifBlank { "Invoice cancelled" }}",
                    timestamp = now
                )

                movementDao.insertMovement(movement)
                productDao.updateStock(item.productId, newStockMilli, now)
            }
        }

        purchaseDao.updateStatus(purchaseId, PurchaseStatus.CANCELLED, confirmedAt = null, timestamp = now)
        Result.success(Unit)
    }
}
