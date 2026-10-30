package com.stockq.feature.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.entity.Customer
import com.stockq.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = customerRepository.getActiveCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDuesPaise: StateFlow<Long> = customerRepository.getTotalCustomerDuesPaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun addCustomer(
        name: String,
        phone: String,
        email: String,
        address: String,
        creditLimitRupees: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val creditLimitPaise = (creditLimitRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
            val result = customerRepository.createCustomer(
                name = name,
                phone = phone,
                email = email,
                address = address,
                creditLimitPaise = creditLimitPaise
            )
            result.onSuccess {
                _message.value = "Customer added successfully"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to add customer"
            }
        }
    }

    fun recordPayment(
        customerId: Long,
        amountRupees: String,
        paymentMode: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val amountPaise = (amountRupees.toDoubleOrNull()?.times(100))?.toLong()
        if (amountPaise == null || amountPaise <= 0L) {
            _message.value = "Enter a valid payment amount"
            return
        }

        viewModelScope.launch {
            val result = customerRepository.recordCustomerPayment(
                customerId = customerId,
                amountPaise = amountPaise,
                paymentMode = paymentMode,
                notes = notes
            )
            result.onSuccess {
                _message.value = "Payment recorded successfully"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to record payment"
            }
        }
    }
}

