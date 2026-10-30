package com.subzero.stockq.feature.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.entity.Product
import com.subzero.stockq.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ReorderItem(
    val product: Product,
    val deficitQuantity: Double,
    val suggestedOrderQuantity: Double,
    val estimatedCostPaise: Long
)

class ReorderViewModel(
    productRepository: ProductRepository
) : ViewModel() {

    val reorderItems: StateFlow<List<ReorderItem>> = productRepository.getLowStockProducts()
        .map { products ->
            products.map { p ->
                val deficit = maxOf(0.0, p.minStockThreshold - p.currentStock)
                // Suggested target is bringing inventory to double the minimum threshold
                val targetStock = maxOf(p.minStockThreshold * 2.0, 1.0)
                val suggestedOrder = maxOf(deficit, targetStock - p.currentStock)
                val estimatedCost = (suggestedOrder * p.costPricePaise).toLong()
                ReorderItem(
                    product = p,
                    deficitQuantity = deficit,
                    suggestedOrderQuantity = suggestedOrder,
                    estimatedCostPaise = estimatedCost
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalEstimatedCostPaise: StateFlow<Long> = reorderItems.map { items ->
        items.sumOf { it.estimatedCostPaise }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}

