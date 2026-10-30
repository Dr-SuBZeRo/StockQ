package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.BusinessDocument
import com.stockq.core.database.entity.DocumentType
import kotlinx.coroutines.flow.Flow

class BusinessDocumentRepository(private val database: StockQDatabase) {

    private val documentDao = database.businessDocumentDao()

    fun getAllActiveDocuments(): Flow<List<BusinessDocument>> = documentDao.getAllActiveDocuments()

    fun getExpiringDocuments(daysAhead: Int = 30): Flow<List<BusinessDocument>> {
        val thresholdDate = System.currentTimeMillis() + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
        return documentDao.getExpiringDocuments(thresholdDate)
    }

    fun getExpiringDocumentsCount(daysAhead: Int = 30): Flow<Int> {
        val thresholdDate = System.currentTimeMillis() + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
        return documentDao.getExpiringDocumentsCount(thresholdDate)
    }

    fun getExpiredDocumentsCount(): Flow<Int> =
        documentDao.getExpiredDocumentsCount(System.currentTimeMillis())

    suspend fun getDocumentById(id: Long): BusinessDocument? = documentDao.getDocumentById(id)

    suspend fun createDocument(
        title: String,
        documentType: DocumentType = DocumentType.LICENSE,
        documentNumber: String = "",
        issuingAuthority: String = "",
        issueDate: Long? = null,
        expiryDate: Long? = null,
        reminderDaysBefore: Int = 30,
        fileUri: String? = null,
        notes: String = ""
    ): Result<Long> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Document title cannot be blank"))
        }

        val document = BusinessDocument(
            title = title.trim(),
            documentType = documentType,
            documentNumber = documentNumber.trim(),
            issuingAuthority = issuingAuthority.trim(),
            issueDate = issueDate,
            expiryDate = expiryDate,
            reminderDaysBefore = reminderDaysBefore,
            fileUri = fileUri,
            notes = notes.trim()
        )

        val id = documentDao.insertDocument(document)
        return Result.success(id)
    }

    suspend fun updateDocument(document: BusinessDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: BusinessDocument) {
        documentDao.deleteDocument(document)
    }
}

