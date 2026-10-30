package com.subzero.stockq.feature.productedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subzero.stockq.R
import com.subzero.stockq.core.ui.components.StockQTopBar

@Composable
fun AddEditProductScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var productName by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var currentStock by remember { mutableStateOf("") }
    var minThreshold by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_add_product),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Barcode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("Cost Price (₹)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = { Text("Selling Price (₹)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentStock,
                onValueChange = { currentStock = it },
                label = { Text("Initial Stock Quantity") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = minThreshold,
                onValueChange = { minThreshold = it },
                label = { Text("Low Stock Alert Threshold") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
