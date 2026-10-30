package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.BusinessDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: BusinessDocument): Long

    @Update
    suspend fun updateDocument(document: BusinessDocument)

    @Delete
    suspend fun deleteDocument(document: BusinessDocument)

    @Query("SELECT * FROM business_documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): BusinessDocument?

    @Query("SELECT * FROM business_documents WHERE isArchived = 0 ORDER BY expiryDate ASC")
    fun getAllActiveDocuments(): Flow<List<BusinessDocument>>

    @Query("SELECT * FROM business_documents WHERE isArchived = 0 AND expiryDate IS NOT NULL AND expiryDate <= :thresholdDate ORDER BY expiryDate ASC")
    fun getExpiringDocuments(thresholdDate: Long): Flow<List<BusinessDocument>>

    @Query("SELECT COUNT(*) FROM business_documents WHERE isArchived = 0 AND expiryDate IS NOT NULL AND expiryDate <= :thresholdDate")
    fun getExpiringDocumentsCount(thresholdDate: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM business_documents WHERE isArchived = 0 AND expiryDate IS NOT NULL AND expiryDate < :currentTime")
    fun getExpiredDocumentsCount(currentTime: Long): Flow<Int>
}

