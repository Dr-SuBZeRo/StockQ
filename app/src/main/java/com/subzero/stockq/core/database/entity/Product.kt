package com.subzero.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val sku: String = "",
    val barcode: String = "",
    val unit: String = "pcs",
    val costPricePaise: Long = 0L,
    val sellingPricePaise: Long = 0L,
    val currentStock: Double = 0.0,
    val minStockThreshold: Double = 5.0,
    val idealReorderQuantity: Double = 20.0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

