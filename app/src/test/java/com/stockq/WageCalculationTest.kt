package com.stockq

import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.WageType
import org.junit.Assert.assertEquals
import org.junit.Test

class WageCalculationTest {

    @Test
    fun dailyWageWithHalfDaysAndAdvances_computesExactNetPayable() {
        val employee = Employee(
            id = 1L,
            name = "Ramesh Kumar",
            role = "Technician",
            wageType = WageType.DAILY,
            standardWageRatePaise = 70000L // ₹700.00 / day
        )

        val presentDays = 22
        val halfDays = 4
        val pendingAdvancesPaise = 500000L // ₹5,000.00

        // Gross = (22 * 700) + (4 * 350) = 15,400 + 1,400 = ₹16,800.00 (1680000 paise)
        val grossWagePaise = (presentDays * employee.standardWageRatePaise) +
                (halfDays * (employee.standardWageRatePaise / 2))
        assertEquals(1680000L, grossWagePaise)

        // Net Payable = Gross - Advance = 16,800 - 5,000 = ₹11,800.00 (1180000 paise)
        val netPayablePaise = (grossWagePaise - pendingAdvancesPaise).coerceAtLeast(0L)
        assertEquals(1180000L, netPayablePaise)
    }

    @Test
    fun advanceExceedingWage_netPayableClampsToZero() {
        val grossWagePaise = 100000L // ₹1,000.00
        val pendingAdvancePaise = 150000L // ₹1,500.00

        val netPayable = (grossWagePaise - pendingAdvancePaise).coerceAtLeast(0L)
        assertEquals(0L, netPayable)
    }
}

