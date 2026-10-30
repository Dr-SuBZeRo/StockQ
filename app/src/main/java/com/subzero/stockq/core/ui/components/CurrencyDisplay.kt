package com.subzero.stockq.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a monetary amount into the Indian Rupee numbering format (Lakhs & Crores).
 * e.g. 150000 -> ₹ 1,50,000.00
 */
fun formatIndianRupees(amount: Double): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
    val symbols = DecimalFormatSymbols(locale).apply {
        currencySymbol = "₹"
    }
    val formatter = DecimalFormat("₹ #,##,##0.00", symbols)
    return formatter.format(amount)
}

@Composable
fun CurrencyDisplay(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = formatIndianRupees(amount),
        style = style,
        color = color,
        modifier = modifier
    )
}
