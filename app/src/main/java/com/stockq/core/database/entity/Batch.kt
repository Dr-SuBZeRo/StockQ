package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "batches",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["batchNumber"]),
        Index(value = ["expiryDate"])
    ]
)
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val batchNumber: String,
    val mfgDate: Long? = null,
    val expiryDate: Long,
    val currentStockMilli: Long = 0L,
    val purchaseItemId: Long? = null,
    val purchasePricePaise: Long = 0L,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val currentStock: Double get() = Quantity.toDouble(currentStockMilli)
    val isExpired: Boolean get() = expiryDate <= System.currentTimeMillis()
}

