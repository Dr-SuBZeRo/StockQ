package com.stockq.core.database.converter

import androidx.room.TypeConverter
import com.stockq.core.database.entity.AttendanceStatus
import com.stockq.core.database.entity.DocumentType
import com.stockq.core.database.entity.JobStatus
import com.stockq.core.database.entity.PaymentStatus
import com.stockq.core.database.entity.PurchaseStatus
import com.stockq.core.database.entity.QuotationStatus
import com.stockq.core.database.entity.SaleStatus
import com.stockq.core.database.entity.StockMovementType
import com.stockq.core.database.entity.WageType

class Converters {

    @TypeConverter
    fun fromStockMovementType(value: StockMovementType): String = value.name

    @TypeConverter
    fun toStockMovementType(value: String): StockMovementType =
        try { StockMovementType.valueOf(value) } catch (e: Exception) { StockMovementType.OPENING_STOCK }

    @TypeConverter
    fun fromPurchaseStatus(value: PurchaseStatus): String = value.name

    @TypeConverter
    fun toPurchaseStatus(value: String): PurchaseStatus =
        try { PurchaseStatus.valueOf(value) } catch (e: Exception) { PurchaseStatus.DRAFT }

    @TypeConverter
    fun fromSaleStatus(value: SaleStatus): String = value.name

    @TypeConverter
    fun toSaleStatus(value: String): SaleStatus =
        try { SaleStatus.valueOf(value) } catch (e: Exception) { SaleStatus.COMPLETED }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus =
        try { PaymentStatus.valueOf(value) } catch (e: Exception) { PaymentStatus.PENDING }

    @TypeConverter
    fun fromQuotationStatus(value: QuotationStatus): String = value.name

    @TypeConverter
    fun toQuotationStatus(value: String): QuotationStatus =
        try { QuotationStatus.valueOf(value) } catch (e: Exception) { QuotationStatus.DRAFT }

    @TypeConverter
    fun fromJobStatus(value: JobStatus): String = value.name

    @TypeConverter
    fun toJobStatus(value: String): JobStatus =
        try { JobStatus.valueOf(value) } catch (e: Exception) { JobStatus.RECEIVED }

    @TypeConverter
    fun fromWageType(value: WageType): String = value.name

    @TypeConverter
    fun toWageType(value: String): WageType =
        try { WageType.valueOf(value) } catch (e: Exception) { WageType.DAILY }

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus =
        try { AttendanceStatus.valueOf(value) } catch (e: Exception) { AttendanceStatus.PRESENT }

    @TypeConverter
    fun fromDocumentType(value: DocumentType): String = value.name

    @TypeConverter
    fun toDocumentType(value: String): DocumentType =
        try { DocumentType.valueOf(value) } catch (e: Exception) { DocumentType.LICENSE }
}
