package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerDirect(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE isArchived = 0 AND outstandingDuePaise > 0 ORDER BY outstandingDuePaise DESC")
    fun getCustomersWithDues(): Flow<List<Customer>>

    @Query("SELECT COALESCE(SUM(outstandingDuePaise), 0) FROM customers WHERE isArchived = 0")
    fun getTotalCustomerDuesPaise(): Flow<Long>

    @Query("SELECT COUNT(*) FROM customers WHERE isArchived = 0")
    fun getActiveCustomerCount(): Flow<Int>

    @Query("SELECT * FROM customers WHERE isArchived = 0 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET outstandingDuePaise = :newDuePaise, updatedAt = :timestamp WHERE id = :customerId")
    suspend fun updateDue(customerId: Long, newDuePaise: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :customerId")
    suspend fun setArchived(customerId: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

