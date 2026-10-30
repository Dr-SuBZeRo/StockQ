package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.stockq.core.database.entity.PaymentStatus
import com.stockq.core.database.entity.Sale
import com.stockq.core.database.entity.SaleItem
import com.stockq.core.database.entity.SaleStatus
import kotlinx.coroutines.flow.Flow

data class SaleWithCustomer(
    val id: Long,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String?,
    val invoiceNumber: String,
    val saleDate: Long,
    val subtotalPaise: Long,
    val discountPaise: Long,
    val taxPaise: Long,
    val totalAmountPaise: Long,
    val paidAmountPaise: Long,
    val balanceDuePaise: Long,
    val dueDate: Long?,
    val paymentMode: String,
    val status: SaleStatus,
    val paymentStatus: PaymentStatus,
    val notes: String
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC, id DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("""
        SELECT 
            s.id AS id,
            s.customerId AS customerId,
            COALESCE(c.name, s.customerName) AS customerName,
            c.phone AS customerPhone,
            s.invoiceNumber AS invoiceNumber,
            s.saleDate AS saleDate,
            s.subtotalPaise AS subtotalPaise,
            s.discountPaise AS discountPaise,
            s.taxPaise AS taxPaise,
            s.totalAmountPaise AS totalAmountPaise,
            s.paidAmountPaise AS paidAmountPaise,
            s.balanceDuePaise AS balanceDuePaise,
            s.dueDate AS dueDate,
            s.paymentMode AS paymentMode,
            s.status AS status,
            s.paymentStatus AS paymentStatus,
            s.notes AS notes
        FROM sales s
        LEFT JOIN customers c ON s.customerId = c.id
        ORDER BY s.saleDate DESC, s.id DESC
    """)
    fun getAllSalesWithCustomer(): Flow<List<SaleWithCustomer>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleDirect(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY saleDate DESC")
    fun getSalesForCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSaleDirect(saleId: Long): List<SaleItem>

    @Query("SELECT COALESCE(SUM(totalAmountPaise), 0) FROM sales WHERE status = 'COMPLETED' AND saleDate >= :startOfDay AND saleDate <= :endOfDay")
    fun getTodaySalesTotalPaise(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(paidAmountPaise), 0) FROM sales WHERE status = 'COMPLETED' AND saleDate >= :startOfDay AND saleDate <= :endOfDay")
    fun getTodayCashInflowPaise(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(si.itemProfitPaise), 0)
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.status = 'COMPLETED' AND s.saleDate >= :startOfDay AND s.saleDate <= :endOfDay
    """)
    fun getTodayGrossProfitPaise(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM sales WHERE status = 'COMPLETED'")
    fun getCompletedSaleCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("UPDATE sales SET paidAmountPaise = :paidPaise, balanceDuePaise = :balanceDue, paymentStatus = :paymentStatus, updatedAt = :timestamp WHERE id = :saleId")
    suspend fun updatePayment(saleId: Long, paidPaise: Long, balanceDue: Long, paymentStatus: PaymentStatus, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sales SET status = :status, updatedAt = :timestamp WHERE id = :saleId")
    suspend fun updateStatus(saleId: Long, status: SaleStatus, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSale(sale: Sale)
}

