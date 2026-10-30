package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstin: String = "",
    val paymentTermsDays: Int = 30,
    val rating: Float = 0.0f,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

