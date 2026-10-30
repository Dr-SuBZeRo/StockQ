package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["purchaseDate"]),
        Index(value = ["status"])
    ]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val supplierId: Long,
    val invoiceNumber: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val subtotalPaise: Long = 0L,
    val discountPaise: Long = 0L,
    val taxPaise: Long = 0L,
    val transportChargesPaise: Long = 0L,
    val totalAmountPaise: Long = 0L,
    val status: PurchaseStatus = PurchaseStatus.DRAFT,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paidAmountPaise: Long = 0L,
    val notes: String = "",
    val confirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

