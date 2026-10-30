package com.subzero.stockq.feature.stockmovement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subzero.stockq.R
import com.subzero.stockq.core.database.entity.StockMovementType
import com.subzero.stockq.core.ui.components.StockQTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockMovementScreen(
    viewModel: StockMovementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val recentMovements by viewModel.recentMovements.collectAsStateWithLifecycle()

    var productDropdownExpanded by remember { mutableStateOf(false) }
    val selectedProduct = products.find { it.id == formState.selectedProductId }

    val movementTypes = listOf(
        StockMovementType.SALE_ISSUE to "Sale Issue",
        StockMovementType.PHYSICAL_ADJUSTMENT to "Physical Count",
        StockMovementType.DAMAGED_WASTAGE to "Damaged / Waste",
        StockMovementType.CUSTOMER_RETURN to "Customer Return"
    )

    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_stock_movement),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Movement Entry Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Record Stock Movement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Product Selection Picker
                        Column {
                            OutlinedCard(
                                onClick = { productDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedProduct?.let { "${it.name} (Cur: ${it.currentStock} ${it.unit})" }
                                            ?: "Select Product SKU *",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                if (products.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No active products available") },
                                        onClick = { productDropdownExpanded = false }
                                    )
                                } else {
                                    products.forEach { prod ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(prod.name, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        "Stock: ${prod.currentStock} ${prod.unit} • SKU: ${prod.sku.ifBlank { "N/A" }}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.onProductSelect(prod.id)
                                                productDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Movement Type Selector
                        Text(
                            text = "Movement Type",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            movementTypes.take(2).forEach { (type, label) ->
                                FilterChip(
                                    selected = formState.movementType == type,
                                    onClick = { viewModel.onMovementTypeSelect(type) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            movementTypes.drop(2).forEach { (type, label) ->
                                FilterChip(
                                    selected = formState.movementType == type,
                                    onClick = { viewModel.onMovementTypeSelect(type) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        // Quantity Input
                        val qtyLabel = if (formState.movementType == StockMovementType.PHYSICAL_ADJUSTMENT) {
                            "Actual Counted Stock (${selectedProduct?.unit ?: "units"}) *"
                        } else {
                            "Quantity (${selectedProduct?.unit ?: "units"}) *"
                        }

                        OutlinedTextField(
                            value = formState.quantityString,
                            onValueChange = { viewModel.onQuantityChange(it) },
                            label = { Text(qtyLabel) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Reason / Notes Input
                        OutlinedTextField(
                            value = formState.reasonOrNotes,
                            onValueChange = { viewModel.onReasonChange(it) },
                            label = { Text("Reason / Remarks / Ref #") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error Banner
                        formState.errorMessage?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Success Banner
                        formState.successMessage?.let { success ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = success,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Action Button
                        Button(
                            onClick = { viewModel.submitMovement() },
                            enabled = !formState.isSubmitting && selectedProduct != null && formState.quantityString.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(if (formState.isSubmitting) "Recording..." else "Commit Movement to Ledger")
                        }
                    }
                }
            }

            // Ledger Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Stock Movements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${recentMovements.size} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Movement Items
            if (recentMovements.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No stock movements recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                items(recentMovements, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${item.movementType.replace('_', ' ')} • ${dateFormat.format(Date(item.timestamp))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (item.reasonOrNotes.isNotBlank()) {
                                    Text(
                                        text = item.reasonOrNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val sign = if (item.quantityChange > 0) "+" else ""
                                Text(
                                    text = "$sign${item.quantityChange} ${item.productUnit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.quantityChange >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Bal: ${item.balanceAfter}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
