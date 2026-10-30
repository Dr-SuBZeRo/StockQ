package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.BusinessExpense
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseTotal(
    val category: String,
    val totalAmountPaise: Long
)

@Dao
interface BusinessExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: BusinessExpense): Long

    @Update
    suspend fun updateExpense(expense: BusinessExpense)

    @Delete
    suspend fun deleteExpense(expense: BusinessExpense)

    @Query("SELECT * FROM business_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): BusinessExpense?

    @Query("SELECT * FROM business_expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<BusinessExpense>>

    @Query("SELECT * FROM business_expenses WHERE expenseDate >= :startDate AND expenseDate <= :endDate ORDER BY expenseDate DESC")
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<BusinessExpense>>

    @Query("SELECT * FROM business_expenses WHERE category = :category ORDER BY expenseDate DESC")
    fun getExpensesByCategory(category: String): Flow<List<BusinessExpense>>

    @Query("SELECT * FROM business_expenses WHERE linkedJobId = :jobId ORDER BY expenseDate DESC")
    fun getExpensesForJob(jobId: Long): Flow<List<BusinessExpense>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM business_expenses WHERE expenseDate >= :startOfDay AND expenseDate < :endOfDay")
    fun getTodayExpensesSum(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM business_expenses WHERE expenseDate >= :startDate AND expenseDate <= :endDate")
    fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT category, COALESCE(SUM(amountPaise), 0) AS totalAmountPaise
        FROM business_expenses
        WHERE expenseDate >= :startDate AND expenseDate <= :endDate
        GROUP BY category
        ORDER BY totalAmountPaise DESC
    """)
    fun getCategoryBreakdown(startDate: Long, endDate: Long): Flow<List<CategoryExpenseTotal>>
}

