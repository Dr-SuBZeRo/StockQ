package com.stockq.feature.reports

import android.content.Intent
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.model.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_reports)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportText = "=== StockQ Daily Profit & Loss ===\n" +
                                "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(summary.startDate))}\n\n" +
                                "Revenue / Sales: ${Money.formatRupees(summary.totalSalesPaise)}\n" +
                                "Cost of Goods Sold (COGS): ${Money.formatRupees(summary.totalCogsPaise)}\n" +
                                "Gross Profit: ${Money.formatRupees(summary.grossProfitPaise)}\n" +
                                "Operating Expenses: ${Money.formatRupees(summary.operatingExpensesPaise)}\n" +
                                "--------------------------------\n" +
                                "Net Profit: ${Money.formatRupees(summary.netProfitPaise)}\n\n" +
                                "=== Cash Flow ===\n" +
                                "Cash Inflow: ${Money.formatRupees(summary.cashInflowPaise)}\n" +
                                "Cash Outflow: ${Money.formatRupees(summary.cashOutflowPaise)}\n" +
                                "Net Cash Flow: ${Money.formatRupees(summary.netCashFlowPaise)}"

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Business Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Profit Highlight Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.netProfitPaise >= 0L) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Today's Net Profit", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = Money.formatRupees(summary.netProfitPaise),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gross Profit (${Money.formatRupees(summary.grossProfitPaise)}) - Expenses (${Money.formatRupees(summary.operatingExpensesPaise)})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // P&L Breakdown Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Income Statement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    PnLRow("Sales Revenue", Money.formatRupees(summary.totalSalesPaise), isPositive = true)
                    PnLRow("Cost of Goods Sold (COGS)", "- ${Money.formatRupees(summary.totalCogsPaise)}", isNegative = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PnLRow("Gross Profit", Money.formatRupees(summary.grossProfitPaise), isBold = true)
                    PnLRow("Operating Expenses", "- ${Money.formatRupees(summary.operatingExpensesPaise)}", isNegative = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PnLRow("Net Business Profit", Money.formatRupees(summary.netProfitPaise), isBold = true, isPositive = summary.netProfitPaise >= 0L)
                }
            }

            // Cash Flow Summary Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cash Flow Movement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    PnLRow("Cash Collections (Inflow)", Money.formatRupees(summary.cashInflowPaise), isPositive = true)
                    PnLRow("Cash Payments (Outflow)", "- ${Money.formatRupees(summary.cashOutflowPaise)}", isNegative = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PnLRow("Net Cash Position", Money.formatRupees(summary.netCashFlowPaise), isBold = true)
                }
            }
        }
    }
}

@Composable
private fun PnLRow(
    label: String,
    amountText: String,
    isBold: Boolean = false,
    isPositive: Boolean = false,
    isNegative: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amountText,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isPositive -> MaterialTheme.colorScheme.primary
                isNegative -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

