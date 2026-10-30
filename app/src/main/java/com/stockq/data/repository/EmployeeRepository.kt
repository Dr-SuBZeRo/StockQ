package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.AttendanceRecord
import com.stockq.core.database.entity.AttendanceStatus
import com.stockq.core.database.entity.BusinessExpense
import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.EmployeeAdvance
import com.stockq.core.database.entity.WagePayment
import com.stockq.core.database.entity.WageType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class WageCalculationResult(
    val employee: Employee,
    val presentDays: Int,
    val halfDays: Int,
    val grossWagePaise: Long,
    val pendingAdvancePaise: Long,
    val netPayablePaise: Long
)

class EmployeeRepository(private val database: StockQDatabase) {

    private val employeeDao = database.employeeDao()
    private val expenseDao = database.businessExpenseDao()

    fun getAllActiveEmployees(): Flow<List<Employee>> = employeeDao.getAllActiveEmployees()

    fun getActiveEmployeeCount(): Flow<Int> = employeeDao.getActiveEmployeeCount()

    suspend fun getEmployeeById(id: Long): Employee? = employeeDao.getEmployeeById(id)

    fun getAttendanceForDay(epochDay: Long = LocalDate.now().toEpochDay()): Flow<List<AttendanceRecord>> =
        employeeDao.getAttendanceForDay(epochDay)

    fun getPendingAdvancesForEmployee(employeeId: Long): Flow<List<EmployeeAdvance>> =
        employeeDao.getPendingAdvancesForEmployee(employeeId)

    fun getWagePaymentsForEmployee(employeeId: Long): Flow<List<WagePayment>> =
        employeeDao.getWagePaymentsForEmployee(employeeId)

    suspend fun createEmployee(
        name: String,
        phone: String = "",
        role: String = "Helper",
        wageType: WageType = WageType.DAILY,
        standardWageRatePaise: Long = 0L
    ): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Employee name cannot be blank"))
        }

        val employee = Employee(
            name = name.trim(),
            phone = phone.trim(),
            role = role.trim(),
            wageType = wageType,
            standardWageRatePaise = standardWageRatePaise
        )

        val id = employeeDao.insertEmployee(employee)
        return Result.success(id)
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun setArchived(employeeId: Long, isArchived: Boolean) {
        val emp = employeeDao.getEmployeeById(employeeId)
        if (emp != null) {
            employeeDao.updateEmployee(emp.copy(isArchived = isArchived))
        }
    }

    suspend fun recordAttendance(
        employeeId: Long,
        status: AttendanceStatus,
        dateEpochDay: Long = LocalDate.now().toEpochDay(),
        overtimeHours: Double = 0.0,
        notes: String = ""
    ): Long {
        return employeeDao.recordAttendance(
            AttendanceRecord(
                employeeId = employeeId,
                dateEpochDay = dateEpochDay,
                status = status,
                overtimeHours = overtimeHours,
                notes = notes.trim()
            )
        )
    }

    suspend fun issueAdvance(
        employeeId: Long,
        amountPaise: Long,
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (amountPaise <= 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Advance amount must be greater than zero"))
        }

        val employee = employeeDao.getEmployeeById(employeeId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Employee #$employeeId not found"))

        val advanceId = employeeDao.insertAdvance(
            EmployeeAdvance(
                employeeId = employeeId,
                amountPaise = amountPaise,
                date = System.currentTimeMillis(),
                notes = notes.trim()
            )
        )

        // Record as business expense
        expenseDao.insertExpense(
            BusinessExpense(
                category = "Salary & Wages",
                title = "Advance to ${employee.name}",
                amountPaise = amountPaise,
                notes = notes.trim().ifBlank { "Salary Advance" }
            )
        )

        Result.success(advanceId)
    }

    suspend fun calculateWages(
        employeeId: Long,
        startEpochDay: Long,
        endEpochDay: Long
    ): WageCalculationResult {
        val employee = employeeDao.getEmployeeById(employeeId)
            ?: throw IllegalArgumentException("Employee #$employeeId not found")

        val presentDays = employeeDao.getPresentDaysCount(employeeId, startEpochDay, endEpochDay)
        val halfDays = employeeDao.getHalfDaysCount(employeeId, startEpochDay, endEpochDay)
        val pendingAdvance = employeeDao.getTotalPendingAdvances(employeeId)

        val grossWagePaise = when (employee.wageType) {
            WageType.DAILY -> {
                (presentDays * employee.standardWageRatePaise) + (halfDays * (employee.standardWageRatePaise / 2))
            }
            WageType.MONTHLY -> {
                employee.standardWageRatePaise
            }
            WageType.HOURLY -> {
                presentDays * 8 * employee.standardWageRatePaise
            }
        }

        val netPayablePaise = (grossWagePaise - pendingAdvance).coerceAtLeast(0L)

        return WageCalculationResult(
            employee = employee,
            presentDays = presentDays,
            halfDays = halfDays,
            grossWagePaise = grossWagePaise,
            pendingAdvancePaise = pendingAdvance,
            netPayablePaise = netPayablePaise
        )
    }

    suspend fun payoutWages(
        employeeId: Long,
        periodStartDate: Long,
        periodEndDate: Long,
        grossWagePaise: Long,
        advanceDeductedPaise: Long,
        netPaidPaise: Long,
        paymentMode: String = "CASH",
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        val employee = employeeDao.getEmployeeById(employeeId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Employee #$employeeId not found"))

        val paymentId = employeeDao.insertWagePayment(
            WagePayment(
                employeeId = employeeId,
                periodStartDate = periodStartDate,
                periodEndDate = periodEndDate,
                calculatedGrossWagePaise = grossWagePaise,
                advancesDeductedPaise = advanceDeductedPaise,
                netPaidPaise = netPaidPaise,
                paymentMode = paymentMode,
                notes = notes.trim()
            )
        )

        if (advanceDeductedPaise > 0L) {
            employeeDao.markAdvancesAsDeducted(employeeId)
        }

        // Record net wage as operational business expense
        if (netPaidPaise > 0L) {
            expenseDao.insertExpense(
                BusinessExpense(
                    category = "Salary & Wages",
                    title = "Wage payout to ${employee.name}",
                    amountPaise = netPaidPaise,
                    notes = notes.trim().ifBlank { "Wage payout" }
                )
            )
        }

        Result.success(paymentId)
    }
}

