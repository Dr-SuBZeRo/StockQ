package com.stockq.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stockq.core.database.converter.Converters
import com.stockq.core.database.dao.BatchDao
import com.stockq.core.database.dao.BusinessDocumentDao
import com.stockq.core.database.dao.BusinessExpenseDao
import com.stockq.core.database.dao.BusinessProfileDao
import com.stockq.core.database.dao.CustomerDao
import com.stockq.core.database.dao.CustomerPaymentDao
import com.stockq.core.database.dao.EmployeeDao
import com.stockq.core.database.dao.ProductCategoryDao
import com.stockq.core.database.dao.ProductDao
import com.stockq.core.database.dao.PurchaseDao
import com.stockq.core.database.dao.PurchaseItemDao
import com.stockq.core.database.dao.PurchasePaymentDao
import com.stockq.core.database.dao.QuotationDao
import com.stockq.core.database.dao.SaleDao
import com.stockq.core.database.dao.ServiceJobDao
import com.stockq.core.database.dao.StockMovementDao
import com.stockq.core.database.dao.SupplierDao
import com.stockq.core.database.dao.SupplierQuotationDao
import com.stockq.core.database.entity.AttendanceRecord
import com.stockq.core.database.entity.Batch
import com.stockq.core.database.entity.BusinessDocument
import com.stockq.core.database.entity.BusinessExpense
import com.stockq.core.database.entity.BusinessProfile
import com.stockq.core.database.entity.Customer
import com.stockq.core.database.entity.CustomerPayment
import com.stockq.core.database.entity.Employee
import com.stockq.core.database.entity.EmployeeAdvance
import com.stockq.core.database.entity.Product
import com.stockq.core.database.entity.ProductCategory
import com.stockq.core.database.entity.Purchase
import com.stockq.core.database.entity.PurchaseItem
import com.stockq.core.database.entity.PurchasePayment
import com.stockq.core.database.entity.Quotation
import com.stockq.core.database.entity.QuotationItem
import com.stockq.core.database.entity.Sale
import com.stockq.core.database.entity.SaleItem
import com.stockq.core.database.entity.ServiceJob
import com.stockq.core.database.entity.StockMovement
import com.stockq.core.database.entity.Supplier
import com.stockq.core.database.entity.SupplierQuotation
import com.stockq.core.database.entity.WagePayment
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
        PurchasePayment::class,
        StockMovement::class,
        Batch::class,
        SupplierQuotation::class,
        Customer::class,
        CustomerPayment::class,
        Sale::class,
        SaleItem::class,
        Quotation::class,
        QuotationItem::class,
        ServiceJob::class,
        BusinessExpense::class,
        Employee::class,
        AttendanceRecord::class,
        EmployeeAdvance::class,
        WagePayment::class,
        BusinessDocument::class
    ],
    version = 2,
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
    abstract fun purchasePaymentDao(): PurchasePaymentDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun batchDao(): BatchDao
    abstract fun supplierQuotationDao(): SupplierQuotationDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerPaymentDao(): CustomerPaymentDao
    abstract fun saleDao(): SaleDao
    abstract fun quotationDao(): QuotationDao
    abstract fun serviceJobDao(): ServiceJobDao
    abstract fun businessExpenseDao(): BusinessExpenseDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun businessDocumentDao(): BusinessDocumentDao

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
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default business profile and standard initial categories
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.businessProfileDao().insertOrUpdate(
                                    BusinessProfile(
                                        id = 1L,
                                        storeName = "My Business",
                                        ownerName = "SuBZeRo",
                                        currencyCode = "INR",
                                        currencySymbol = "₹"
                                    )
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "General", description = "General goods and merchandise")
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "Provisions & Groceries", description = "Daily retail goods")
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "Hardware & Electrical", description = "Parts, tools and components")
                                )
                                database.productCategoryDao().insertCategory(
                                    ProductCategory(name = "Services & Repairs", description = "Labour, servicing and maintenance")
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
