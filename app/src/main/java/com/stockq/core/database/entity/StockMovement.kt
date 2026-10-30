package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "stock_movements",
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
        Index(value = ["timestamp"]),
        Index(value = ["movementType"]),
        Index(value = ["referenceType", "referenceId"])
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val batchId: Long? = null,
    val movementType: StockMovementType,
    val quantityChangeMilli: Long,
    val balanceAfterMilli: Long,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val unitCostPaise: Long = 0L,
    val reasonOrNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val quantityChange: Double get() = Quantity.toDouble(quantityChangeMilli)
    val balanceAfter: Double get() = Quantity.toDouble(balanceAfterMilli)
}
