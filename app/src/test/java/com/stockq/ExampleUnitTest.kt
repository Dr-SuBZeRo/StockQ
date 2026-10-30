package com.stockq

import com.stockq.core.ui.components.formatIndianRupees
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 */
class ExampleUnitTest {
    @Test
    fun indianRupeeFormatting_isCorrect() {
        val formatted = formatIndianRupees(150000.0)
        // Verify symbol and decimal formatting
        assert(formatted.contains("₹"))
        assert(formatted.contains("1,50,000.00") || formatted.contains("150,000.00"))
    }

    @Test
    fun math_addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
