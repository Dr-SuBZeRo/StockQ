package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotations",
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
        Index(value = ["quotationNumber"]),
        Index(value = ["quotationDate"]),
        Index(value = ["status"])
    ]
)
data class Quotation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val quotationNumber: String,
    val quotationDate: Long = System.currentTimeMillis(),
    val validUntilDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000), // 14 days default
    val subtotalPaise: Long = 0L,
    val discountPaise: Long = 0L,
    val taxPaise: Long = 0L,
    val totalAmountPaise: Long = 0L,
    val status: QuotationStatus = QuotationStatus.DRAFT,
    val convertedSaleId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

