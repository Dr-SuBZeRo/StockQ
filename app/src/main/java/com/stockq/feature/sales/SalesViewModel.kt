package com.stockq.feature.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.SaleWithCustomer
import com.stockq.core.database.entity.Customer
import com.stockq.core.database.entity.Product
import com.stockq.data.repository.CustomerRepository
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.SaleItemInput
import com.stockq.data.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PosCartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val unitPricePaise: Long = product.sellingPricePaise,
    val discountPercent: Double = 0.0
)

class SalesViewModel(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val sales: StateFlow<List<SaleWithCustomer>> = saleRepository.getAllSalesWithCustomer()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerRepository.getActiveCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cartItems = MutableStateFlow<List<PosCartItem>>(emptyList())
    val cartItems: StateFlow<List<PosCartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _paymentMode = MutableStateFlow("CASH")
    val paymentMode: StateFlow<String> = _paymentMode.asStateFlow()

    private val _paidAmountRupees = MutableStateFlow("")
    val paidAmountRupees: StateFlow<String> = _paidAmountRupees.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cartItems.value
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            val updated = current.toMutableList()
            updated[existingIndex] = item.copy(quantity = item.quantity + 1.0)
            _cartItems.value = updated
        } else {
            _cartItems.value = current + PosCartItem(product = product)
        }
    }

    fun updateCartItemQuantity(productId: Long, quantity: Double) {
        if (quantity <= 0.0) {
            _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    fun updateCartItemDiscount(productId: Long, discountPercent: Double) {
        _cartItems.value = _cartItems.value.map {
            if (it.product.id == productId) it.copy(discountPercent = discountPercent.coerceIn(0.0, 100.0)) else it
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setPaymentMode(mode: String) {
        _paymentMode.value = mode
    }

    fun setPaidAmount(rupeesText: String) {
        _paidAmountRupees.value = rupeesText
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _paidAmountRupees.value = ""
    }

    fun clearMessage() {
        _message.value = null
    }

    fun checkoutSale(onSuccess: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _message.value = "Cart is empty"
            return
        }

        viewModelScope.launch {
            val saleInputs = items.map {
                SaleItemInput(
                    productId = it.product.id,
                    quantity = it.quantity,
                    unitPricePaise = it.unitPricePaise,
                    discountPercent = it.discountPercent
                )
            }

            val paidPaise = _paidAmountRupees.value.toDoubleOrNull()?.let { (it * 100).toLong() }

            val result = saleRepository.createSale(
                customerId = _selectedCustomer.value?.id,
                customerName = _selectedCustomer.value?.name ?: "Walk-in Customer",
                items = saleInputs,
                paymentMode = _paymentMode.value,
                paidAmountPaise = paidPaise
            )

            result.onSuccess { saleId ->
                clearCart()
                _message.value = "Sale recorded successfully!"
                onSuccess(saleId)
            }.onFailure { err ->
                _message.value = err.message ?: "Failed to process sale"
            }
        }
    }

    fun cancelSale(saleId: Long, reason: String) {
        viewModelScope.launch {
            val result = saleRepository.cancelSale(saleId, reason)
            result.onSuccess {
                _message.value = "Sale #$saleId cancelled and inventory restored."
            }.onFailure { err ->
                _message.value = err.message ?: "Failed to cancel sale"
            }
        }
    }
}

