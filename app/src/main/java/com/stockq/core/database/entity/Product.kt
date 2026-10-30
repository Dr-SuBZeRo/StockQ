package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ProductCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["sku"]),
        Index(value = ["barcode"]),
        Index(value = ["name"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long? = null,
    val name: String,
    val description: String = "",
    val sku: String = "",
    val barcode: String = "",
    val unit: String = "pcs",
    val costPricePaise: Long = 0L,
    val sellingPricePaise: Long = 0L,
    val taxRatePercent: Double = 0.0,
    val isTaxInclusive: Boolean = false,
    val currentStockMilli: Long = 0L,
    val minStockThresholdMilli: Long = 5000L,
    val idealReorderQuantityMilli: Long = 20000L,
    val preferredSupplierId: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Backward-compatible double accessors */
    val currentStock: Double get() = Quantity.toDouble(currentStockMilli)
    val minStockThreshold: Double get() = Quantity.toDouble(minStockThresholdMilli)
    val idealReorderQuantity: Double get() = Quantity.toDouble(idealReorderQuantityMilli)
}
