package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["saleDate"]),
        Index(value = ["invoiceNumber"]),
        Index(value = ["status"])
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long? = null,
    val customerName: String = "",
    val invoiceNumber: String = "",
    val saleDate: Long = System.currentTimeMillis(),
    val subtotalPaise: Long = 0L,
    val discountPaise: Long = 0L,
    val taxPaise: Long = 0L,
    val totalAmountPaise: Long = 0L,
    val paidAmountPaise: Long = 0L,
    val balanceDuePaise: Long = 0L,
    val dueDate: Long? = null,
    val paymentMode: String = "CASH", // CASH, UPI, CARD, CREDIT, SPLIT
    val status: SaleStatus = SaleStatus.COMPLETED,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

