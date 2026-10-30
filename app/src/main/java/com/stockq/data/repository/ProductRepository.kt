package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.Product
import com.stockq.core.database.entity.ProductCategory
import com.stockq.core.model.Quantity
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

    fun getOutOfStockProducts(): Flow<List<Product>> = productDao.getOutOfStockProducts()

    fun getActiveProductCount(): Flow<Int> = productDao.getActiveProductCount()

    fun getLowStockCount(): Flow<Int> = productDao.getLowStockCount()

    fun getOutOfStockCount(): Flow<Int> = productDao.getOutOfStockCount()

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
        categoryId: Long?,
        taxRatePercent: Double = 0.0,
        isTaxInclusive: Boolean = false,
        preferredSupplierId: Long? = null,
        description: String = ""
    ): Result<Long> = database.withTransaction {
        if (name.isBlank()) {
            return@withTransaction Result.failure(IllegalArgumentException("Product name cannot be blank"))
        }
        if (costPricePaise < 0L || sellingPricePaise < 0L) {
            return@withTransaction Result.failure(IllegalArgumentException("Prices cannot be negative"))
        }

        val trimmedSku = sku.trim()
        if (trimmedSku.isNotEmpty()) {
            val duplicateSku = productDao.findByDuplicateSku(trimmedSku)
            if (duplicateSku != null) {
                return@withTransaction Result.failure(IllegalArgumentException("SKU '$trimmedSku' is already assigned to '${duplicateSku.name}'"))
            }
        }

        val trimmedBarcode = barcode.trim()
        if (trimmedBarcode.isNotEmpty()) {
            val duplicateBarcode = productDao.findByDuplicateBarcode(trimmedBarcode)
            if (duplicateBarcode != null) {
                return@withTransaction Result.failure(IllegalArgumentException("Barcode '$trimmedBarcode' is already assigned to '${duplicateBarcode.name}'"))
            }
        }

        val product = Product(
            categoryId = categoryId,
            name = name.trim(),
            description = description.trim(),
            sku = trimmedSku,
            barcode = trimmedBarcode,
            unit = unit.trim().ifBlank { "pcs" },
            costPricePaise = costPricePaise,
            sellingPricePaise = sellingPricePaise,
            taxRatePercent = taxRatePercent,
            isTaxInclusive = isTaxInclusive,
            currentStockMilli = 0L, // Initialized to 0, updated by ledger below
            minStockThresholdMilli = Quantity.fromDouble(minStockThreshold),
            idealReorderQuantityMilli = Quantity.fromDouble(idealReorderQuantity),
            preferredSupplierId = preferredSupplierId
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
        if (product.sku.isNotBlank()) {
            val dup = productDao.findByDuplicateSku(product.sku.trim(), product.id)
            if (dup != null) {
                return Result.failure(IllegalArgumentException("SKU '${product.sku}' is already in use by '${dup.name}'"))
            }
        }
        if (product.barcode.isNotBlank()) {
            val dup = productDao.findByDuplicateBarcode(product.barcode.trim(), product.id)
            if (dup != null) {
                return Result.failure(IllegalArgumentException("Barcode '${product.barcode}' is already in use by '${dup.name}'"))
            }
        }
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    suspend fun setArchived(productId: Long, isArchived: Boolean) {
        productDao.setArchived(productId, isArchived)
    }
}
