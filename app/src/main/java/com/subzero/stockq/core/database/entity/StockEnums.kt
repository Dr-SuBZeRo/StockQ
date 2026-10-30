package com.subzero.stockq.core.database.entity

enum class PurchaseStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PARTIAL,
    PAID
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

