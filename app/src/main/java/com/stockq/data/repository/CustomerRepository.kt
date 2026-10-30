package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.Customer
import com.stockq.core.database.entity.CustomerPayment
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val database: StockQDatabase) {

    private val customerDao = database.customerDao()
    private val paymentDao = database.customerPaymentDao()

    fun getActiveCustomers(): Flow<List<Customer>> = customerDao.getActiveCustomers()

    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomersWithDues(): Flow<List<Customer>> = customerDao.getCustomersWithDues()

    fun getTotalCustomerDuesPaise(): Flow<Long> = customerDao.getTotalCustomerDuesPaise()

    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)

    suspend fun getCustomerDirect(id: Long): Customer? = customerDao.getCustomerDirect(id)

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPayment>> =
        paymentDao.getPaymentsForCustomer(customerId)

    suspend fun createCustomer(
        name: String,
        phone: String = "",
        email: String = "",
        address: String = "",
        gstin: String = "",
        creditLimitPaise: Long = 0L
    ): Result<Long> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }

        val customer = Customer(
            name = trimmedName,
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim(),
            gstin = gstin.trim(),
            creditLimitPaise = creditLimitPaise
        )

        val id = customerDao.insertCustomer(customer)
        return Result.success(id)
    }

    suspend fun updateCustomer(customer: Customer): Result<Unit> {
        if (customer.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }
        customerDao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    suspend fun setArchived(customerId: Long, isArchived: Boolean) {
        customerDao.setArchived(customerId, isArchived)
    }

    /**
     * Records customer payment / collection towards outstanding dues.
     */
    suspend fun recordCustomerPayment(
        customerId: Long,
        amountPaise: Long,
        paymentMode: String = "CASH",
        referenceNumber: String = "",
        notes: String = "",
        saleId: Long? = null
    ): Result<Long> = database.withTransaction {
        if (amountPaise <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Payment amount must be greater than zero"))
        }

        val customer = customerDao.getCustomerDirect(customerId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Customer #$customerId not found"))

        val payment = CustomerPayment(
            customerId = customerId,
            saleId = saleId,
            amountPaise = amountPaise,
            paymentMode = paymentMode,
            referenceNumber = referenceNumber.trim(),
            notes = notes.trim(),
            paymentDate = System.currentTimeMillis()
        )

        val paymentId = paymentDao.insertPayment(payment)
        val newDue = (customer.outstandingDuePaise - amountPaise).coerceAtLeast(0L)
        customerDao.updateDue(customerId, newDue)

        Result.success(paymentId)
    }
}

