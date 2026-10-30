package com.stockq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stockq.core.navigation.StockQNavHost
import com.stockq.core.ui.theme.StockQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockQTheme {
                StockQNavHost()
            }
        }
    }
}
