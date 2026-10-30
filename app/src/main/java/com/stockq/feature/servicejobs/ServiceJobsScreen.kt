package com.stockq.feature.servicejobs

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.database.entity.JobStatus
import com.stockq.core.database.entity.ServiceJob
import com.stockq.core.model.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceJobsScreen(
    viewModel: ServiceJobsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
    val pendingBalancePaise by viewModel.pendingBalancePaise.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var jobForPayment by remember { mutableStateOf<ServiceJob?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_service_jobs)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Job")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Stats Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Repair Jobs", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$activeCount In Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pending Dues", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = Money.formatRupees(pendingBalancePaise),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (pendingBalancePaise > 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (jobs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.empty_service_jobs),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs, key = { it.id }) { job ->
                        JobCard(
                            job = job,
                            onUpdateStatus = { newStatus -> viewModel.updateJobStatus(job.id, newStatus) },
                            onRecordPayment = { jobForPayment = job }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var customerName by remember { mutableStateOf("") }
        var customerPhone by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var labourRupees by remember { mutableStateOf("") }
        var materialRupees by remember { mutableStateOf("") }
        var directExpenseRupees by remember { mutableStateOf("") }
        var paidRupees by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Register Service / Repair Job") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Title / Device *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Problem Description / Work Required") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = labourRupees,
                            onValueChange = { labourRupees = it },
                            label = { Text("Labour (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = materialRupees,
                            onValueChange = { materialRupees = it },
                            label = { Text("Materials (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = directExpenseRupees,
                            onValueChange = { directExpenseRupees = it },
                            label = { Text("Cost Incurred (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidRupees,
                            onValueChange = { paidRupees = it },
                            label = { Text("Initial Advance (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createJob(
                            customerName, customerPhone, title, description,
                            labourRupees, materialRupees, directExpenseRupees, paidRupees
                        ) {
                            showCreateDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && customerName.isNotBlank()
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

    jobForPayment?.let { job ->
        var paymentAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { jobForPayment = null },
            title = { Text("Record Job Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Job: ${job.title}", fontWeight = FontWeight.Bold)
                    Text("Outstanding: ${Money.formatRupees(job.balanceDuePaise)}", color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Payment Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordJobPayment(job.id, paymentAmount) {
                            jobForPayment = null
                        }
                    },
                    enabled = paymentAmount.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_record_payment))
                }
            },
            dismissButton = {
                TextButton(onClick = { jobForPayment = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun JobCard(
    job: ServiceJob,
    onUpdateStatus: (JobStatus) -> Unit,
    onRecordPayment: () -> Unit
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
                Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (job.status) {
                        JobStatus.DELIVERED -> MaterialTheme.colorScheme.primaryContainer
                        JobStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                        JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = job.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Customer: ${job.customerName} ${if (job.customerPhone.isNotBlank()) "(${job.customerPhone})" else ""}")
            if (job.description.isNotBlank()) {
                Text("Issue: ${job.description}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Charge", style = MaterialTheme.typography.labelSmall)
                    Text(Money.formatRupees(job.totalChargePaise), fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Expenses", style = MaterialTheme.typography.labelSmall)
                    Text(Money.formatRupees(job.directExpensesPaise))
                }
                Column {
                    Text("Est. Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(Money.formatRupees(job.estimatedProfitPaise), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    Text(Money.formatRupees(job.balanceDuePaise), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (job.balanceDuePaise > 0L) {
                    OutlinedButton(onClick = onRecordPayment, modifier = Modifier.weight(1f)) {
                        Text("Collect Due")
                    }
                }
                if (job.status != JobStatus.DELIVERED && job.status != JobStatus.CANCELLED) {
                    Button(
                        onClick = {
                            val next = when (job.status) {
                                JobStatus.RECEIVED -> JobStatus.IN_PROGRESS
                                JobStatus.IN_PROGRESS -> JobStatus.COMPLETED
                                JobStatus.WAITING_PARTS -> JobStatus.COMPLETED
                                JobStatus.COMPLETED -> JobStatus.DELIVERED
                                else -> JobStatus.DELIVERED
                            }
                            onUpdateStatus(next)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        val label = when (job.status) {
                            JobStatus.RECEIVED -> "Start Work"
                            JobStatus.IN_PROGRESS -> "Mark Ready"
                            JobStatus.WAITING_PARTS -> "Mark Ready"
                            JobStatus.COMPLETED -> "Deliver to Customer"
                            else -> "Update"
                        }
                        Text(label)
                    }
                }
            }
        }
    }
}

