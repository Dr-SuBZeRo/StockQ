package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.stockq.core.database.entity.Quotation
import com.stockq.core.database.entity.QuotationItem
import com.stockq.core.database.entity.QuotationStatus
import kotlinx.coroutines.flow.Flow

data class QuotationWithItems(
    @Embedded val quotation: Quotation,
    @Relation(
        parentColumn = "id",
        entityColumn = "quotationId"
    )
    val items: List<QuotationItem>
)

@Dao
interface QuotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: Quotation): Long

    @Update
    suspend fun updateQuotation(quotation: Quotation)

    @Delete
    suspend fun deleteQuotation(quotation: Quotation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotationItems(items: List<QuotationItem>)

    @Query("DELETE FROM quotation_items WHERE quotationId = :quotationId")
    suspend fun deleteItemsForQuotation(quotationId: Long)

    @Transaction
    suspend fun insertQuotationWithItems(quotation: Quotation, items: List<QuotationItem>): Long {
        val quotationId = insertQuotation(quotation)
        val itemsWithId = items.map { it.copy(quotationId = quotationId) }
        insertQuotationItems(itemsWithId)
        return quotationId
    }

    @Transaction
    @Query("SELECT * FROM quotations WHERE id = :id")
    suspend fun getQuotationWithItemsById(id: Long): QuotationWithItems?

    @Transaction
    @Query("SELECT * FROM quotations ORDER BY quotationDate DESC")
    fun getAllQuotationsWithItems(): Flow<List<QuotationWithItems>>

    @Transaction
    @Query("SELECT * FROM quotations WHERE status = :status ORDER BY quotationDate DESC")
    fun getQuotationsByStatus(status: QuotationStatus): Flow<List<QuotationWithItems>>

    @Query("UPDATE quotations SET status = :status, updatedAt = :updatedAt WHERE id = :quotationId")
    suspend fun updateStatus(quotationId: Long, status: QuotationStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE quotations SET status = 'CONVERTED_TO_SALE', convertedSaleId = :saleId, updatedAt = :updatedAt WHERE id = :quotationId")
    suspend fun markAsConverted(quotationId: Long, saleId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM quotations WHERE status IN ('DRAFT', 'SENT')")
    fun getPendingQuotationsCount(): Flow<Int>

    @Query("SELECT * FROM quotations WHERE quotationNumber = :quotationNumber LIMIT 1")
    suspend fun getQuotationByNumber(quotationNumber: String): Quotation?
}

