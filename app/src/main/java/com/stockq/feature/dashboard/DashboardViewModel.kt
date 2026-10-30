package com.stockq.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.StockMovementWithProduct
import com.stockq.core.database.entity.BusinessProfile
import com.stockq.data.repository.BatchRepository
import com.stockq.data.repository.BusinessProfileRepository
import com.stockq.data.repository.CustomerRepository
import com.stockq.data.repository.ExpenseRepository
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.PurchaseRepository
import com.stockq.data.repository.SaleRepository
import com.stockq.data.repository.ServiceJobRepository
import com.stockq.data.repository.StockLedgerRepository
import com.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class DashboardViewModel(
    productRepository: ProductRepository,
    stockLedgerRepository: StockLedgerRepository,
    supplierRepository: SupplierRepository,
    businessProfileRepository: BusinessProfileRepository,
    saleRepository: SaleRepository,
    customerRepository: CustomerRepository,
    purchaseRepository: PurchaseRepository,
    expenseRepository: ExpenseRepository,
    batchRepository: BatchRepository,
    serviceJobRepository: ServiceJobRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val valuation: StateFlow<Long> = productRepository.getTotalInventoryValuationPaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val productCount: StateFlow<Int> = productRepository.getActiveProductCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount: StateFlow<Int> = productRepository.getLowStockCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val supplierCount: StateFlow<Int> = supplierRepository.getActiveSupplierCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentMovements: StateFlow<List<StockMovementWithProduct>> =
        stockLedgerRepository.getRecentMovementsWithProduct(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<BusinessProfile?> = businessProfileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySales: StateFlow<Long> = saleRepository.getTodaySalesTotalPaise(startOfDay, endOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayGrossProfit: StateFlow<Long> = saleRepository.getTodayGrossProfitPaise(startOfDay, endOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayExpenses: StateFlow<Long> = expenseRepository.getTodayExpensesSum(startOfDay, endOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val customerDues: StateFlow<Long> = customerRepository.getTotalCustomerDuesPaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val supplierDues: StateFlow<Long> = purchaseRepository.getPendingSupplierDuesPaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val expiringBatchesCount: StateFlow<Int> = batchRepository.getExpiringSoonBatchCount(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeJobsCount: StateFlow<Int> = serviceJobRepository.getActiveJobsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
