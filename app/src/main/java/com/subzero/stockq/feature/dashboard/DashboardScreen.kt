package com.subzero.stockq.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.subzero.stockq.R
import com.subzero.stockq.core.model.Money
import com.subzero.stockq.core.navigation.StockQDestination
import com.subzero.stockq.core.ui.components.StockQTopBar
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
            // Inventory Valuation Card (Real live valuation in paise)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Total Inventory Valuation",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Money.formatPaise(valuation),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$productCount active catalog SKUs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick Status Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusPill(
                    label = "Catalog",
                    value = "$productCount",
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
                    label = "Suppliers",
                    value = "$supplierCount",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SUPPLIERS) }
                )
            }

            // Quick Navigation Hub
            Text(
                text = "Operations Hub",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "Inventory",
                    subtitle = "Catalog & Stock",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.INVENTORY) }
                )
                DashboardActionCard(
                    title = "Purchases",
                    subtitle = "Stock Inward",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.PURCHASES) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "Stock Ledger",
                    subtitle = "Movement History",
                    icon = Icons.Default.SwapHoriz,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.STOCK_MOVEMENT) }
                )
                DashboardActionCard(
                    title = "Reorder List",
                    subtitle = "Restock Alerts",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.REORDER) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "Suppliers",
                    subtitle = "Vendor Directory",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SUPPLIERS) }
                )
                DashboardActionCard(
                    title = "Compare",
                    subtitle = "Supplier Quotes",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SUPPLIER_COMPARISON) }
                )
            }

            // Recent Ledger Activity
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Stock Movements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { onNavigate(StockQDestination.STOCK_MOVEMENT) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View all")
                        }
                    }

                    if (recentMovements.isEmpty()) {
                        Text(
                            text = "No stock movements recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        recentMovements.forEachIndexed { index, m ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = m.productName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${m.movementType.replace('_', ' ')} • ${dateFormat.format(Date(m.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                val sign = if (m.quantityChange > 0) "+" else ""
                                Text(
                                    text = "$sign${m.quantityChange} ${m.productUnit}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.quantityChange >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    value: String,
    icon: ImageVector,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
