package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.SupplierQuotation
import kotlinx.coroutines.flow.Flow

data class SupplierQuoteComparison(
    val quoteId: Long,
    val supplierId: Long,
    val supplierName: String,
    val supplierPhone: String,
    val quotationRef: String,
    val quoteDate: Long,
    val validUntilDate: Long,
    val unitCostPaise: Long,
    val discountPercent: Double,
    val taxRatePercent: Double,
    val freightPaise: Long,
    val effectiveUnitCostPaise: Long,
    val notes: String
)

@Dao
interface SupplierQuotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SupplierQuotation): Long

    @Update
    suspend fun updateQuote(quote: SupplierQuotation)

    @Delete
    suspend fun deleteQuote(quote: SupplierQuotation)

    @Query("SELECT * FROM supplier_quotations WHERE id = :id")
    suspend fun getQuoteById(id: Long): SupplierQuotation?

    @Query("SELECT * FROM supplier_quotations WHERE productId = :productId AND isArchived = 0 ORDER BY effectiveUnitCostPaise ASC")
    fun getQuotesForProduct(productId: Long): Flow<List<SupplierQuotation>>

    @Query("""
        SELECT 
            sq.id AS quoteId,
            sq.supplierId,
            s.name AS supplierName,
            s.phone AS supplierPhone,
            sq.quotationRef,
            sq.quoteDate,
            sq.validUntilDate,
            sq.unitCostPaise,
            sq.discountPercent,
            sq.taxRatePercent,
            sq.freightPaise,
            sq.effectiveUnitCostPaise,
            sq.notes
        FROM supplier_quotations sq
        INNER JOIN suppliers s ON s.id = sq.supplierId
        WHERE sq.productId = :productId AND sq.isArchived = 0
        ORDER BY sq.effectiveUnitCostPaise ASC
    """)
    fun compareSupplierQuotesForProduct(productId: Long): Flow<List<SupplierQuoteComparison>>

    @Query("SELECT * FROM supplier_quotations WHERE supplierId = :supplierId ORDER BY quoteDate DESC")
    fun getQuotesBySupplier(supplierId: Long): Flow<List<SupplierQuotation>>

    @Query("SELECT * FROM supplier_quotations WHERE isArchived = 0 ORDER BY quoteDate DESC")
    fun getAllActiveQuotes(): Flow<List<SupplierQuotation>>
}

