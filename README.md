# StockQ — Offline-First Indian Retail & Inventory Management

**Developer**: SuBZeRo  
**Application ID**: `com.subzero.stockq`  
**Platform**: Native Android (Kotlin, Jetpack Compose, Material 3, Room SQLite)  
**Target Market**: Indian Retail, Wholesalers & Shopkeepers  
**Default Currency**: INR (₹)  
**Privacy Status**: 100% Offline-First • Zero Cloud Sync • Zero Telemetry • Zero Ads  

---

## Architecture & Technology Stack

- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Language**: Kotlin 2.3.20 (Compose Compiler Plugin bundled)
- **Database**: AndroidX Room 2.8.5 with SQLite & KSP (Kotlin Symbol Processing 2.3.12)
- **Build System**: Android Gradle Plugin 9.0.1 with Gradle 9.1.0
- **Compile & Target SDK**: Android SDK 36 (Android 16)
- **Minimum SDK**: Android API 26 (Android 8.0 Oreo)
- **JDK Runtime**: Eclipse Temurin OpenJDK 17.0.20.1

---

## Screens & Functional Modules

1. **Welcome / Setup**: Initial shop branding and onboarding.
2. **Dashboard**: Financial overview, total inventory valuation (₹), low-stock warnings, navigation hub.
3. **Product Inventory**: Searchable catalog with stock badges and category filters.
4. **Add / Edit Product**: SKU/barcode, cost price, selling price, reorder thresholds.
5. **Product Details**: Historical stock logs, unit margin calculations, supplier mappings.
6. **Stock Movement**: Inward, Sales, Wastage, and Customer Returns.
7. **Purchase Entry**: Inward stock recording with supplier invoices and cost tracking.
8. **Supplier List**: Vendor directory, lead times, and payment terms.
9. **Supplier Comparison**: Side-by-side wholesale price comparison matrix.
10. **Reorder List**: Real-time automated purchase recommendation list for stock below threshold.
11. **Settings**: Indian Rupee currency formatting, Tamil & English language toggles.
12. **Backup & Restore**: User-controlled snapshot export and restoration via Android Storage Access Framework.

---

## Build & Test Instructions

### Clean Build
```powershell
.\gradlew.bat clean
```

### Run Unit Tests
```powershell
.\gradlew.bat testDebugUnitTest
```

### Assemble Debug APK
```powershell
.\gradlew.bat assembleDebug
```
Output artifact: `app/build/outputs/apk/debug/app-debug.apk`
