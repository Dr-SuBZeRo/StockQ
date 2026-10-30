package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BusinessPnLSummary(
    val startDate: Long,
    val endDate: Long,
    val totalSalesPaise: Long,
    val totalCogsPaise: Long,
    val grossProfitPaise: Long,
    val operatingExpensesPaise: Long,
    val netProfitPaise: Long,
    val cashInflowPaise: Long,
    val cashOutflowPaise: Long,
    val netCashFlowPaise: Long
)

class ReportRepository(private val database: StockQDatabase) {

    private val saleDao = database.saleDao()
    private val expenseDao = database.businessExpenseDao()
    private val paymentDao = database.customerPaymentDao()
    private val purchasePaymentDao = database.purchasePaymentDao()

    fun getDailySummary(startOfDay: Long, endOfDay: Long): Flow<BusinessPnLSummary> {
        val salesFlow = saleDao.getTodaySalesTotalPaise(startOfDay, endOfDay)
        val grossProfitFlow = saleDao.getTodayGrossProfitPaise(startOfDay, endOfDay)
        val expenseFlow = expenseDao.getTodayExpensesSum(startOfDay, endOfDay)
        val cashInflowFlow = paymentDao.getTodayCollectionsPaise(startOfDay, endOfDay)
        val supplierPaymentFlow = purchasePaymentDao.getTodayPurchasePaymentsSum(startOfDay, endOfDay)

        return combine(
            salesFlow,
            grossProfitFlow,
            expenseFlow,
            cashInflowFlow,
            supplierPaymentFlow
        ) { sales, grossProfit, expenses, cashInflow, supplierPayments ->
            val cogs = (sales - grossProfit).coerceAtLeast(0L)
            val netProfit = grossProfit - expenses
            val cashOutflow = expenses + supplierPayments
            val netCash = cashInflow - cashOutflow

            BusinessPnLSummary(
                startDate = startOfDay,
                endDate = endOfDay,
                totalSalesPaise = sales,
                totalCogsPaise = cogs,
                grossProfitPaise = grossProfit,
                operatingExpensesPaise = expenses,
                netProfitPaise = netProfit,
                cashInflowPaise = cashInflow,
                cashOutflowPaise = cashOutflow,
                netCashFlowPaise = netCash
            )
        }
    }
}

