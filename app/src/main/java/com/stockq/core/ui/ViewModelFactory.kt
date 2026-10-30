package com.stockq.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stockq.core.di.AppContainer
import com.stockq.feature.backup.BackupRestoreViewModel
import com.stockq.feature.batches.BatchesViewModel
import com.stockq.feature.customers.CustomersViewModel
import com.stockq.feature.dashboard.DashboardViewModel
import com.stockq.feature.documents.DocumentsViewModel
import com.stockq.feature.employees.EmployeesViewModel
import com.stockq.feature.expenses.ExpensesViewModel
import com.stockq.feature.inventory.InventoryViewModel
import com.stockq.feature.productdetail.ProductDetailsViewModel
import com.stockq.feature.productedit.AddEditProductViewModel
import com.stockq.feature.purchases.PurchasesViewModel
import com.stockq.feature.quotations.QuotationsViewModel
import com.stockq.feature.reorder.ReorderViewModel
import com.stockq.feature.reports.ReportsViewModel
import com.stockq.feature.sales.SalesViewModel
import com.stockq.feature.servicejobs.ServiceJobsViewModel
import com.stockq.feature.settings.SettingsViewModel
import com.stockq.feature.stockmovement.StockMovementViewModel
import com.stockq.feature.suppliercomparison.SupplierComparisonViewModel
import com.stockq.feature.suppliers.SuppliersViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(
                    productRepository = container.productRepository,
                    stockLedgerRepository = container.stockLedgerRepository,
                    supplierRepository = container.supplierRepository,
                    businessProfileRepository = container.businessProfileRepository,
                    saleRepository = container.saleRepository,
                    customerRepository = container.customerRepository,
                    purchaseRepository = container.purchaseRepository,
                    expenseRepository = container.expenseRepository,
                    batchRepository = container.batchRepository,
                    serviceJobRepository = container.serviceJobRepository
                ) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(container.productRepository) as T
            }
            modelClass.isAssignableFrom(AddEditProductViewModel::class.java) -> {
                AddEditProductViewModel(container.productRepository) as T
            }
            modelClass.isAssignableFrom(ProductDetailsViewModel::class.java) -> {
                ProductDetailsViewModel(
                    productRepository = container.productRepository,
                    stockLedgerRepository = container.stockLedgerRepository,
                    supplierRepository = container.supplierRepository
                ) as T
            }
            modelClass.isAssignableFrom(StockMovementViewModel::class.java) -> {
                StockMovementViewModel(
                    productRepository = container.productRepository,
                    stockLedgerRepository = container.stockLedgerRepository
                ) as T
            }
            modelClass.isAssignableFrom(PurchasesViewModel::class.java) -> {
                PurchasesViewModel(
                    purchaseRepository = container.purchaseRepository,
                    supplierRepository = container.supplierRepository,
                    productRepository = container.productRepository
                ) as T
            }
            modelClass.isAssignableFrom(SuppliersViewModel::class.java) -> {
                SuppliersViewModel(container.supplierRepository) as T
            }
            modelClass.isAssignableFrom(SupplierComparisonViewModel::class.java) -> {
                SupplierComparisonViewModel(
                    productRepository = container.productRepository,
                    supplierRepository = container.supplierRepository
                ) as T
            }
            modelClass.isAssignableFrom(ReorderViewModel::class.java) -> {
                ReorderViewModel(container.productRepository) as T
            }
            modelClass.isAssignableFrom(SalesViewModel::class.java) -> {
                SalesViewModel(
                    saleRepository = container.saleRepository,
                    productRepository = container.productRepository,
                    customerRepository = container.customerRepository
                ) as T
            }
            modelClass.isAssignableFrom(CustomersViewModel::class.java) -> {
                CustomersViewModel(container.customerRepository) as T
            }
            modelClass.isAssignableFrom(QuotationsViewModel::class.java) -> {
                QuotationsViewModel(
                    quotationRepository = container.quotationRepository,
                    customerRepository = container.customerRepository,
                    productRepository = container.productRepository
                ) as T
            }
            modelClass.isAssignableFrom(ServiceJobsViewModel::class.java) -> {
                ServiceJobsViewModel(container.serviceJobRepository) as T
            }
            modelClass.isAssignableFrom(ExpensesViewModel::class.java) -> {
                ExpensesViewModel(container.expenseRepository) as T
            }
            modelClass.isAssignableFrom(BatchesViewModel::class.java) -> {
                BatchesViewModel(
                    batchRepository = container.batchRepository,
                    productRepository = container.productRepository
                ) as T
            }
            modelClass.isAssignableFrom(EmployeesViewModel::class.java) -> {
                EmployeesViewModel(container.employeeRepository) as T
            }
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> {
                ReportsViewModel(container.reportRepository) as T
            }
            modelClass.isAssignableFrom(DocumentsViewModel::class.java) -> {
                DocumentsViewModel(container.businessDocumentRepository) as T
            }
            modelClass.isAssignableFrom(BackupRestoreViewModel::class.java) -> {
                BackupRestoreViewModel(container.backupRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(container.businessProfileRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
