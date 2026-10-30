package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_categories")
data class ProductCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val defaultTaxRatePercent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

