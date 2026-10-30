package com.stockq.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.CategoryExpenseTotal
import com.stockq.core.database.entity.BusinessExpense
import com.stockq.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class ExpensesViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val expenses: StateFlow<List<BusinessExpense>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotalPaise: StateFlow<Long> = expenseRepository.getTodayExpensesSum(startOfDay, endOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val categoryBreakdown: StateFlow<List<CategoryExpenseTotal>> =
        expenseRepository.getCategoryBreakdown(startOfDay - (30L * 24 * 60 * 60 * 1000), endOfDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun addExpense(
        category: String,
        title: String,
        amountRupees: String,
        paymentMode: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val amountPaise = (amountRupees.toDoubleOrNull()?.times(100))?.toLong()
        if (amountPaise == null || amountPaise <= 0L) {
            _message.value = "Enter a valid expense amount"
            return
        }

        viewModelScope.launch {
            val result = expenseRepository.createExpense(
                category = category,
                title = title,
                amountPaise = amountPaise,
                paymentMode = paymentMode,
                notes = notes
            )
            result.onSuccess {
                _message.value = "Expense logged successfully"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to log expense"
            }
        }
    }

    fun deleteExpense(expense: BusinessExpense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            _message.value = "Expense deleted"
        }
    }
}

