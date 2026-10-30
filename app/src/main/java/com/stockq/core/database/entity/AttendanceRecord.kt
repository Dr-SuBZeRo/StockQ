package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["employeeId", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val employeeId: Long,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val overtimeHours: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

