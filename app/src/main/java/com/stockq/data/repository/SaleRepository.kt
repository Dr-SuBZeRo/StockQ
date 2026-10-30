package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.SaleWithCustomer
import com.stockq.core.database.entity.CustomerPayment
import com.stockq.core.database.entity.PaymentStatus
import com.stockq.core.database.entity.Sale
import com.stockq.core.database.entity.SaleItem
import com.stockq.core.database.entity.SaleStatus
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.model.Money
import com.stockq.core.model.Quantity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SaleItemInput(
    val productId: Long,
    val quantity: Double,
    val unitPricePaise: Long,
    val discountPercent: Double = 0.0,
    val batchId: Long? = null
)

class SaleRepository(private val database: StockQDatabase) {

    private val saleDao = database.saleDao()
    private val productDao = database.productDao()
    private val batchDao = database.batchDao()
    private val movementDao = database.stockMovementDao()
    private val customerDao = database.customerDao()
    private val paymentDao = database.customerPaymentDao()

    fun getAllSales(): Flow<List<Sale>> = saleDao.getAllSales()

    fun getAllSalesWithCustomer(): Flow<List<SaleWithCustomer>> = saleDao.getAllSalesWithCustomer()

    fun getSaleById(id: Long): Flow<Sale?> = saleDao.getSaleById(id)

    suspend fun getSaleDirect(id: Long): Sale? = saleDao.getSaleDirect(id)

    fun getItemsForSale(saleId: Long): Flow<List<SaleItem>> = saleDao.getItemsForSale(saleId)

    fun getSalesForCustomer(customerId: Long): Flow<List<Sale>> = saleDao.getSalesForCustomer(customerId)

    fun getTodaySalesTotalPaise(startOfDay: Long, endOfDay: Long): Flow<Long> =
        saleDao.getTodaySalesTotalPaise(startOfDay, endOfDay)

    fun getTodayCashInflowPaise(startOfDay: Long, endOfDay: Long): Flow<Long> =
        saleDao.getTodayCashInflowPaise(startOfDay, endOfDay)

    fun getTodayGrossProfitPaise(startOfDay: Long, endOfDay: Long): Flow<Long> =
        saleDao.getTodayGrossProfitPaise(startOfDay, endOfDay)

    fun getCompletedSaleCount(): Flow<Int> = saleDao.getCompletedSaleCount()

