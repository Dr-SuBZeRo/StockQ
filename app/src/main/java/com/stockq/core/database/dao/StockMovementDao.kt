package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stockq.core.database.entity.StockMovement
import kotlinx.coroutines.flow.Flow

data class StockMovementWithProduct(
    val id: Long,
    val productId: Long,
    val productName: String,
    val productUnit: String,
    val movementType: String,
    val quantityChange: Double,
    val balanceAfter: Double,
    val referenceType: String?,
    val referenceId: Long?,
    val unitCostPaise: Long,
    val reasonOrNotes: String,
    val timestamp: Long
)

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC, id DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 50): Flow<List<StockMovement>>

    @Query("""
        SELECT 
            sm.id AS id,
            sm.productId AS productId,
            p.name AS productName,
            p.unit AS productUnit,
            sm.movementType AS movementType,
            sm.quantityChange AS quantityChange,
            sm.balanceAfter AS balanceAfter,
            sm.referenceType AS referenceType,
            sm.referenceId AS referenceId,
            sm.unitCostPaise AS unitCostPaise,
            sm.reasonOrNotes AS reasonOrNotes,
            sm.timestamp AS timestamp
        FROM stock_movements sm
        INNER JOIN products p ON sm.productId = p.id
        ORDER BY sm.timestamp DESC, sm.id DESC
        LIMIT :limit
    """)
    fun getRecentMovementsWithProduct(limit: Int = 50): Flow<List<StockMovementWithProduct>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun getLatestMovementForProduct(productId: Long): StockMovement?

    @Query("SELECT COALESCE(SUM(quantityChange), 0.0) FROM stock_movements WHERE productId = :productId")
    suspend fun calculateNetStockFromLedger(productId: Long): Double

    @Query("SELECT COUNT(*) FROM stock_movements WHERE referenceType = :refType AND referenceId = :refId")
    suspend fun countMovementsForReference(refType: String, refId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovement(movement: StockMovement): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovements(movements: List<StockMovement>)
}

