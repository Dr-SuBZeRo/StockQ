package com.stockq

import com.stockq.core.database.entity.Batch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchExpiryTest {

    @Test
    fun batch_correctlyEvaluatesExpiredStatus() {
        val now = System.currentTimeMillis()
        val expiredBatch = Batch(
            productId = 1L,
            batchNumber = "BATCH-OLD",
            expiryDate = now - 10000L, // 10 seconds in past
            currentStockMilli = 5000L
        )
        assertTrue(expiredBatch.isExpired)

        val freshBatch = Batch(
            productId = 1L,
            batchNumber = "BATCH-NEW",
            expiryDate = now + 1000000L, // future
            currentStockMilli = 5000L
        )
        assertFalse(freshBatch.isExpired)
    }

    @Test
    fun fifoDeductionOrder_consumesEarliestExpiryFirst() {
        val now = System.currentTimeMillis()
        val batch1 = Batch(id = 1L, productId = 1L, batchNumber = "B1", expiryDate = now + 100000L, currentStockMilli = 10000L)
        val batch2 = Batch(id = 2L, productId = 1L, batchNumber = "B2", expiryDate = now + 500000L, currentStockMilli = 20000L)

        val batches = listOf(batch1, batch2).sortedBy { it.expiryDate }

        var quantityToDeduct = 15000L // 15 units
        val deductions = mutableMapOf<Long, Long>()

        for (b in batches) {
            if (quantityToDeduct <= 0L) break
            val deduct = minOf(b.currentStockMilli, quantityToDeduct)
            deductions[b.id] = deduct
            quantityToDeduct -= deduct
        }

        assertEquals(10000L, deductions[1L]) // Batch 1 exhausted
        assertEquals(5000L, deductions[2L])  // Batch 2 partially deducted
        assertEquals(0L, quantityToDeduct)
    }
}

