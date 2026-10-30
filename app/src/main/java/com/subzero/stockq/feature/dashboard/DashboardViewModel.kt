package com.subzero.stockq.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.dao.StockMovementWithProduct
import com.subzero.stockq.core.database.entity.BusinessProfile
import com.subzero.stockq.data.repository.BusinessProfileRepository
import com.subzero.stockq.data.repository.ProductRepository
import com.subzero.stockq.data.repository.StockLedgerRepository
import com.subzero.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val valuationPaise: Long = 0L,
    val productCount: Int = 0,
    val lowStockCount: Int = 0,
    val supplierCount: Int = 0,
    val recentMovements: List<StockMovementWithProduct> = emptyList(),
    val storeProfile: BusinessProfile? = null
)

class DashboardViewModel(
    productRepository: ProductRepository,
    stockLedgerRepository: StockLedgerRepository,
    supplierRepository: SupplierRepository,
    businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val valuation: StateFlow<Long> = productRepository.getTotalInventoryValuationPaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val productCount: StateFlow<Int> = productRepository.getActiveProductCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount: StateFlow<Int> = productRepository.getLowStockCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val supplierCount: StateFlow<Int> = supplierRepository.getActiveSupplierCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentMovements: StateFlow<List<StockMovementWithProduct>> = stockLedgerRepository.getRecentMovementsWithProduct(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<BusinessProfile?> = businessProfileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

