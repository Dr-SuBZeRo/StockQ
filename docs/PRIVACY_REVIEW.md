# StockQ — Privacy Review

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  

---

## 1. Permission Audit

The application manifest (`AndroidManifest.xml`) does not declare:
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.READ_PHONE_STATE`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.CAMERA` (unless user explicitly requests barcode scanning in a later phase)
- `android.permission.RECORD_AUDIO`

StockQ operates in total network isolation. Even if a rogue library were somehow injected, the Android Linux sandbox prevents any TCP/UDP socket creation at the kernel level without the `INTERNET` permission.

---

## 2. On-Device Storage Layout

All persistent application data resides in internal private app storage:
- `/data/data/com.stockq/databases/stockq.db`: Main Room SQLite database.
- `/data/data/com.stockq/shared_prefs/stockq_preferences.preferences_pb`: DataStore preferences (e.g. currency symbol, store profile, language selection).

---

## 3. Data Export & Import Governance

1. Data export is triggered solely upon explicit user action within the "Backup & Restore" screen.
2. The user selects a target destination using Android's system document picker (`ACTION_CREATE_DOCUMENT`).
3. The app writes the database export directly to the user-selected Uri.
4. Data import reads from a user-selected Uri via `ACTION_OPEN_DOCUMENT` and verifies schema integrity before restoration.
