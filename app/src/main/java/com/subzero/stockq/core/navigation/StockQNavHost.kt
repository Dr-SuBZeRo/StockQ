package com.subzero.stockq.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subzero.stockq.StockQApp
import com.subzero.stockq.core.ui.ViewModelFactory
import com.subzero.stockq.feature.backup.BackupRestoreScreen
import com.subzero.stockq.feature.dashboard.DashboardScreen
import com.subzero.stockq.feature.dashboard.DashboardViewModel
import com.subzero.stockq.feature.inventory.InventoryScreen
import com.subzero.stockq.feature.inventory.InventoryViewModel
import com.subzero.stockq.feature.productdetail.ProductDetailsScreen
import com.subzero.stockq.feature.productdetail.ProductDetailsViewModel
import com.subzero.stockq.feature.productedit.AddEditProductScreen
import com.subzero.stockq.feature.productedit.AddEditProductViewModel
import com.subzero.stockq.feature.purchases.PurchasesScreen
import com.subzero.stockq.feature.purchases.PurchasesViewModel
import com.subzero.stockq.feature.reorder.ReorderScreen
import com.subzero.stockq.feature.reorder.ReorderViewModel
import com.subzero.stockq.feature.settings.SettingsScreen
import com.subzero.stockq.feature.settings.SettingsViewModel
import com.subzero.stockq.feature.stockmovement.StockMovementScreen
import com.subzero.stockq.feature.stockmovement.StockMovementViewModel
import com.subzero.stockq.feature.suppliercomparison.SupplierComparisonScreen
import com.subzero.stockq.feature.suppliercomparison.SupplierComparisonViewModel
import com.subzero.stockq.feature.suppliers.SuppliersScreen
import com.subzero.stockq.feature.suppliers.SuppliersViewModel
import com.subzero.stockq.feature.welcome.WelcomeScreen

/**
 * Robust, type-safe navigation host for StockQ connecting all ViewModels and UI screens.
 */
@Composable
fun StockQNavHost(
    modifier: Modifier = Modifier,
    initialDestination: StockQDestination = StockQDestination.DASHBOARD
) {
    val context = LocalContext.current
    val app = context.applicationContext as StockQApp
    val factory = remember { ViewModelFactory(app.container) }

    val backStack = remember { mutableStateListOf(initialDestination) }
    val currentDestination = backStack.lastOrNull() ?: StockQDestination.DASHBOARD
    var selectedProductId by remember { mutableStateOf<Long?>(null) }

    fun navigateTo(destination: StockQDestination) {
        backStack.add(destination)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    when (currentDestination) {
        StockQDestination.WELCOME -> {
            WelcomeScreen(
                onGetStarted = {
                    backStack.clear()
                    backStack.add(StockQDestination.DASHBOARD)
                },
                modifier = modifier
            )
        }
        StockQDestination.DASHBOARD -> {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigate = { dest -> navigateTo(dest) },
                modifier = modifier
            )
        }
        StockQDestination.INVENTORY -> {
            val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
            InventoryScreen(
                viewModel = inventoryViewModel,
                onNavigateBack = { navigateBack() },
                onAddProduct = { navigateTo(StockQDestination.ADD_EDIT_PRODUCT) },
                onProductClick = { prodId ->
                    selectedProductId = prodId
                    navigateTo(StockQDestination.PRODUCT_DETAILS)
                },
                modifier = modifier
            )
        }
        StockQDestination.ADD_EDIT_PRODUCT -> {
            val addEditProductViewModel: AddEditProductViewModel = viewModel(factory = factory)
            AddEditProductScreen(
                viewModel = addEditProductViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.PRODUCT_DETAILS -> {
            val productDetailsViewModel: ProductDetailsViewModel = viewModel(factory = factory)
            selectedProductId?.let { id ->
                productDetailsViewModel.setProductId(id)
            }
            ProductDetailsScreen(
                viewModel = productDetailsViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.STOCK_MOVEMENT -> {
            val stockMovementViewModel: StockMovementViewModel = viewModel(factory = factory)
            StockMovementScreen(
                viewModel = stockMovementViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.PURCHASES -> {
            val purchasesViewModel: PurchasesViewModel = viewModel(factory = factory)
            PurchasesScreen(
                viewModel = purchasesViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.SUPPLIERS -> {
            val suppliersViewModel: SuppliersViewModel = viewModel(factory = factory)
            SuppliersScreen(
                viewModel = suppliersViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.SUPPLIER_COMPARISON -> {
            val supplierComparisonViewModel: SupplierComparisonViewModel = viewModel(factory = factory)
            SupplierComparisonScreen(
                viewModel = supplierComparisonViewModel,
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.REORDER -> {
            val reorderViewModel: ReorderViewModel = viewModel(factory = factory)
            ReorderScreen(
                viewModel = reorderViewModel,
                onNavigateBack = { navigateBack() },
                onNavigateToPurchases = { navigateTo(StockQDestination.PURCHASES) },
                modifier = modifier
            )
        }
        StockQDestination.SETTINGS -> {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navigateBack() },
                onNavigateToBackup = { navigateTo(StockQDestination.BACKUP_RESTORE) },
                modifier = modifier
            )
        }
        StockQDestination.BACKUP_RESTORE -> {
            BackupRestoreScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
    }
}
