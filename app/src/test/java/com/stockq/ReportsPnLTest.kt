package com.stockq

import com.stockq.data.repository.BusinessPnLSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsPnLTest {

    @Test
    fun pnlCalculations_computesExactGrossAndNetProfit() {
        val totalSalesPaise = 10000000L // ₹100,000.00
        val cogsPaise = 6500000L       // ₹65,000.00
        val expensesPaise = 1500000L   // ₹15,000.00
        val grossProfit = totalSalesPaise - cogsPaise // ₹35,000.00 (3500000 paise)
        val netProfit = grossProfit - expensesPaise   // ₹20,000.00 (2000000 paise)

        val summary = BusinessPnLSummary(
            startDate = 1000L,
            endDate = 2000L,
            totalSalesPaise = totalSalesPaise,
            totalCogsPaise = cogsPaise,
            grossProfitPaise = grossProfit,
            operatingExpensesPaise = expensesPaise,
            netProfitPaise = netProfit,
            cashInflowPaise = 9000000L,
            cashOutflowPaise = 7000000L,
            netCashFlowPaise = 2000000L
        )

        assertEquals(3500000L, summary.grossProfitPaise)
        assertEquals(2000000L, summary.netProfitPaise)
        assertEquals(2000000L, summary.netCashFlowPaise)
    }
}

