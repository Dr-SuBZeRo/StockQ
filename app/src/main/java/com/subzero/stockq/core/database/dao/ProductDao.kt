package com.subzero.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subzero.stockq.core.database.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductDirect(id: Long): Product?

    @Query("SELECT * FROM products WHERE isArchived = 0 AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isArchived = 0 AND categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isArchived = 0 AND currentStock <= minStockThreshold ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0")
    fun getActiveProductCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0 AND currentStock <= minStockThreshold")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(CAST(currentStock * costPricePaise AS INTEGER)), 0) FROM products WHERE isArchived = 0 AND currentStock > 0")
    fun getTotalInventoryValuationPaise(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :productId")
    suspend fun setArchived(productId: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: Product)
}

