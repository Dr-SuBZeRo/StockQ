package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstin: String = "",
    val creditLimitPaise: Long = 0L,
    val outstandingDuePaise: Long = 0L,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

