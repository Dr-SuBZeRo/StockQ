package com.stockq.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.data.repository.BusinessPnLSummary
import com.stockq.data.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class ReportsViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val dailySummary: StateFlow<BusinessPnLSummary> =
        reportRepository.getDailySummary(startOfDay, endOfDay)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                BusinessPnLSummary(
                    startDate = startOfDay,
                    endDate = endOfDay,
                    totalSalesPaise = 0L,
                    totalCogsPaise = 0L,
                    grossProfitPaise = 0L,
                    operatingExpensesPaise = 0L,
                    netProfitPaise = 0L,
                    cashInflowPaise = 0L,
                    cashOutflowPaise = 0L,
                    netCashFlowPaise = 0L
                )
            )
}

