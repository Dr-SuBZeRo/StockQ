package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplier_quotations",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["productId"]),
        Index(value = ["quoteDate"])
    ]
)
data class SupplierQuotation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val supplierId: Long,
    val productId: Long,
    val quotationRef: String = "",
    val quoteDate: Long = System.currentTimeMillis(),
    val validUntilDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val unitCostPaise: Long,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val freightPaise: Long = 0L,
    val effectiveUnitCostPaise: Long,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

