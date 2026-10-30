package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.ProductCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductCategoryDao {
    @Query("SELECT * FROM product_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ProductCategory>>

    @Query("SELECT * FROM product_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): ProductCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ProductCategory): Long

    @Update
    suspend fun updateCategory(category: ProductCategory)

    @Delete
    suspend fun deleteCategory(category: ProductCategory)
}

