package com.subzero.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subzero.stockq.core.database.entity.Purchase
import com.subzero.stockq.core.database.entity.PurchaseStatus
import kotlinx.coroutines.flow.Flow

data class PurchaseWithSupplier(
    @Embedded val purchase: Purchase,
    val supplierName: String
)

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("""
        SELECT p.*, s.name AS supplierName
        FROM purchases p
        INNER JOIN suppliers s ON p.supplierId = s.id
        ORDER BY p.purchaseDate DESC, p.id DESC
    """)
    fun getAllPurchasesWithSupplier(): Flow<List<PurchaseWithSupplier>>

    @Query("SELECT * FROM purchases WHERE status = :status ORDER BY purchaseDate DESC")
    fun getPurchasesByStatus(status: PurchaseStatus): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    fun getPurchaseById(id: Long): Flow<Purchase?>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseDirect(id: Long): Purchase?

    @Query("SELECT COUNT(*) FROM purchases WHERE status = 'CONFIRMED'")
    fun getConfirmedPurchaseCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Query("UPDATE purchases SET status = :status, confirmedAt = :confirmedAt, updatedAt = :timestamp WHERE id = :purchaseId")
    suspend fun updateStatus(
        purchaseId: Long,
        status: PurchaseStatus,
        confirmedAt: Long?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deletePurchase(purchase: Purchase)
}
