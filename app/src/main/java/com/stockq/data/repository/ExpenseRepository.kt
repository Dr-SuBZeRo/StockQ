package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.CategoryExpenseTotal
import com.stockq.core.database.entity.BusinessExpense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val database: StockQDatabase) {

    private val expenseDao = database.businessExpenseDao()

    fun getAllExpenses(): Flow<List<BusinessExpense>> = expenseDao.getAllExpenses()

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<BusinessExpense>> =
        expenseDao.getExpensesBetweenDates(startDate, endDate)

    fun getExpensesByCategory(category: String): Flow<List<BusinessExpense>> =
        expenseDao.getExpensesByCategory(category)

    fun getTodayExpensesSum(startOfDay: Long, endOfDay: Long): Flow<Long> =
        expenseDao.getTodayExpensesSum(startOfDay, endOfDay)

    fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Long> =
        expenseDao.getTotalExpensesBetween(startDate, endDate)

    fun getCategoryBreakdown(startDate: Long, endDate: Long): Flow<List<CategoryExpenseTotal>> =
        expenseDao.getCategoryBreakdown(startDate, endDate)

    suspend fun getExpenseById(id: Long): BusinessExpense? = expenseDao.getExpenseById(id)

    suspend fun createExpense(
        category: String,
        title: String,
        amountPaise: Long,
        expenseDate: Long = System.currentTimeMillis(),
        paymentMode: String = "CASH",
        referenceId: String = "",
        linkedJobId: Long? = null,
        notes: String = ""
    ): Result<Long> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Expense title cannot be empty"))
        }
        if (amountPaise <= 0L) {
            return Result.failure(IllegalArgumentException("Expense amount must be greater than zero"))
        }

        val expense = BusinessExpense(
            category = category.trim().ifBlank { "General" },
            title = title.trim(),
            amountPaise = amountPaise,
            expenseDate = expenseDate,
            paymentMode = paymentMode,
            referenceId = referenceId.trim(),
            linkedJobId = linkedJobId,
            notes = notes.trim()
        )

        val id = expenseDao.insertExpense(expense)
        return Result.success(id)
    }

    suspend fun deleteExpense(expense: BusinessExpense) {
        expenseDao.deleteExpense(expense)
    }
}

