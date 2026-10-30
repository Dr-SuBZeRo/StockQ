package com.subzero.stockq.feature.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subzero.stockq.R
import com.subzero.stockq.core.database.dao.PurchaseWithSupplier
import com.subzero.stockq.core.database.entity.Product
import com.subzero.stockq.core.database.entity.PurchaseStatus
import com.subzero.stockq.core.database.entity.Supplier
import com.subzero.stockq.core.model.Money
import com.subzero.stockq.core.ui.components.StockQTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesScreen(
    viewModel: PurchasesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var cancelTargetPurchaseId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_purchases),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewPurchaseDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Purchase / Inward")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (purchases.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.empty_purchases),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.openNewPurchaseDialog() }) {
                    Text("Create Inward Purchase")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(purchases, key = { it.purchase.id }) { item ->
                    PurchaseCard(
                        purchaseWithSupplier = item,
                        onConfirm = { viewModel.confirmPurchase(item.purchase.id) },
                        onCancel = { cancelTargetPurchaseId = item.purchase.id }
                    )
                }
            }
        }

        // New Purchase Dialog
        if (dialogState.isOpen) {
            NewPurchaseDialog(
                dialogState = dialogState,
                suppliers = suppliers,
                products = products,
                onDismiss = { viewModel.closeDialog() },
                onSupplierSelected = { viewModel.onSupplierSelected(it) },
                onInvoiceNumberChange = { viewModel.onInvoiceNumberChange(it) },
                onFreightChange = { viewModel.onFreightChange(it) },
                onNotesChange = { viewModel.onNotesChange(it) },
                onAddItem = { pId, q, c, t, d -> viewModel.addItemToDraft(pId, q, c, t, d) },
                onRemoveItem = { idx -> viewModel.removeItemFromDraft(idx) },
                onSaveDraft = { viewModel.savePurchase(autoConfirm = false) },
                onConfirmDirectly = { viewModel.savePurchase(autoConfirm = true) }
            )
        }

        // Cancellation Confirmation Dialog
        cancelTargetPurchaseId?.let { targetId ->
            AlertDialog(
                onDismissRequest = { cancelTargetPurchaseId = null },
                title = { Text("Cancel Purchase #$targetId?") },
                text = {
                    Text("Cancelling this purchase will reverse all received stock from inventory and generate audit reversal movement entries.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelPurchase(targetId, "User cancelled from purchases screen")
                            cancelTargetPurchaseId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm Cancellation")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { cancelTargetPurchaseId = null }) {
                        Text("Back")
                    }
                }
            )
        }
    }
}

