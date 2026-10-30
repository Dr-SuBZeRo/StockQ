package com.stockq.feature.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.SupplierProductQuote
import com.stockq.core.database.entity.Product
import com.stockq.core.database.entity.StockMovement
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.StockLedgerRepository
import com.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsViewModel(
    private val productRepository: ProductRepository,
    private val stockLedgerRepository: StockLedgerRepository,
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _productId = MutableStateFlow<Long?>(null)
    val productId: StateFlow<Long?> = _productId.asStateFlow()

    fun setProductId(id: Long) {
        _productId.value = id
    }

    val product: StateFlow<Product?> = _productId.filterNotNull().flatMapLatest { id ->
        productRepository.getProductById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val movements: StateFlow<List<StockMovement>> = _productId.filterNotNull().flatMapLatest { id ->
        stockLedgerRepository.getMovementsForProduct(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierQuotes: StateFlow<List<SupplierProductQuote>> = _productId.filterNotNull().flatMapLatest { id ->
        supplierRepository.getSupplierQuotesForProduct(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

