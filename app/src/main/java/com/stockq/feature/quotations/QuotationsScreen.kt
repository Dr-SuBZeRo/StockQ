package com.stockq.feature.quotations

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.database.dao.QuotationWithItems
import com.stockq.core.database.entity.QuotationStatus
import com.stockq.core.model.Money
import com.stockq.data.repository.QuotationItemDraftInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationsScreen(
    viewModel: QuotationsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotations by viewModel.quotations.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
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
                title = { Text(stringResource(R.string.title_quotations)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Quotation")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (quotations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.empty_quotations),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotations, key = { it.quotation.id }) { quoteWithItems ->
                    QuotationCard(
                        quoteWithItems = quoteWithItems,
                        onConvertToSale = { viewModel.convertToSale(quoteWithItems.quotation.id) },
                        onShare = {
                            val q = quoteWithItems.quotation
                            val text = "StockQ Formal Quotation: ${q.quotationNumber}\n" +
                                    "Customer: ${q.customerName}\n" +
                                    "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(q.quotationDate))}\n" +
                                    "Items:\n" + quoteWithItems.items.joinToString("\n") { "• ${it.itemTitle} x ${it.quantity}: ${Money.formatRupees(it.totalItemPricePaise)}" } +
                                    "\nTotal: ${Money.formatRupees(q.totalAmountPaise)}\nValid for 14 days."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Quotation"))
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        var customerName by remember { mutableStateOf("") }
        var customerPhone by remember { mutableStateOf("") }
        var itemTitle by remember { mutableStateOf("") }
        var quantityText by remember { mutableStateOf("1") }
        var priceText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generate Customer Quotation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("Item / Service Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Unit Price (₹) *") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 1.0
                        val pricePaise = (priceText.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        val draftItem = QuotationItemDraftInput(
                            itemTitle = itemTitle,
                            quantity = qty,
                            unitPricePaise = pricePaise
                        )
                        viewModel.createQuotation(
                            customerId = null,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            items = listOf(draftItem)
                        ) {
                            showCreateDialog = false
                        }
                    },
                    enabled = customerName.isNotBlank() && itemTitle.isNotBlank() && priceText.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun QuotationCard(
    quoteWithItems: QuotationWithItems,
    onConvertToSale: () -> Unit,
    onShare: () -> Unit
) {
    val q = quoteWithItems.quotation
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
                    text = q.quotationNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (q.status) {
                        QuotationStatus.CONVERTED_TO_SALE -> MaterialTheme.colorScheme.primaryContainer
                        QuotationStatus.SENT -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = q.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Customer: ${q.customerName}")
            Text(
                text = "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(q.quotationDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Items:", style = MaterialTheme.typography.labelMedium)
            quoteWithItems.items.forEach { itm ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• ${itm.itemTitle} (${itm.quantity}x)")
                    Text(Money.formatRupees(itm.totalItemPricePaise), fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Quote", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = Money.formatRupees(q.totalAmountPaise),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                    if (q.status != QuotationStatus.CONVERTED_TO_SALE) {
                        Button(onClick = onConvertToSale) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.action_convert_to_sale))
                        }
                    }
                }
            }
        }
    }
}

