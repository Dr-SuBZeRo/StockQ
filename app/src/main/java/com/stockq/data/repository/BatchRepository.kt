package com.stockq.data.repository

import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.dao.BatchWithProduct
import com.stockq.core.database.entity.Batch
import kotlinx.coroutines.flow.Flow

class BatchRepository(private val database: StockQDatabase) {

    private val batchDao = database.batchDao()

    fun getActiveBatchesForProduct(productId: Long): Flow<List<Batch>> =
        batchDao.getActiveBatchesForProduct(productId)

    fun getBatchesExpiringWithinDays(days: Int = 30): Flow<List<BatchWithProduct>> {
        val threshold = System.currentTimeMillis() + (days.toLong() * 24 * 60 * 60 * 1000)
        return batchDao.getBatchesExpiringBefore(threshold)
    }

    fun getExpiredBatchCount(): Flow<Int> = batchDao.getExpiredBatchCount()

    fun getExpiringSoonBatchCount(days: Int = 30): Flow<Int> {
        val now = System.currentTimeMillis()
        val upcoming = now + (days.toLong() * 24 * 60 * 60 * 1000)
        return batchDao.getExpiringSoonBatchCount(now, upcoming)
    }

    suspend fun createBatch(
        productId: Long,
        batchNumber: String,
        expiryDate: Long,
        quantityMilli: Long,
        purchasePricePaise: Long = 0L,
        mfgDate: Long? = null
    ): Long {
        return batchDao.insertBatch(
            Batch(
                productId = productId,
                batchNumber = batchNumber.trim(),
                mfgDate = mfgDate,
                expiryDate = expiryDate,
                currentStockMilli = quantityMilli,
                purchasePricePaise = purchasePricePaise
            )
        )
    }

    suspend fun archiveBatch(batchId: Long) {
        batchDao.setBatchArchived(batchId, true)
    }
}

