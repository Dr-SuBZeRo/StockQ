package com.stockq.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stockq.core.database.converter.Converters
import com.stockq.core.database.dao.BusinessProfileDao
import com.stockq.core.database.dao.ProductCategoryDao
import com.stockq.core.database.dao.ProductDao
import com.stockq.core.database.dao.PurchaseDao
import com.stockq.core.database.dao.PurchaseItemDao
import com.stockq.core.database.dao.StockMovementDao
import com.stockq.core.database.dao.SupplierDao
import com.stockq.core.database.entity.BusinessProfile
import com.stockq.core.database.entity.Product
import com.stockq.core.database.entity.ProductCategory
import com.stockq.core.database.entity.Purchase
import com.stockq.core.database.entity.PurchaseItem
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.Supplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessProfile::class,
        ProductCategory::class,
        Product::class,
        Supplier::class,
        Purchase::class,
        PurchaseItem::class,
        StockMovement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockQDatabase : RoomDatabase() {

    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun productCategoryDao(): ProductCategoryDao
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        const val DATABASE_NAME = "stockq.db"

        @Volatile
        private var INSTANCE: StockQDatabase? = null

        fun getInstance(context: Context): StockQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockQDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default business profile and standard initial categories
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.businessProfileDao().insertOrUpdate(
                                    BusinessProfile(
                                        id = 1L,
                                        storeName = "My Retail Store",
                                        ownerName = "SuBZeRo",
                                        currencyCode = "INR",
                                        currencySymbol = "₹"
                                    )
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "General", description = "General goods and merchandise")
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "Groceries & Provisions", description = "Daily provisions and packaged food")
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "Electrical & Hardware", description = "Tools, wires, and fixtures")
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

