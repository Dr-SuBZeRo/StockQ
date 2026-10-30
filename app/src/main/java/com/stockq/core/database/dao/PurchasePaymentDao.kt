package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.PurchasePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchasePaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PurchasePayment): Long

    @Update
    suspend fun updatePayment(payment: PurchasePayment)

    @Delete
    suspend fun deletePayment(payment: PurchasePayment)

    @Query("SELECT * FROM purchase_payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): PurchasePayment?

    @Query("SELECT * FROM purchase_payments WHERE purchaseId = :purchaseId ORDER BY paymentDate DESC")
    fun getPaymentsForPurchase(purchaseId: Long): Flow<List<PurchasePayment>>

    @Query("SELECT * FROM purchase_payments WHERE supplierId = :supplierId ORDER BY paymentDate DESC")
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<PurchasePayment>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM purchase_payments WHERE purchaseId = :purchaseId")
    suspend fun getTotalPaidForPurchase(purchaseId: Long): Long

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM purchase_payments WHERE paymentDate >= :startOfDay AND paymentDate < :endOfDay")
    fun getTodayPurchasePaymentsSum(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query("SELECT * FROM purchase_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PurchasePayment>>
}

