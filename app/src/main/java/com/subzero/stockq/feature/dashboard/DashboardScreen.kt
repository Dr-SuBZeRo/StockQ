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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subzero.stockq.R
import com.subzero.stockq.core.navigation.StockQDestination
import com.subzero.stockq.core.ui.components.CurrencyDisplay
import com.subzero.stockq.core.ui.components.StockQTopBar

@Composable
fun DashboardScreen(
    onNavigate: (StockQDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_dashboard),
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
            // Inventory Valuation Card
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
                    Spacer(modifier = Modifier.height(8.dp))
                    CurrencyDisplay(
                        amount = 0.00,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0 distinct stock items cataloged",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick Navigation Hub
            Text(
                text = "Inventory Management",
                style = MaterialTheme.typography.titleMedium,
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
                    title = "Movement",
                    subtitle = "Sales & Returns",
                    icon = Icons.Default.SwapHoriz,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.STOCK_MOVEMENT) }
                )
                DashboardActionCard(
                    title = "Reorder",
                    subtitle = "Low Stock List",
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
                    subtitle = "Directory",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SUPPLIERS) }
                )
                DashboardActionCard(
                    title = "Compare",
                    subtitle = "Price Matrix",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StockQDestination.SUPPLIER_COMPARISON) }
                )
            }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
