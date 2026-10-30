package com.subzero.stockq.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.subzero.stockq.feature.backup.BackupRestoreScreen
import com.subzero.stockq.feature.dashboard.DashboardScreen
import com.subzero.stockq.feature.inventory.InventoryScreen
import com.subzero.stockq.feature.productdetail.ProductDetailsScreen
import com.subzero.stockq.feature.productedit.AddEditProductScreen
import com.subzero.stockq.feature.purchases.PurchasesScreen
import com.subzero.stockq.feature.reorder.ReorderScreen
import com.subzero.stockq.feature.settings.SettingsScreen
import com.subzero.stockq.feature.stockmovement.StockMovementScreen
import com.subzero.stockq.feature.suppliercomparison.SupplierComparisonScreen
import com.subzero.stockq.feature.suppliers.SuppliersScreen
import com.subzero.stockq.feature.welcome.WelcomeScreen

/**
 * Lightweight, robust, type-safe navigation host for StockQ.
 */
@Composable
fun StockQNavHost(
    modifier: Modifier = Modifier,
    initialDestination: StockQDestination = StockQDestination.DASHBOARD
) {
    val backStack = remember { mutableStateListOf(initialDestination) }
    val currentDestination = backStack.lastOrNull() ?: StockQDestination.DASHBOARD

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
            DashboardScreen(
                onNavigate = { dest -> navigateTo(dest) },
                modifier = modifier
            )
        }
        StockQDestination.INVENTORY -> {
            InventoryScreen(
                onNavigateBack = { navigateBack() },
                onAddProduct = { navigateTo(StockQDestination.ADD_EDIT_PRODUCT) },
                modifier = modifier
            )
        }
        StockQDestination.ADD_EDIT_PRODUCT -> {
            AddEditProductScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.PRODUCT_DETAILS -> {
            ProductDetailsScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.STOCK_MOVEMENT -> {
            StockMovementScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.PURCHASES -> {
            PurchasesScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.SUPPLIERS -> {
            SuppliersScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.SUPPLIER_COMPARISON -> {
            SupplierComparisonScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.REORDER -> {
            ReorderScreen(
                onNavigateBack = { navigateBack() },
                modifier = modifier
            )
        }
        StockQDestination.SETTINGS -> {
            SettingsScreen(
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
