package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Long = 1L,
    val storeName: String = "My Store",
    val ownerName: String = "Store Owner",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstin: String = "",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val updatedAt: Long = System.currentTimeMillis()
)

