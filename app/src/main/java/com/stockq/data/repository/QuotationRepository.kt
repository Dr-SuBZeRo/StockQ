package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.QuotationWithItems
import com.stockq.core.database.entity.Quotation
import com.stockq.core.database.entity.QuotationItem
import com.stockq.core.database.entity.QuotationStatus
import com.stockq.core.model.Money
import com.stockq.core.model.Quantity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuotationItemDraftInput(
    val productId: Long? = null,
    val itemTitle: String,
    val isService: Boolean = false,
    val quantity: Double,
    val unitPricePaise: Long,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0
)

class QuotationRepository(
    private val database: StockQDatabase,
    private val saleRepository: SaleRepository
) {
    private val quotationDao = database.quotationDao()

    fun getAllQuotations(): Flow<List<QuotationWithItems>> = quotationDao.getAllQuotationsWithItems()

    fun getQuotationsByStatus(status: QuotationStatus): Flow<List<QuotationWithItems>> =
        quotationDao.getQuotationsByStatus(status)

    fun getPendingQuotationsCount(): Flow<Int> = quotationDao.getPendingQuotationsCount()

    suspend fun getQuotationById(id: Long): QuotationWithItems? = quotationDao.getQuotationWithItemsById(id)

    suspend fun createQuotation(
        customerId: Long?,
        customerName: String,
        customerPhone: String = "",
        validityDays: Int = 14,
        items: List<QuotationItemDraftInput>,
        overallDiscountPaise: Long = 0L,
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (items.isEmpty()) {
            return@withTransaction Result.failure(IllegalArgumentException("Quotation must have at least one line item"))
        }

        val now = System.currentTimeMillis()
        val quoteNumber = "QT-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(now))
        val validUntil = now + (validityDays.toLong() * 24 * 60 * 60 * 1000)

        var subtotalPaise = 0L
        var totalItemDiscountPaise = 0L
        var totalTaxPaise = 0L
        val calculatedItems = mutableListOf<QuotationItem>()

        for (item in items) {
            val qtyMilli = Quantity.fromDouble(item.quantity)
            val itemBase = Quantity.multiplyMoney(qtyMilli, item.unitPricePaise)
            val itemDiscount = Money.calculateDiscountPaise(itemBase, item.discountPercent)
            val taxable = (itemBase - itemDiscount).coerceAtLeast(0L)
            val itemTax = Money.calculateTaxPaise(taxable, item.taxRatePercent)
            val totalItemPrice = taxable + itemTax

            subtotalPaise += itemBase
            totalItemDiscountPaise += itemDiscount
            totalTaxPaise += itemTax

            calculatedItems.add(
                QuotationItem(
                    quotationId = 0L,
                    productId = item.productId,
                    itemTitle = item.itemTitle.trim(),
                    isService = item.isService,
                    quantityMilli = qtyMilli,
                    unitPricePaise = item.unitPricePaise,
                    discountPercent = item.discountPercent,
                    taxRatePercent = item.taxRatePercent,
                    totalItemPricePaise = totalItemPrice
                )
            )
        }

        val totalDiscount = totalItemDiscountPaise + overallDiscountPaise
        val grandTotal = (subtotalPaise - totalDiscount).coerceAtLeast(0L) + totalTaxPaise

        val quotation = Quotation(
            customerId = customerId,
            customerName = customerName.trim().ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone.trim(),
            quotationNumber = quoteNumber,
            quotationDate = now,
            validUntilDate = validUntil,
            subtotalPaise = subtotalPaise,
            discountPaise = totalDiscount,
            taxPaise = totalTaxPaise,
            totalAmountPaise = grandTotal,
            status = QuotationStatus.SENT,
            notes = notes.trim()
        )

        val id = quotationDao.insertQuotationWithItems(quotation, calculatedItems)
        Result.success(id)
    }

    suspend fun updateStatus(quotationId: Long, status: QuotationStatus) {
        quotationDao.updateStatus(quotationId, status)
    }

    /**
     * Converts a quotation to a Sale in a single tap.
     */
    suspend fun convertToSale(
        quotationId: Long,
        paymentMode: String = "CASH",
        paidAmountPaise: Long? = null
    ): Result<Long> = database.withTransaction {
        val quoteWithItems = quotationDao.getQuotationWithItemsById(quotationId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Quotation #$quotationId not found"))

        if (quoteWithItems.quotation.status == QuotationStatus.CONVERTED_TO_SALE) {
            return@withTransaction Result.failure(IllegalStateException("Quotation #$quotationId is already converted"))
        }

        val saleItemInputs = quoteWithItems.items
            .filter { it.productId != null }
            .map {
                SaleItemInput(
                    productId = it.productId!!,
                    quantity = it.quantity,
                    unitPricePaise = it.unitPricePaise,
                    discountPercent = it.discountPercent
                )
            }

        if (saleItemInputs.isEmpty()) {
            return@withTransaction Result.failure(IllegalArgumentException("Quotation contains no physical inventory products to convert to sale"))
        }

        val saleResult = saleRepository.createSale(
            customerId = quoteWithItems.quotation.customerId,
            customerName = quoteWithItems.quotation.customerName,
            items = saleItemInputs,
            paymentMode = paymentMode,
            paidAmountPaise = paidAmountPaise,
            notes = "Converted from Quotation ${quoteWithItems.quotation.quotationNumber}"
        )

        saleResult.onSuccess { saleId ->
            quotationDao.markAsConverted(quotationId, saleId)
        }

        saleResult
    }
}

