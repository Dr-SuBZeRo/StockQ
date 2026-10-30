package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.SupplierProductQuote
import com.stockq.core.database.entity.Supplier
import kotlinx.coroutines.flow.Flow

class SupplierRepository(private val database: StockQDatabase) {

    private val supplierDao = database.supplierDao()
    private val itemDao = database.purchaseItemDao()

    fun getActiveSuppliers(): Flow<List<Supplier>> = supplierDao.getActiveSuppliers()

    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()

    fun getSupplierById(id: Long): Flow<Supplier?> = supplierDao.getSupplierById(id)

    suspend fun getSupplierDirect(id: Long): Supplier? = supplierDao.getSupplierDirect(id)

    fun getActiveSupplierCount(): Flow<Int> = supplierDao.getActiveSupplierCount()

    suspend fun createSupplier(
        name: String,
        contactPerson: String = "",
        phone: String = "",
        email: String = "",
        address: String = "",
        gstin: String = "",
        paymentTermsDays: Int = 30
    ): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Supplier name cannot be blank"))
        }

        val supplier = Supplier(
            name = name.trim(),
            contactPerson = contactPerson.trim(),
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim(),
            gstin = gstin.trim().uppercase(),
            paymentTermsDays = paymentTermsDays
        )

        val id = supplierDao.insertSupplier(supplier)
        return Result.success(id)
    }

    suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        if (supplier.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Supplier name cannot be blank"))
        }
        supplierDao.updateSupplier(supplier)
        return Result.success(Unit)
    }

    suspend fun setArchived(supplierId: Long, isArchived: Boolean) {
        supplierDao.setArchived(supplierId, isArchived)
    }

    /**
     * Retrieves all recorded supplier purchase prices and quotes for a given product.
     * Evaluates actual effective unit cost (base price - discount + tax + transport).
     */
    fun getSupplierQuotesForProduct(productId: Long): Flow<List<SupplierProductQuote>> =
        itemDao.getSupplierQuotesForProduct(productId)
}

