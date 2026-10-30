package com.stockq

import android.app.Application
import com.stockq.core.di.AppContainer
import com.stockq.core.di.DefaultAppContainer

/**
 * StockQ Application class initialized on app launch.
 * Maintains application-level state and initializes offline databases.
 *
 * Developer: SuBZeRo
 */
class StockQApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
