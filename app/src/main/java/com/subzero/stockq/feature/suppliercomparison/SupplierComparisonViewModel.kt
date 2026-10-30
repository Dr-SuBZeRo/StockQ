package com.subzero.stockq.feature.suppliercomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.dao.SupplierProductQuote
import com.subzero.stockq.core.database.entity.Product
import com.subzero.stockq.data.repository.ProductRepository
import com.subzero.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierComparisonViewModel(
    productRepository: ProductRepository,
    supplierRepository: SupplierRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProductId = MutableStateFlow<Long?>(null)
    val selectedProductId: StateFlow<Long?> = _selectedProductId.asStateFlow()

    fun selectProduct(productId: Long) {
        _selectedProductId.value = productId
    }

    val quotes: StateFlow<List<SupplierProductQuote>> = _selectedProductId.flatMapLatest { id ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            supplierRepository.getSupplierQuotesForProduct(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

