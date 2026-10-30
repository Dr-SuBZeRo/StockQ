package com.subzero.stockq.feature.suppliers

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subzero.stockq.R
import com.subzero.stockq.core.database.entity.Supplier
import com.subzero.stockq.core.ui.components.StockQTopBar

@Composable
fun SuppliersScreen(
    viewModel: SuppliersViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    var supplierToDelete by remember { mutableStateOf<Supplier?>(null) }

    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_suppliers),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (suppliers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.empty_suppliers),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.openAddDialog() }) {
                    Text("Add First Supplier")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suppliers, key = { it.id }) { supplier ->
                    SupplierCard(
                        supplier = supplier,
                        onEdit = { viewModel.openEditDialog(supplier) },
                        onDelete = { supplierToDelete = supplier }
                    )
                }
            }
        }

        // Add/Edit Dialog
        if (dialogState.isOpen) {
            SupplierDialog(
                state = dialogState,
                onDismiss = { viewModel.closeDialog() },
                onNameChange = { viewModel.updateName(it) },
                onContactChange = { viewModel.updateContactPerson(it) },
                onPhoneChange = { viewModel.updatePhone(it) },
                onEmailChange = { viewModel.updateEmail(it) },
                onGstinChange = { viewModel.updateGstin(it) },
                onAddressChange = { viewModel.updateAddress(it) },
                onTermsChange = { viewModel.updatePaymentTerms(it) },
                onSave = { viewModel.saveSupplier() }
            )
        }

        // Delete Confirmation
        supplierToDelete?.let { supplier ->
            AlertDialog(
                onDismissRequest = { supplierToDelete = null },
                title = { Text("Archive Supplier?") },
                text = { Text("Are you sure you want to archive ${supplier.name}? Existing purchase records referencing this supplier will be preserved.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSupplier(supplier)
                            supplierToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Archive")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { supplierToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SupplierCard(
    supplier: Supplier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (supplier.contactPerson.isNotBlank()) {
                Text("Contact: ${supplier.contactPerson}", style = MaterialTheme.typography.bodyMedium)
            }
            if (supplier.phone.isNotBlank()) {
                Text("Phone: ${supplier.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (supplier.gstin.isNotBlank()) {
                Text("GSTIN: ${supplier.gstin}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (supplier.paymentTermsDays > 0) {
                Text("Credit terms: Net ${supplier.paymentTermsDays} days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun SupplierDialog(
    state: SupplierDialogState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onGstinChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onTermsChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.supplierId == null) "Add Supplier" else "Edit Supplier",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Supplier Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.contactPerson,
                    onValueChange = onContactChange,
                    label = { Text("Contact Person") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.gstin,
                    onValueChange = onGstinChange,
                    label = { Text("GSTIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.address,
                    onValueChange = onAddressChange,
                    label = { Text("Address / City") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.paymentTermsDays,
                    onValueChange = onTermsChange,
                    label = { Text("Payment Terms (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Supplier")
                }
            }
        }
    }
}
