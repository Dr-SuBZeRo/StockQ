package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

data class SupplierProductQuote(
    val purchaseItemId: Long,
    val purchaseId: Long,
    val supplierId: Long,
    val supplierName: String,
    val productId: Long,
    val productName: String,
    val productUnit: String,
    val unitCostPaise: Long,
    val discountPercent: Double,
    val taxRatePercent: Double,
    val transportAllocationPaise: Long,
    val effectiveUnitCostPaise: Long,
    val purchaseDate: Long
)

@Dao
interface PurchaseItemDao {
    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchaseDirect(purchaseId: Long): List<PurchaseItem>

    @Query("SELECT * FROM purchase_items WHERE productId = :productId")
    fun getItemsForProduct(productId: Long): Flow<List<PurchaseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PurchaseItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItem>)

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsForPurchase(purchaseId: Long)

    @Delete
    suspend fun deleteItem(item: PurchaseItem)

    @Query("""
        SELECT 
            pi.id AS purchaseItemId,
            pi.purchaseId AS purchaseId,
            p.supplierId AS supplierId,
            s.name AS supplierName,
            pi.productId AS productId,
            pr.name AS productName,
            pr.unit AS productUnit,
            pi.unitCostPaise AS unitCostPaise,
            pi.discountPercent AS discountPercent,
            pi.taxRatePercent AS taxRatePercent,
            pi.transportAllocationPaise AS transportAllocationPaise,
            pi.effectiveUnitCostPaise AS effectiveUnitCostPaise,
            p.purchaseDate AS purchaseDate
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        INNER JOIN suppliers s ON p.supplierId = s.id
        INNER JOIN products pr ON pi.productId = pr.id
        WHERE pi.productId = :productId AND p.status = 'CONFIRMED'
        ORDER BY pi.effectiveUnitCostPaise ASC, p.purchaseDate DESC
    """)
    fun getSupplierQuotesForProduct(productId: Long): Flow<List<SupplierProductQuote>>
}

