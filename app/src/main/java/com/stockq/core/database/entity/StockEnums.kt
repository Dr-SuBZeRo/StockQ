package com.stockq.core.database.entity

enum class PurchaseStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED
}

enum class SaleStatus {
    COMPLETED,
    CANCELLED,
    RETURNED
}

enum class PaymentStatus {
    PENDING,
    PARTIAL,
    PAID
}

enum class QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    CONVERTED_TO_SALE
}

enum class JobStatus {
    RECEIVED,
    IN_PROGRESS,
    WAITING_PARTS,
    COMPLETED,
    DELIVERED,
    CANCELLED
}

enum class WageType {
    DAILY,
    MONTHLY,
    HOURLY
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    HALF_DAY,
    PAID_LEAVE
}

enum class DocumentType {
    LICENSE,
    REGISTRATION,
    TAX_FILING,
    INSURANCE,
    CONTRACT,
    OTHER
}

enum class StockMovementType {
    OPENING_STOCK,
    PURCHASE_RECEIPT,
    SALE_ISSUE,
    PHYSICAL_ADJUSTMENT,
    DAMAGED_WASTAGE,
    CUSTOMER_RETURN,
    SUPPLIER_RETURN
}
