package com.subzero.stockq.data.repository

import androidx.room.withTransaction
import com.subzero.stockq.core.database.StockQDatabase
import com.subzero.stockq.core.database.entity.Product
import com.subzero.stockq.core.database.entity.ProductCategory
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val database: StockQDatabase,
    private val stockLedgerRepository: StockLedgerRepository
) {
    private val productDao = database.productDao()
    private val categoryDao = database.productCategoryDao()

    fun getActiveProducts(): Flow<List<Product>> = productDao.getActiveProducts()

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    fun getProductById(id: Long): Flow<Product?> = productDao.getProductById(id)

    suspend fun getProductDirect(id: Long): Product? = productDao.getProductDirect(id)

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = productDao.getProductsByCategory(categoryId)

    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()

    fun getActiveProductCount(): Flow<Int> = productDao.getActiveProductCount()

    fun getLowStockCount(): Flow<Int> = productDao.getLowStockCount()

    fun getTotalInventoryValuationPaise(): Flow<Long> = productDao.getTotalInventoryValuationPaise()

    fun getAllCategories(): Flow<List<ProductCategory>> = categoryDao.getAllCategories()

    suspend fun createCategory(name: String, description: String = "", defaultTaxRatePercent: Double = 0.0): Long {
        return categoryDao.insertCategory(
            ProductCategory(
                name = name.trim(),
                description = description.trim(),
                defaultTaxRatePercent = defaultTaxRatePercent
            )
        )
    }

    /**
     * Atomically creates a product and writes an opening stock ledger entry if initial stock > 0.
     */
    suspend fun createProduct(
        name: String,
        sku: String,
        barcode: String,
        unit: String,
        costPricePaise: Long,
        sellingPricePaise: Long,
        initialStock: Double,
        minStockThreshold: Double,
        idealReorderQuantity: Double,
        categoryId: Long?
    ): Result<Long> = database.withTransaction {
        if (name.isBlank()) {
            return@withTransaction Result.failure(IllegalArgumentException("Product name cannot be blank"))
        }
        if (costPricePaise < 0L || sellingPricePaise < 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Prices cannot be negative"))
        }

        val product = Product(
            categoryId = categoryId,
            name = name.trim(),
            sku = sku.trim(),
            barcode = barcode.trim(),
            unit = unit.trim().ifBlank { "pcs" },
            costPricePaise = costPricePaise,
            sellingPricePaise = sellingPricePaise,
            currentStock = 0.0, // Initialized to 0, updated by ledger below
            minStockThreshold = minStockThreshold,
            idealReorderQuantity = idealReorderQuantity
        )

        val productId = productDao.insertProduct(product)

        if (initialStock > 0.0) {
            stockLedgerRepository.recordOpeningStock(
                productId = productId,
                initialStock = initialStock,
                costPricePaise = costPricePaise
            )
        }

        Result.success(productId)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        if (product.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name cannot be blank"))
        }
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    suspend fun setArchived(productId: Long, isArchived: Boolean) {
        productDao.setArchived(productId, isArchived)
    }
}

