package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_payments",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["saleId"]),
        Index(value = ["paymentDate"])
    ]
)
data class CustomerPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long,
    val saleId: Long? = null,
    val amountPaise: Long,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH", // CASH, UPI, BANK_TRANSFER, CHEQUE
    val referenceNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

