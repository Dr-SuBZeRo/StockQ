package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "business_expenses",
    foreignKeys = [
        ForeignKey(
            entity = ServiceJob::class,
            parentColumns = ["id"],
            childColumns = ["linkedJobId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["category"]),
        Index(value = ["expenseDate"]),
        Index(value = ["linkedJobId"])
    ]
)
data class BusinessExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String, // Rent, Electricity, Tea/Snacks, Travel, Packaging, Maintenance, Salary/Wages, Other
    val title: String,
    val amountPaise: Long,
    val expenseDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH",
    val referenceId: String = "",
    val linkedJobId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

