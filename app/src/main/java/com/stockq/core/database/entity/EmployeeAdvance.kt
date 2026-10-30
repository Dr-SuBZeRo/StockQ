package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "employee_advances",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["employeeId"]),
        Index(value = ["date"])
    ]
)
data class EmployeeAdvance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val employeeId: Long,
    val amountPaise: Long,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isDeducted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

