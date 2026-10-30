# StockQ — Auditable Inventory Engine Specification

**Product**: StockQ  
**Application ID**: `com.stockq`  
**Developer**: SuBZeRo  
**Date**: October 30, 2026  

---

## 1. Inventory Integrity Principles

1. **Immutable Movement Records**:
   - Every operation that changes a product's available stock creates a permanent row in `stock_movements`.
   - Modifying historical ledger rows is disallowed. Corrections are executed by recording an offsetting movement (e.g. `PHYSICAL_ADJUSTMENT` or `SUPPLIER_RETURN`).
2. **Negative-Stock Enforcement**:
   - Outward stock movements (`SALE_ISSUE`, `DAMAGED_WASTAGE`, `SUPPLIER_RETURN`) verify available quantity before execution. If `requested > currentStock`, the operation fails with an `IllegalStateException`.
3. **Atomic Balance Updates**:
   - The ledger insertion and product table balance synchronization execute within a single Room `@Transaction`.
4. **Audit Trail Reconcilability**:
   - At any point in time, the sum of all `quantityChange` values in `stock_movements` for a product equals `Product.currentStock`.
   - `StockLedgerRepository.reconcileStock(productId)` programmatically validates this invariance.

---

## 2. Stock Movement Taxonomy

| Movement Type | Code | Quantity Delta | Cost Basis | Trigger Mechanism |
|---|---|---|---|---|
| **Opening Stock** | `OPENING_STOCK` | `+` (Positive) | Initial cost price | Catalog onboarding |
| **Purchase Receipt** | `PURCHASE_RECEIPT` | `+` (Positive) | Effective unit cost | Purchase confirmation |
| **Sale Issue** | `SALE_ISSUE` | `-` (Negative) | Product cost price | Counter sale / outward issue |
| **Physical Adjustment** | `PHYSICAL_ADJUSTMENT` | `+/-` (Delta) | Product cost price | Stocktake audit |
| **Damaged / Wastage** | `DAMAGED_WASTAGE` | `-` (Negative) | Product cost price | Expiry, transit damage, spoilage |
| **Customer Return** | `CUSTOMER_RETURN` | `+` (Positive) | Product cost price | Returned merchandise |
| **Supplier Return** | `SUPPLIER_RETURN` | `-` (Negative) | Purchase effective cost | Purchase cancellation / return |

---

## 3. Low-Stock & Reorder Logic

- **Low-Stock Trigger**: `currentStock <= minStockThreshold`
- **Suggested Reorder Quantity**: `idealReorderQuantity` (customizable per SKU, default 20 units).
- **Valuation Formula**:
  $$\text{Inventory Valuation} = \sum_{\text{active products}} \left(\text{currentStock} \times \text{costPricePaise}\right)$$

