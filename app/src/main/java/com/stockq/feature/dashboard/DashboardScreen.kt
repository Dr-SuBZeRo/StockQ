package com.stockq.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.model.Money
import com.stockq.core.model.Quantity
import com.stockq.core.navigation.StockQDestination
import com.stockq.core.ui.components.StockQTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (StockQDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val valuation by viewModel.valuation.collectAsStateWithLifecycle()
    val productCount by viewModel.productCount.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val supplierCount by viewModel.supplierCount.collectAsStateWithLifecycle()
    val recentMovements by viewModel.recentMovements.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val todaySales by viewModel.todaySales.collectAsStateWithLifecycle()
    val todayGrossProfit by viewModel.todayGrossProfit.collectAsStateWithLifecycle()
    val todayExpenses by viewModel.todayExpenses.collectAsStateWithLifecycle()
    val customerDues by viewModel.customerDues.collectAsStateWithLifecycle()
    val supplierDues by viewModel.supplierDues.collectAsStateWithLifecycle()
    val expiringBatchesCount by viewModel.expiringBatchesCount.collectAsStateWithLifecycle()
    val activeJobsCount by viewModel.activeJobsCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            StockQTopBar(
                title = profile?.storeName ?: stringResource(R.string.title_dashboard),
                actions = {
                    IconButton(onClick = { onNavigate(StockQDestination.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.title_settings),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Sales & P&L Hero Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(StockQDestination.REPORTS) }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Business Overview",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Reports",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sales Revenue", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = Money.formatRupees(todaySales),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column {
                            Text("Gross Profit", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = Money.formatRupees(todayGrossProfit),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("Expenses", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = Money.formatRupees(todayExpenses),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Dues & Working Capital Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(StockQDestination.CUSTOMERS) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Customer Dues", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = Money.formatRupees(customerDues),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (customerDues > 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(StockQDestination.PURCHASES) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Supplier Dues", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = Money.formatRupees(supplierDues),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Quick Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    label = "Stock Value",
                    value = Money.formatRupees(valuation),
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.INVENTORY) }
                )
                StatusPill(
                    label = "Low Stock",
                    value = "$lowStockCount",
                    icon = Icons.Default.Warning,
                    isAlert = lowStockCount > 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.REORDER) }
                )
                StatusPill(
                    label = "Expiring",
                    value = "$expiringBatchesCount",
                    icon = Icons.Default.Warning,
                    isAlert = expiringBatchesCount > 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.BATCHES) }
                )
            }

            // Operations Grid Header
            Text(
                text = "Operations Hub",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Primary Hub Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Sales & POS",
                    subtitle = "Billing & Invoices",
                    icon = Icons.Default.PointOfSale,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SALES) }
                )
                DashboardActionCard(
                    title = "Inventory",
                    subtitle = "$productCount Items",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.INVENTORY) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Purchases",
                    subtitle = "Stock Inward",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.PURCHASES) }
                )
                DashboardActionCard(
                    title = "Customers",
                    subtitle = "Dues Ledger",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.CUSTOMERS) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Quotations",
                    subtitle = "Formal Estimates",
                    icon = Icons.Default.PriceCheck,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.QUOTATIONS) }
                )
                DashboardActionCard(
                    title = "Service & Repairs",
                    subtitle = "$activeJobsCount Active",
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SERVICE_JOBS) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Daily Expenses",
                    subtitle = "Operational Costs",
                    icon = Icons.Default.Money,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.EXPENSES) }
                )
                DashboardActionCard(
                    title = "Profit & Reports",
                    subtitle = "P&L Summary",
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.REPORTS) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Staff & Wages",
                    subtitle = "Attendance Register",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.EMPLOYEES) }
                )
                DashboardActionCard(
                    title = "Batch & Expiry",
                    subtitle = "Lot Tracking",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.BATCHES) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardActionCard(
                    title = "Compliance",
                    subtitle = "Licenses & Permits",
                    icon = Icons.Default.VerifiedUser,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.DOCUMENTS) }
                )
                DashboardActionCard(
                    title = "Stock Movements",
                    subtitle = "Auditable Ledger",
                    icon = Icons.Default.SwapHoriz,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.STOCK_MOVEMENT) }
                )
            }

            // Recent Movements Section
            if (recentMovements.isNotEmpty()) {
                Text(
                    text = "Recent Stock Movements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        recentMovements.forEachIndexed { index, movement ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movement.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${movement.movementType} • ${SimpleDateFormat("HH:mm, dd MMM", Locale.US).format(Date(movement.timestamp))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${if (movement.quantityChangeMilli >= 0) "+" else ""}${Quantity.toDisplay(movement.quantityChangeMilli)} ${movement.productUnit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (movement.quantityChangeMilli >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            if (index < recentMovements.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
