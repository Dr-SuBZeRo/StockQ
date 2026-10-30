package com.stockq.feature.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.entity.AttendanceRecord
import com.stockq.core.database.entity.AttendanceStatus
import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.WageType
import com.stockq.data.repository.EmployeeRepository
import com.stockq.data.repository.WageCalculationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class EmployeesViewModel(
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val todayEpoch = LocalDate.now().toEpochDay()

    val employees: StateFlow<List<Employee>> = employeeRepository.getAllActiveEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<List<AttendanceRecord>> = employeeRepository.getAttendanceForDay(todayEpoch)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _wageCalc = MutableStateFlow<WageCalculationResult?>(null)
    val wageCalc: StateFlow<WageCalculationResult?> = _wageCalc.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun addEmployee(
        name: String,
        phone: String,
        role: String,
        wageType: WageType,
        wageRateRupees: String,
        onSuccess: () -> Unit
    ) {
        val wageRatePaise = (wageRateRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
        viewModelScope.launch {
            val result = employeeRepository.createEmployee(
                name = name,
                phone = phone,
                role = role,
                wageType = wageType,
                standardWageRatePaise = wageRatePaise
            )
            result.onSuccess {
                _message.value = "Employee registered"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to add employee"
            }
        }
    }

    fun markAttendance(employeeId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            employeeRepository.recordAttendance(
                employeeId = employeeId,
                status = status,
                dateEpochDay = todayEpoch
            )
            _message.value = "Attendance marked: ${status.name}"
        }
    }

    fun issueAdvance(employeeId: Long, amountRupees: String, onSuccess: () -> Unit) {
        val amountPaise = (amountRupees.toDoubleOrNull()?.times(100))?.toLong()
        if (amountPaise == null || amountPaise <= 0L) {
            _message.value = "Enter valid advance amount"
            return
        }

        viewModelScope.launch {
            val result = employeeRepository.issueAdvance(employeeId, amountPaise)
            result.onSuccess {
                _message.value = "Advance issued and recorded as business expense"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to issue advance"
            }
        }
    }

    fun calculateWages(employeeId: Long) {
        viewModelScope.launch {
            try {
                val startEpoch = todayEpoch - 30L
                val calc = employeeRepository.calculateWages(employeeId, startEpoch, todayEpoch)
                _wageCalc.value = calc
            } catch (e: Exception) {
                _message.value = e.message ?: "Error calculating wages"
            }
        }
    }

    fun clearWageCalc() {
        _wageCalc.value = null
    }

    fun payoutWages(employeeId: Long, onSuccess: () -> Unit) {
        val calc = _wageCalc.value ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val result = employeeRepository.payoutWages(
                employeeId = employeeId,
                periodStartDate = now - (30L * 24 * 60 * 60 * 1000),
                periodEndDate = now,
                grossWagePaise = calc.grossWagePaise,
                advanceDeductedPaise = calc.pendingAdvancePaise,
                netPaidPaise = calc.netPayablePaise
            )
            result.onSuccess {
                _message.value = "Wage payout completed"
                clearWageCalc()
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to payout wage"
            }
        }
    }
}

