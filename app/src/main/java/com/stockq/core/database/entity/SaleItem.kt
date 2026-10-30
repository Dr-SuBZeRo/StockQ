package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
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
        Index(value = ["saleId"]),
        Index(value = ["productId"]),
        Index(value = ["batchId"])
    ]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleId: Long,
    val productId: Long,
    val batchId: Long? = null,
    val productName: String = "",
    val productUnit: String = "pcs",
    val quantityMilli: Long,
    val unitPricePaise: Long,
    val costPricePaise: Long, // Snapshot of cost basis at sale time for accurate COGS
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val totalItemPricePaise: Long = 0L,
    val itemProfitPaise: Long = 0L // Revenue minus Cost
) {
    val quantity: Double get() = Quantity.toDouble(quantityMilli)
}

