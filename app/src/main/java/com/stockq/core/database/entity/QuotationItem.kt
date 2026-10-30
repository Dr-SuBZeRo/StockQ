package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stockq.core.model.Quantity

@Entity(
    tableName = "quotation_items",
    foreignKeys = [
        ForeignKey(
            entity = Quotation::class,
            parentColumns = ["id"],
            childColumns = ["quotationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["quotationId"]),
        Index(value = ["productId"])
    ]
)
data class QuotationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val quotationId: Long,
    val productId: Long? = null,
    val itemTitle: String,
    val isService: Boolean = false,
    val quantityMilli: Long = 1000L,
    val unitPricePaise: Long = 0L,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val totalItemPricePaise: Long = 0L
) {
    val quantity: Double get() = Quantity.toDouble(quantityMilli)
}

