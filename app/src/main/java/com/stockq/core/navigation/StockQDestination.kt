package com.stockq.core.navigation

/**
 * Type-safe navigation destinations for StockQ.
 */
enum class StockQDestination(val route: String, val label: String) {
    WELCOME("welcome", "Welcome"),
    DASHBOARD("dashboard", "Dashboard"),
    INVENTORY("inventory", "Inventory"),
    ADD_EDIT_PRODUCT("add_edit_product", "Add / Edit Product"),
    PRODUCT_DETAILS("product_details", "Product Details"),
    STOCK_MOVEMENT("stock_movement", "Stock Movement"),
    PURCHASES("purchases", "Purchases"),
    SUPPLIERS("suppliers", "Suppliers"),
    SUPPLIER_COMPARISON("supplier_comparison", "Supplier Comparison"),
    REORDER("reorder", "Reorder List"),
    SALES("sales", "Sales & POS"),
    CUSTOMERS("customers", "Customers & Dues"),
    QUOTATIONS("quotations", "Quotations"),
    SERVICE_JOBS("service_jobs", "Service & Repairs"),
    EXPENSES("expenses", "Expenses"),
    BATCHES("batches", "Batches & Expiry"),
    EMPLOYEES("employees", "Staff & Wages"),
    REPORTS("reports", "Profit & Reports"),
    DOCUMENTS("documents", "Compliance & Licenses"),
    SETTINGS("settings", "Settings"),
    BACKUP_RESTORE("backup_restore", "Backup & Restore");

    companion object {
        fun fromRoute(route: String?): StockQDestination =
            entries.find { it.route == route } ?: DASHBOARD
    }
}
