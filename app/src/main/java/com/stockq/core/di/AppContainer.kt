package com.stockq.core.di

import android.content.Context
import com.stockq.core.database.StockQDatabase
import com.stockq.data.repository.BackupRepository
import com.stockq.data.repository.BatchRepository
import com.stockq.data.repository.BusinessDocumentRepository
import com.stockq.data.repository.BusinessProfileRepository
import com.stockq.data.repository.CustomerRepository
import com.stockq.data.repository.EmployeeRepository
import com.stockq.data.repository.ExpenseRepository
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.PurchaseRepository
import com.stockq.data.repository.QuotationRepository
import com.stockq.data.repository.ReportRepository
import com.stockq.data.repository.SaleRepository
import com.stockq.data.repository.ServiceJobRepository
import com.stockq.data.repository.StockLedgerRepository
import com.stockq.data.repository.SupplierRepository

interface AppContainer {
    val database: StockQDatabase
    val stockLedgerRepository: StockLedgerRepository
    val productRepository: ProductRepository
    val purchaseRepository: PurchaseRepository
    val supplierRepository: SupplierRepository
    val businessProfileRepository: BusinessProfileRepository
    val batchRepository: BatchRepository
    val customerRepository: CustomerRepository
    val saleRepository: SaleRepository
    val quotationRepository: QuotationRepository
    val serviceJobRepository: ServiceJobRepository
    val expenseRepository: ExpenseRepository
    val employeeRepository: EmployeeRepository
    val businessDocumentRepository: BusinessDocumentRepository
    val reportRepository: ReportRepository
    val backupRepository: BackupRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: StockQDatabase by lazy {
        StockQDatabase.getInstance(context)
    }

    override val stockLedgerRepository: StockLedgerRepository by lazy {
        StockLedgerRepository(database)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(database, stockLedgerRepository)
    }

    override val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepository(database)
    }

    override val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(database)
    }

    override val businessProfileRepository: BusinessProfileRepository by lazy {
        BusinessProfileRepository(database)
    }

    override val batchRepository: BatchRepository by lazy {
        BatchRepository(database)
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepository(database)
    }

    override val saleRepository: SaleRepository by lazy {
        SaleRepository(database)
    }

    override val quotationRepository: QuotationRepository by lazy {
        QuotationRepository(database, saleRepository)
    }

    override val serviceJobRepository: ServiceJobRepository by lazy {
        ServiceJobRepository(database)
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database)
    }

    override val employeeRepository: EmployeeRepository by lazy {
        EmployeeRepository(database)
    }

    override val businessDocumentRepository: BusinessDocumentRepository by lazy {
        BusinessDocumentRepository(database)
    }

    override val reportRepository: ReportRepository by lazy {
        ReportRepository(database)
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepository(context, database)
    }
}
