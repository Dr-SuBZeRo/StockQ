package com.stockq.core.di

import android.content.Context
import com.stockq.core.database.StockQDatabase
import com.stockq.data.repository.BusinessProfileRepository
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.PurchaseRepository
import com.stockq.data.repository.StockLedgerRepository
import com.stockq.data.repository.SupplierRepository

interface AppContainer {
    val database: StockQDatabase
    val stockLedgerRepository: StockLedgerRepository
    val productRepository: ProductRepository
    val purchaseRepository: PurchaseRepository
    val supplierRepository: SupplierRepository
    val businessProfileRepository: BusinessProfileRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: StockQDatabase by lazy {
        StockQDatabase.getInstance(context)
    }

    override val stockLedgerRepository: StockLedgerRepository by lazy {
        StockLedgerRepository(database)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(database, stockLedgerRepository)
    }

    override val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepository(database)
    }

    override val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(database)
    }

    override val businessProfileRepository: BusinessProfileRepository by lazy {
        BusinessProfileRepository(database)
    }
}

