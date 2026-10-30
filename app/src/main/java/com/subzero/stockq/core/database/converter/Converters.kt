package com.subzero.stockq.core.database.converter

import androidx.room.TypeConverter
import com.subzero.stockq.core.database.entity.PaymentStatus
import com.subzero.stockq.core.database.entity.PurchaseStatus
import com.subzero.stockq.core.database.entity.StockMovementType

class Converters {
    @TypeConverter
    fun fromPurchaseStatus(value: PurchaseStatus): String = value.name

    @TypeConverter
    fun toPurchaseStatus(value: String): PurchaseStatus =
        try { PurchaseStatus.valueOf(value) } catch (e: Exception) { PurchaseStatus.DRAFT }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus =
        try { PaymentStatus.valueOf(value) } catch (e: Exception) { PaymentStatus.PENDING }

    @TypeConverter
    fun fromStockMovementType(value: StockMovementType): String = value.name

    @TypeConverter
    fun toStockMovementType(value: String): StockMovementType =
        try { StockMovementType.valueOf(value) } catch (e: Exception) { StockMovementType.PHYSICAL_ADJUSTMENT }
}

