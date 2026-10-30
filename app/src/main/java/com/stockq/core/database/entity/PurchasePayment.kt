package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_payments",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["supplierId"]),
        Index(value = ["paymentDate"])
    ]
)
data class PurchasePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val purchaseId: Long,
    val supplierId: Long,
    val amountPaise: Long,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "CASH", // CASH, UPI, BANK_TRANSFER, CHEQUE
    val referenceNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

