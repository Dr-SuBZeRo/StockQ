package com.stockq.feature.employees

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.stockq.core.database.entity.AttendanceRecord
import com.stockq.core.database.entity.AttendanceStatus
import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.WageType
import com.stockq.core.model.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    viewModel: EmployeesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val todayAttendance by viewModel.todayAttendance.collectAsStateWithLifecycle()
    val wageCalc by viewModel.wageCalc.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var employeeForAdvance by remember { mutableStateOf<Employee?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_employees)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (employees.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.empty_employees),
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
                items(employees, key = { it.id }) { employee ->
                    val attendance = todayAttendance.find { it.employeeId == employee.id }
                    EmployeeCard(
                        employee = employee,
                        todayAttendance = attendance,
                        onMarkAttendance = { status -> viewModel.markAttendance(employee.id, status) },
                        onIssueAdvance = { employeeForAdvance = employee },
                        onCalculateWage = { viewModel.calculateWages(employee.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Helper") }
        var wageRateRupees by remember { mutableStateOf("500") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Employee / Staff") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Employee Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role / Designation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wageRateRupees,
                        onValueChange = { wageRateRupees = it },
                        label = { Text("Daily Wage Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addEmployee(name, phone, role, WageType.DAILY, wageRateRupees) {
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    employeeForAdvance?.let { emp ->
        var advanceAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { employeeForAdvance = null },
            title = { Text("Issue Advance to ${emp.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = advanceAmount,
                        onValueChange = { advanceAmount = it },
                        label = { Text("Advance Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.issueAdvance(emp.id, advanceAmount) {
                            employeeForAdvance = null
                        }
                    },
                    enabled = advanceAmount.isNotBlank()
                ) {
                    Text("Issue Advance")
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeForAdvance = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    wageCalc?.let { calc ->
        AlertDialog(
            onDismissRequest = { viewModel.clearWageCalc() },
            title = { Text("Wage Settlement: ${calc.employee.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Present Days (Last 30 Days): ${calc.presentDays}")
                    Text("Half Days: ${calc.halfDays}")
                    Text("Gross Wage: ${Money.formatRupees(calc.grossWagePaise)}", fontWeight = FontWeight.SemiBold)
                    Text("Advance Deducted: ${Money.formatRupees(calc.pendingAdvancePaise)}", color = MaterialTheme.colorScheme.error)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Net Payable: ${Money.formatRupees(calc.netPayablePaise)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.payoutWages(calc.employee.id) {} }) {
                    Text("Pay & Record Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearWageCalc() }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    todayAttendance: AttendanceRecord?,
    onMarkAttendance: (AttendanceStatus) -> Unit,
    onIssueAdvance: () -> Unit,
    onCalculateWage: () -> Unit
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
                Column {
                    Text(employee.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${employee.role} • ${Money.formatRupees(employee.standardWageRatePaise)} / day", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Today's Attendance:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = todayAttendance?.status == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMarkAttendance(status) },
                        label = { Text(status.name.take(4)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onIssueAdvance, modifier = Modifier.weight(1f)) {
                    Text("Advance")
                }
                Button(onClick = onCalculateWage, modifier = Modifier.weight(1f)) {
                    Text("Settle Wage")
                }
            }
        }
    }
}

