package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "employees",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val role: String = "Helper", // Helper, Technician, Sales, Cashier, Manager
    val wageType: WageType = WageType.DAILY,
    val standardWageRatePaise: Long = 0L, // Daily rate, monthly salary, or hourly rate in paise
    val joiningDate: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

