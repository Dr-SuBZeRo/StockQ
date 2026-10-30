package com.stockq.feature.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.BatchWithProduct
import com.stockq.core.database.entity.Product
import com.stockq.core.model.Quantity
import com.stockq.data.repository.BatchRepository
import com.stockq.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatchesViewModel(
    private val batchRepository: BatchRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val expiringBatches: StateFlow<List<BatchWithProduct>> =
        batchRepository.getBatchesExpiringWithinDays(60)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiredCount: StateFlow<Int> = batchRepository.getExpiredBatchCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringSoonCount: StateFlow<Int> = batchRepository.getExpiringSoonBatchCount(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun createBatch(
        productId: Long,
        batchNumber: String,
        expiryDate: Long,
        quantity: Double,
        purchasePriceRupees: String,
        onSuccess: () -> Unit
    ) {
        val qtyMilli = Quantity.fromDouble(quantity)
        val purchasePaise = (purchasePriceRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L

        viewModelScope.launch {
            batchRepository.createBatch(
                productId = productId,
                batchNumber = batchNumber,
                expiryDate = expiryDate,
                quantityMilli = qtyMilli,
                purchasePricePaise = purchasePaise
            )
            _message.value = "Batch registered"
            onSuccess()
        }
    }
}

