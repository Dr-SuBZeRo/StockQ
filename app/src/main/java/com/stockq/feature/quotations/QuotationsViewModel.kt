package com.stockq.feature.quotations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.QuotationWithItems
import com.stockq.core.database.entity.Customer
import com.stockq.core.database.entity.Product
import com.stockq.data.repository.CustomerRepository
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.QuotationItemDraftInput
import com.stockq.data.repository.QuotationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuotationsViewModel(
    private val quotationRepository: QuotationRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val quotations: StateFlow<List<QuotationWithItems>> = quotationRepository.getAllQuotations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerRepository.getActiveCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun createQuotation(
        customerId: Long?,
        customerName: String,
        customerPhone: String,
        items: List<QuotationItemDraftInput>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = quotationRepository.createQuotation(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                items = items
            )
            result.onSuccess {
                _message.value = "Quotation generated successfully"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to generate quotation"
            }
        }
    }

    fun convertToSale(quotationId: Long) {
        viewModelScope.launch {
            val result = quotationRepository.convertToSale(quotationId)
            result.onSuccess {
                _message.value = "Quotation successfully converted to Sale!"
            }.onFailure {
                _message.value = it.message ?: "Failed to convert quotation"
            }
        }
    }
}

