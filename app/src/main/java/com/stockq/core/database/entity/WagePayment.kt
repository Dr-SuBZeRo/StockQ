package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wage_payments",
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
        Index(value = ["paymentDate"])
    ]
)
data class WagePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val employeeId: Long,
    val periodStartDate: Long,
    val periodEndDate: Long,
    val calculatedGrossWagePaise: Long,
    val advancesDeductedPaise: Long = 0L,
    val netPaidPaise: Long,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

