package com.stockq.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stockq.core.di.AppContainer
import com.stockq.feature.dashboard.DashboardViewModel
import com.stockq.feature.inventory.InventoryViewModel
import com.stockq.feature.productdetail.ProductDetailsViewModel
import com.stockq.feature.productedit.AddEditProductViewModel
import com.stockq.feature.purchases.PurchasesViewModel
import com.stockq.feature.reorder.ReorderViewModel
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
                    businessProfileRepository = container.businessProfileRepository
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
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(container.businessProfileRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