@Composable
fun PurchaseCard(
    purchaseWithSupplier: PurchaseWithSupplier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = purchaseWithSupplier.purchase
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val statusColor = when (p.status) {
        PurchaseStatus.CONFIRMED -> MaterialTheme.colorScheme.secondary
        PurchaseStatus.DRAFT -> MaterialTheme.colorScheme.tertiary
        PurchaseStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = purchaseWithSupplier.supplierName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invoice: ${p.invoiceNumber.ifBlank { "N/A" }} • ${dateFormat.format(Date(p.purchaseDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = p.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Net Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = Money.formatPaise(p.totalAmountPaise),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (p.transportChargesPaise > 0L) {
                        Text(
                            text = "Incl. ${Money.formatPaise(p.transportChargesPaise)} freight",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons based on status
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (p.status == PurchaseStatus.DRAFT) {
                        OutlinedButton(onClick = onCancel) {
                            Text("Discard")
                        }
                        Button(onClick = onConfirm) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inward")
                        }
                    } else if (p.status == PurchaseStatus.CONFIRMED) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reverse")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewPurchaseDialog(
    dialogState: PurchaseDialogState,
    suppliers: List<Supplier>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSupplierSelected: (Long) -> Unit,
    onInvoiceNumberChange: (String) -> Unit,
    onFreightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAddItem: (productId: Long, qty: Double, cost: Double, tax: Double, disc: Double) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onSaveDraft: () -> Unit,
    onConfirmDirectly: () -> Unit
) {
    var supplierMenuOpen by remember { mutableStateOf(false) }
    var productMenuOpen by remember { mutableStateOf(false) }

    var selectedProdId by remember { mutableStateOf<Long?>(products.firstOrNull()?.id) }
    var itemQty by remember { mutableStateOf("1") }
    var itemCost by remember { mutableStateOf("") }
    var itemTax by remember { mutableStateOf("0") }
    var itemDiscount by remember { mutableStateOf("0") }

    val selectedSupplier = suppliers.find { it.id == dialogState.selectedSupplierId }
    val chosenProduct = products.find { it.id == selectedProdId }

    // Pre-populate product cost when product changes
    LaunchedEffect(selectedProdId) {
        chosenProduct?.let {
            if (itemCost.isBlank()) {
                itemCost = Money.paiseToRupeesString(it.costPricePaise)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Purchase / Inward",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Supplier Picker
                Text("Select Supplier *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedCard(
                    onClick = { supplierMenuOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSupplier?.name ?: "Tap to choose Supplier",
                            color = if (selectedSupplier != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = supplierMenuOpen, onDismissRequest = { supplierMenuOpen = false }) {
                    suppliers.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = {
                                onSupplierSelected(s.id)
                                supplierMenuOpen = false
                            }
                        )
                    }
                }

                // Invoice Number and Freight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.invoiceNumber,
                        onValueChange = onInvoiceNumberChange,
                        label = { Text("Invoice #") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dialogState.freightString,
                        onValueChange = onFreightChange,
                        label = { Text("Freight (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Item Inwarding Sub-form
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Item to Inward",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        // Product selector
                        OutlinedCard(
                            onClick = { productMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chosenProduct?.name ?: "Select Product SKU",
                                    color = if (chosenProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(expanded = productMenuOpen, onDismissRequest = { productMenuOpen = false }) {
                            products.forEach { pr ->
                                DropdownMenuItem(
                                    text = { Text("${pr.name} (Cur: ${pr.currentStock} ${pr.unit})") },
                                    onClick = {
                                        selectedProdId = pr.id
                                        itemCost = Money.paiseToRupeesString(pr.costPricePaise)
                                        productMenuOpen = false
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = itemQty,
                                onValueChange = { itemQty = it },
                                label = { Text("Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemCost,
                                onValueChange = { itemCost = it },
                                label = { Text("Unit ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemTax,
                                onValueChange = { itemTax = it },
                                label = { Text("Tax %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                        }

                        Button(
                            onClick = {
                                val pId = selectedProdId ?: return@Button
                                val q = itemQty.toDoubleOrNull() ?: 0.0
                                val c = itemCost.toDoubleOrNull() ?: 0.0
                                val t = itemTax.toDoubleOrNull() ?: 0.0
                                val d = itemDiscount.toDoubleOrNull() ?: 0.0
                                if (q > 0 && c >= 0) {
                                    onAddItem(pId, q, c, t, d)
                                    itemQty = "1"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Line Item")
                        }
                    }
                }

                // Line items list
                if (dialogState.items.isNotEmpty()) {
                    Text(
                        text = "Inward Items (${dialogState.items.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    dialogState.items.forEachIndexed { idx, itm ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(itm.productName, fontWeight = FontWeight.Medium)
                                Text(
                                    "${itm.quantity} × ${Money.formatPaise(itm.unitCostPaise)} (Tax ${itm.taxPercent}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onRemoveItem(idx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = dialogState.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes / Remarks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error
                dialogState.errorMessage?.let { err ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSaveDraft,
                        enabled = !dialogState.isSaving && dialogState.items.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Draft")
                    }
                    Button(
                        onClick = onConfirmDirectly,
                        enabled = !dialogState.isSaving && dialogState.items.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Inward")
                    }
                }
            }
        }
    }
}
