package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val purchaseId: Long,
    val productId: Long,
    val batchNumber: String? = null,
    val expiryDate: Long? = null,
    val quantityMilli: Long,
    val unitCostPaise: Long,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val transportAllocationPaise: Long = 0L,
    val effectiveUnitCostPaise: Long = 0L,
    val totalItemCostPaise: Long = 0L
) {
    val quantity: Double get() = Quantity.toDouble(quantityMilli)
}
