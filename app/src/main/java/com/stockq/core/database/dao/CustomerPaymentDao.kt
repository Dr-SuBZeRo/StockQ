package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stockq.core.database.entity.CustomerPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerPaymentDao {
    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPayment>>

    @Query("SELECT * FROM customer_payments WHERE saleId = :saleId ORDER BY paymentDate DESC")
    fun getPaymentsForSale(saleId: Long): Flow<List<CustomerPayment>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM customer_payments WHERE paymentDate >= :startOfDay AND paymentDate <= :endOfDay")
    fun getTodayCollectionsPaise(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPayment): Long
}

