package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "business_documents",
    indices = [
        Index(value = ["title"]),
        Index(value = ["expiryDate"])
    ]
)
data class BusinessDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val documentType: DocumentType = DocumentType.LICENSE,
    val documentNumber: String = "",
    val issuingAuthority: String = "",
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val reminderDaysBefore: Int = 30,
    val fileUri: String? = null,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

