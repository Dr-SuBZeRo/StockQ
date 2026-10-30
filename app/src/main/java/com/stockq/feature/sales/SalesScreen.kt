package com.stockq.feature.sales

import android.content.Intent
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.database.dao.SaleWithCustomer
import com.stockq.core.database.entity.PaymentStatus
import com.stockq.core.model.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val paymentMode by viewModel.paymentMode.collectAsStateWithLifecycle()
    val paidAmountRupees by viewModel.paidAmountRupees.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPosDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_sales)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPosDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Sale")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (sales.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.empty_sales),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showPosDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start POS Billing")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales, key = { it.id }) { sale ->
                    SaleCard(
                        sale = sale,
                        onShareInvoice = {
                            val shareText = "StockQ Invoice: ${sale.invoiceNumber}\n" +
                                    "Customer: ${sale.customerName}\n" +
                                    "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(sale.saleDate))}\n" +
                                    "Total: ${Money.formatRupees(sale.totalAmountPaise)}\n" +
                                    "Paid: ${Money.formatRupees(sale.paidAmountPaise)}\n" +
                                    "Balance Due: ${Money.formatRupees(sale.balanceDuePaise)}\n" +
                                    "Status: ${sale.paymentStatus}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invoice"))
                        }
                    )
                }
            }
        }
    }

    if (showPosDialog) {
        Dialog(
            onDismissRequest = { showPosDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POS Billing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showPosDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Customer selection
                        var customerExpanded by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { customerExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Customer", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = selectedCustomer?.name ?: "Walk-in Customer",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = customerExpanded,
                                onDismissRequest = { customerExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Walk-in Customer") },
                                    onClick = {
                                        viewModel.selectCustomer(null)
                                        customerExpanded = false
                                    }
                                )
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.name} (${cust.phone})") },
                                        onClick = {
                                            viewModel.selectCustomer(cust)
                                            customerExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Product add dropdown
                        var productExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { productExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Product to Cart")
                        }
                        DropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (${Money.formatRupees(prod.sellingPricePaise)})") },
                                    onClick = {
                                        viewModel.addToCart(prod)
                                        productExpanded = false
                                    }
                                )
                            }
                        }

                        // Cart items list
                        Text(
                            text = "Cart Items (${cartItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        cartItems.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.product.name, fontWeight = FontWeight.Bold)
                                        Text("Unit: ${Money.formatRupees(item.unitPricePaise)} | ${item.product.unit}")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(
                                                onClick = { viewModel.updateCartItemQuantity(item.product.id, item.quantity - 1.0) }
                                            ) { Text("-") }
                                            Text(
                                                text = " ${item.quantity} ",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            OutlinedButton(
                                                onClick = { viewModel.updateCartItemQuantity(item.product.id, item.quantity + 1.0) }
                                            ) { Text("+") }
                                        }
                                    }
                                    IconButton(onClick = { viewModel.updateCartItemQuantity(item.product.id, 0.0) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        // Payment mode
                        var paymentModeExpanded by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { paymentModeExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Payment Mode", style = MaterialTheme.typography.labelMedium)
                                    Text(paymentMode, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = paymentModeExpanded,
                                onDismissRequest = { paymentModeExpanded = false }
                            ) {
                                listOf("CASH", "UPI", "CARD", "CREDIT").forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode) },
                                        onClick = {
                                            viewModel.setPaymentMode(mode)
                                            paymentModeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Paid Amount field
                        OutlinedTextField(
                            value = paidAmountRupees,
                            onValueChange = { viewModel.setPaidAmount(it) },
                            label = { Text("Paid Amount (Leave empty for Full Payment)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Calculation total and action
                    val estimatedTotal = cartItems.sumOf { (it.quantity * it.unitPricePaise).toLong() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = Money.formatRupees(estimatedTotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.checkoutSale {
                                    showPosDialog = false
                                }
                            },
                            enabled = cartItems.isNotEmpty()
                        ) {
                            Text(stringResource(R.string.action_complete_sale))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleCard(
    sale: SaleWithCustomer,
    onShareInvoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (sale.paymentStatus) {
                        PaymentStatus.PAID -> MaterialTheme.colorScheme.primaryContainer
                        PaymentStatus.PARTIAL -> MaterialTheme.colorScheme.tertiaryContainer
                        PaymentStatus.PENDING -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = sale.paymentStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customer: ${sale.customerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(sale.saleDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = Money.formatRupees(sale.totalAmountPaise),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Paid", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = Money.formatRupees(sale.paidAmountPaise),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (sale.balanceDuePaise > 0L) {
                    Column {
                        Text("Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Text(
                            text = Money.formatRupees(sale.balanceDuePaise),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onShareInvoice) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