    /**
     * Executes an atomic sale transaction:
     * 1. Validates physical inventory availability (strictly enforces negative-stock prevention).
     * 2. Decrements Product stock and issues StockMovement audit entries.
     * 3. Decrements FIFO batch inventory if applicable.
     * 4. Calculates COGS and profit snapshots.
     * 5. Tracks customer dues if partially paid or credit.
     */
    suspend fun createSale(
        customerId: Long?,
        customerName: String,
        items: List<SaleItemInput>,
        paymentMode: String = "CASH",
        paidAmountPaise: Long? = null,
        overallDiscountPaise: Long = 0L,
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (items.isEmpty()) {
            return@withTransaction Result.failure(IllegalArgumentException("Sale must have at least one line item"))
        }

        val now = System.currentTimeMillis()
        val invoiceNumber = "INV-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(now))

        var subtotalPaise = 0L
        var totalItemDiscountPaise = 0L
        var totalTaxPaise = 0L
        val calculatedSaleItems = mutableListOf<SaleItem>()

        // 1. Validation & calculations
        for (item in items) {
            val qtyMilli = Quantity.fromDouble(item.quantity)
            if (qtyMilli <= 0L) {
                return@withTransaction Result.failure(IllegalArgumentException("Quantity must be greater than zero"))
            }

            val product = productDao.getProductDirect(item.productId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Product #${item.productId} not found"))

            if (product.currentStockMilli < qtyMilli) {
                return@withTransaction Result.failure(
                    IllegalStateException(
                        "Insufficient stock for '${product.name}'. Available: ${product.currentStock} ${product.unit}, requested: ${item.quantity}"
                    )
                )
            }

            val itemBase = Quantity.multiplyMoney(qtyMilli, item.unitPricePaise)
            val itemDiscount = Money.calculateDiscountPaise(itemBase, item.discountPercent)
            val taxableAmount = (itemBase - itemDiscount).coerceAtLeast(0L)
            val itemTax = Money.calculateTaxPaise(taxableAmount, product.taxRatePercent)
            val totalItemPrice = taxableAmount + itemTax

            val itemCostBasis = Quantity.multiplyMoney(qtyMilli, product.costPricePaise)
            val itemProfit = totalItemPrice - itemCostBasis

            subtotalPaise += itemBase
            totalItemDiscountPaise += itemDiscount
            totalTaxPaise += itemTax

            calculatedSaleItems.add(
                SaleItem(
                    saleId = 0L,
                    productId = item.productId,
                    batchId = item.batchId,
                    productName = product.name,
                    productUnit = product.unit,
                    quantityMilli = qtyMilli,
                    unitPricePaise = item.unitPricePaise,
                    costPricePaise = product.costPricePaise,
                    discountPercent = item.discountPercent,
                    taxRatePercent = product.taxRatePercent,
                    totalItemPricePaise = totalItemPrice,
                    itemProfitPaise = itemProfit
                )
            )
        }

        val totalDiscountPaise = totalItemDiscountPaise + overallDiscountPaise
        val grandTotalPaise = (subtotalPaise - totalDiscountPaise).coerceAtLeast(0L) + totalTaxPaise

        val finalPaidAmount = (paidAmountPaise ?: grandTotalPaise).coerceAtLeast(0L).coerceAtMost(grandTotalPaise)
        val balanceDue = grandTotalPaise - finalPaidAmount

        val paymentStatus = when {
            finalPaidAmount >= grandTotalPaise -> PaymentStatus.PAID
            finalPaidAmount > 0L -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }

        // 2. Insert Sale
        val resolvedCustomerName = if (customerId != null) {
            val c = customerDao.getCustomerDirect(customerId)
            c?.name ?: customerName.ifBlank { "Walk-in Customer" }
        } else {
            customerName.ifBlank { "Walk-in Customer" }
        }

        val sale = Sale(
            customerId = customerId,
            customerName = resolvedCustomerName,
            invoiceNumber = invoiceNumber,
            saleDate = now,
            subtotalPaise = subtotalPaise,
            discountPaise = totalDiscountPaise,
            taxPaise = totalTaxPaise,
            totalAmountPaise = grandTotalPaise,
            paidAmountPaise = finalPaidAmount,
            balanceDuePaise = balanceDue,
            dueDate = if (balanceDue > 0) now + (30L * 24 * 60 * 60 * 1000) else null,
            paymentMode = paymentMode,
            status = SaleStatus.COMPLETED,
            paymentStatus = paymentStatus,
            notes = notes.trim(),
            createdAt = now,
            updatedAt = now
        )

        val saleId = saleDao.insertSale(sale)

        // 3. Insert SaleItems and update stock ledger & batches
        for (saleItem in calculatedSaleItems) {
            val product = productDao.getProductDirect(saleItem.productId)!!
            val newStockMilli = product.currentStockMilli - saleItem.quantityMilli

            // FIFO batch deduction if batch specified or available
            var resolvedBatchId = saleItem.batchId
            if (resolvedBatchId != null) {
                val batch = batchDao.findByProductAndBatchNumber(saleItem.productId, "")
            } else {
                val sellableBatches = batchDao.getSellableBatchesFIFO(saleItem.productId, now)
                var remainingToDeduct = saleItem.quantityMilli
                for (batch in sellableBatches) {
                    if (remainingToDeduct <= 0L) break
                    val deductFromThisBatch = minOf(batch.currentStockMilli, remainingToDeduct)
                    batchDao.updateStock(batch.id, batch.currentStockMilli - deductFromThisBatch)
                    remainingToDeduct -= deductFromThisBatch
                    if (resolvedBatchId == null) resolvedBatchId = batch.id
                }
            }

            val movement = StockMovement(
                productId = saleItem.productId,
                batchId = resolvedBatchId,
                movementType = StockMovementType.SALE_ISSUE,
                quantityChangeMilli = -saleItem.quantityMilli,
                balanceAfterMilli = newStockMilli,
                referenceType = "SALE",
                referenceId = saleId,
                unitCostPaise = saleItem.costPricePaise,
                reasonOrNotes = "Sale invoice: $invoiceNumber",
                timestamp = now
            )

            movementDao.insertMovement(movement)
            productDao.updateStock(saleItem.productId, newStockMilli, now)
        }

        saleDao.insertSaleItems(calculatedSaleItems.map { it.copy(saleId = saleId) })

        // 4. Update Customer dues if balance remains
        if (customerId != null) {
            val customer = customerDao.getCustomerDirect(customerId)
            if (customer != null) {
                if (balanceDue > 0L) {
                    customerDao.updateDue(customerId, customer.outstandingDuePaise + balanceDue, now)
                }
                if (finalPaidAmount > 0L) {
                    paymentDao.insertPayment(
                        CustomerPayment(
                            customerId = customerId,
                            saleId = saleId,
                            amountPaise = finalPaidAmount,
                            paymentMode = paymentMode,
                            referenceNumber = invoiceNumber,
                            notes = "Initial payment at billing",
                            paymentDate = now
                        )
                    )
                }
            }
        }

        Result.success(saleId)
    }

    /**
     * Reverses a sale safely. Restores inventory and adjusts customer dues.
     */
    suspend fun cancelSale(saleId: Long, reason: String): Result<Unit> = database.withTransaction {
        val sale = saleDao.getSaleDirect(saleId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Sale #$saleId not found"))

        if (sale.status == SaleStatus.CANCELLED) {
            return@withTransaction Result.failure(IllegalStateException("Sale #$saleId is already cancelled"))
        }

        val now = System.currentTimeMillis()
        val items = saleDao.getItemsForSaleDirect(saleId)

        for (item in items) {
            val product = productDao.getProductDirect(item.productId) ?: continue
            val newStockMilli = product.currentStockMilli + item.quantityMilli

            val movement = StockMovement(
                productId = item.productId,
                movementType = StockMovementType.CUSTOMER_RETURN,
                quantityChangeMilli = item.quantityMilli,
                balanceAfterMilli = newStockMilli,
                referenceType = "SALE_CANCEL",
                referenceId = saleId,
                unitCostPaise = item.costPricePaise,
                reasonOrNotes = "Sale cancellation: ${reason.ifBlank { "Reversed" }}",
                timestamp = now
            )

            movementDao.insertMovement(movement)
            productDao.updateStock(item.productId, newStockMilli, now)
        }

        // Reverse dues if unpaid balance was charged to customer
        if (sale.customerId != null && sale.balanceDuePaise > 0L) {
            val customer = customerDao.getCustomerDirect(sale.customerId)
            if (customer != null) {
                val adjustedDue = (customer.outstandingDuePaise - sale.balanceDuePaise).coerceAtLeast(0L)
                customerDao.updateDue(sale.customerId, adjustedDue, now)
            }
        }

        saleDao.updateStatus(saleId, SaleStatus.CANCELLED, now)
        Result.success(Unit)
    }
}

