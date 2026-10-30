package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.AttendanceRecord
import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.EmployeeAdvance
import com.stockq.core.database.entity.WagePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    // === Employee Management ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT COUNT(*) FROM employees WHERE isArchived = 0")
    fun getActiveEmployeeCount(): Flow<Int>

    // === Attendance Records ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendance(attendance: AttendanceRecord): Long

    @Query("SELECT * FROM attendance_records WHERE dateEpochDay = :epochDay")
    fun getAttendanceForDay(epochDay: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY dateEpochDay ASC")
    fun getAttendanceForEmployeePeriod(employeeId: Long, startEpochDay: Long, endEpochDay: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE employeeId = :employeeId AND dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay AND status = 'PRESENT'")
    suspend fun getPresentDaysCount(employeeId: Long, startEpochDay: Long, endEpochDay: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE employeeId = :employeeId AND dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay AND status = 'HALF_DAY'")
    suspend fun getHalfDaysCount(employeeId: Long, startEpochDay: Long, endEpochDay: Long): Int

    // === Employee Advances ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: EmployeeAdvance): Long

    @Query("SELECT * FROM employee_advances WHERE employeeId = :employeeId AND isDeducted = 0 ORDER BY date DESC")
    fun getPendingAdvancesForEmployee(employeeId: Long): Flow<List<EmployeeAdvance>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM employee_advances WHERE employeeId = :employeeId AND isDeducted = 0")
    suspend fun getTotalPendingAdvances(employeeId: Long): Long

    @Query("UPDATE employee_advances SET isDeducted = 1 WHERE employeeId = :employeeId AND isDeducted = 0")
    suspend fun markAdvancesAsDeducted(employeeId: Long)

    // === Wage Payments ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWagePayment(wagePayment: WagePayment): Long

    @Query("SELECT * FROM wage_payments WHERE employeeId = :employeeId ORDER BY paymentDate DESC")
    fun getWagePaymentsForEmployee(employeeId: Long): Flow<List<WagePayment>>

    @Query("SELECT * FROM wage_payments ORDER BY paymentDate DESC")
    fun getAllWagePayments(): Flow<List<WagePayment>>

    @Query("SELECT COALESCE(SUM(netPaidPaise), 0) FROM wage_payments WHERE paymentDate >= :startDate AND paymentDate <= :endDate")
    fun getTotalWagesPaidBetween(startDate: Long, endDate: Long): Flow<Long>
}

