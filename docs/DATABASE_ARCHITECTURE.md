# StockQ — Database Architecture & Schema Specification

**Product**: StockQ  
**Application ID**: `com.stockq`  
**Database Name**: `stockq.db`  
**Engine**: SQLite via AndroidX Room 2.8.5 with KSP  
**Date**: October 30, 2026  

---

## 1. Architectural Principles

1. **Exact Integer Monetary Representation**:
   - Every financial column (cost price, selling price, subtotal, discount, tax, transport charges, effective cost) is stored as **paise** (`Long`).
   - 1 Indian Rupee = 100 paise (`₹ 1.00 = 100L`).
   - Floating-point (`Float` or `Double`) is strictly forbidden for financial arithmetic to avoid IEEE-754 binary rounding discrepancies.
2. **Immutable Audit Ledger**:
   - Stock balances are never altered by loose updates. Every stock increment or decrement is recorded as an immutable `StockMovement` row.
3. **Transaction Safety**:
   - Business operations modifying multiple tables (e.g. Confirming a purchase and posting inventory movements) are executed inside `@Transaction` blocks.
4. **Relational Integrity**:
   - Foreign key constraints enforce referential integrity (`RESTRICT` on products referenced in stock movements or purchases, `CASCADE` on purchase line items, `SET_NULL` on category deletion).

---

## 2. Entity Relationship Diagram

```mermaid
erDiagram
    BUSINESS_PROFILE {
        int id PK
        string storeName
        string ownerName
        string phone
        string email
        string address
        string gstin
        string currencyCode
        string currencySymbol
        long updatedAt
    }

    PRODUCT_CATEGORY {
        int id PK
        string name
        string description
        double defaultTaxRatePercent
        long createdAt
    }

    PRODUCT {
        int id PK
        int categoryId FK
        string name
        string sku
        string barcode
        string unit
        long costPricePaise
        long sellingPricePaise
        double currentStock
        double minStockThreshold
        double idealReorderQuantity
        boolean isArchived
        long createdAt
        long updatedAt
    }

    SUPPLIER {
        int id PK
        string name
        string contactPerson
        string phone
        string email
        string address
        string gstin
        int paymentTermsDays
        float rating
        boolean isArchived
        long createdAt
    }

    PURCHASE {
        int id PK
        int supplierId FK
        string invoiceNumber
        long purchaseDate
        long subtotalPaise
        long discountPaise
        long taxPaise
        long transportChargesPaise
        long totalAmountPaise
        string status
        string paymentStatus
        long paidAmountPaise
        string notes
        long confirmedAt
        long createdAt
        long updatedAt
    }

    PURCHASE_ITEM {
        int id PK
        int purchaseId FK
        int productId FK
        double quantity
        long unitCostPaise
        double discountPercent
        double taxRatePercent
        long transportAllocationPaise
        long effectiveUnitCostPaise
        long totalItemCostPaise
    }

    STOCK_MOVEMENT {
        int id PK
        int productId FK
        string movementType
        double quantityChange
        double balanceAfter
        string referenceType
        long referenceId
        long unitCostPaise
        string reasonOrNotes
        long timestamp
    }

    PRODUCT_CATEGORY ||--o{ PRODUCT : categorizes
    SUPPLIER ||--o{ PURCHASE : supplies
    PURCHASE ||--|{ PURCHASE_ITEM : contains
    PRODUCT ||--o{ PURCHASE_ITEM : purchased_in
    PRODUCT ||--o{ STOCK_MOVEMENT : tracks_movement
```

---

## 3. Financial Calculation Standards

- **Item Total Cost**:
  $$\text{base} = \text{round}(\text{quantity} \times \text{unitCostPaise})$$
  $$\text{discount} = \text{round}(\text{base} \times \frac{\text{discountPercent}}{100})$$
  $$\text{tax} = \text{round}((\text{base} - \text{discount}) \times \frac{\text{taxRatePercent}}{100})$$
  $$\text{totalItemCostPaise} = \text{base} - \text{discount} + \text{tax} + \text{transportAllocationPaise}$$
- **Effective Unit Cost**:
  $$\text{effectiveUnitCostPaise} = \text{round}\left(\frac{\text{totalItemCostPaise}}{\text{quantity}}\right)$$

