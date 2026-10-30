package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.Batch
import kotlinx.coroutines.flow.Flow

data class BatchWithProduct(
    val id: Long,
    val productId: Long,
    val productName: String,
    val productUnit: String,
    val batchNumber: String,
    val mfgDate: Long?,
    val expiryDate: Long,
    val currentStockMilli: Long,
    val purchasePricePaise: Long,
    val isArchived: Boolean
)

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches WHERE productId = :productId AND isArchived = 0 ORDER BY expiryDate ASC")
    fun getActiveBatchesForProduct(productId: Long): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE productId = :productId AND isArchived = 0 AND currentStockMilli > 0 AND expiryDate > :currentTimestamp ORDER BY expiryDate ASC")
    suspend fun getSellableBatchesFIFO(productId: Long, currentTimestamp: Long = System.currentTimeMillis()): List<Batch>

    @Query("""
        SELECT 
            b.id AS id,
            b.productId AS productId,
            p.name AS productName,
            p.unit AS productUnit,
            b.batchNumber AS batchNumber,
            b.mfgDate AS mfgDate,
            b.expiryDate AS expiryDate,
            b.currentStockMilli AS currentStockMilli,
            b.purchasePricePaise AS purchasePricePaise,
            b.isArchived AS isArchived
        FROM batches b
        INNER JOIN products p ON b.productId = p.id
        WHERE b.isArchived = 0 AND b.currentStockMilli > 0 AND b.expiryDate <= :expiryThreshold
        ORDER BY b.expiryDate ASC
    """)
    fun getBatchesExpiringBefore(expiryThreshold: Long): Flow<List<BatchWithProduct>>

    @Query("""
        SELECT COUNT(*) FROM batches 
        WHERE isArchived = 0 AND currentStockMilli > 0 AND expiryDate <= :currentTimestamp
    """)
    fun getExpiredBatchCount(currentTimestamp: Long = System.currentTimeMillis()): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM batches 
        WHERE isArchived = 0 AND currentStockMilli > 0 AND expiryDate > :currentTimestamp AND expiryDate <= :upcomingTimestamp
    """)
    fun getExpiringSoonBatchCount(currentTimestamp: Long = System.currentTimeMillis(), upcomingTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch): Long

    @Update
    suspend fun updateBatch(batch: Batch)

    @Query("UPDATE batches SET currentStockMilli = :newStockMilli WHERE id = :batchId")
    suspend fun updateStock(batchId: Long, newStockMilli: Long)

    @Query("UPDATE batches SET isArchived = :isArchived WHERE id = :batchId")
    suspend fun setBatchArchived(batchId: Long, isArchived: Boolean)

    @Query("SELECT * FROM batches WHERE productId = :productId AND batchNumber = :batchNumber LIMIT 1")
    suspend fun findByProductAndBatchNumber(productId: Long, batchNumber: String): Batch?
}
