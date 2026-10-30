# StockQ — Dependency Inventory & License Review

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.subzero.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  

---

## 1. Centralized Version Catalog (`gradle/libs.versions.toml`)

All dependencies in StockQ are declared strictly via the version catalog. No hardcoded or dynamic version strings are permitted in module build files.

| Dependency Module | Coordinate | Version | License | Justification |
|---|---|---|---|---|
| **Android Gradle Plugin** | `com.android.tools.build:gradle` | 9.0.1 | Apache 2.0 | Official build tool for Android applications |
| **Kotlin Gradle Plugin** | `org.jetbrains.kotlin:kotlin-gradle-plugin` | 2.3.20 | Apache 2.0 | Kotlin 2.3 language support |
| **Compose Compiler Plugin**| `org.jetbrains.kotlin.plugin.compose` | 2.3.20 | Apache 2.0 | Official Kotlin compiler plugin for Jetpack Compose |
| **KSP Plugin** | `com.google.devtools.ksp` | 2.3.12 | Apache 2.0 | Kotlin Symbol Processing for fast code generation |
| **Compose BOM** | `androidx.compose:compose-bom` | 2026.03.01 | Apache 2.0 | Harmonizes all Compose UI and runtime versions |
| **Compose Material 3** | `androidx.compose.material3:material3` | BOM | Apache 2.0 | Modern Android Design System implementation |
| **Compose UI** | `androidx.compose.ui:ui` | BOM | Apache 2.0 | Core UI rendering toolkit |
| **Compose Tooling Preview** | `androidx.compose.ui:ui-tooling-preview` | BOM | Apache 2.0 | Layout preview support in Antigravity |
| **Compose Material Icons** | `androidx.compose.material:material-icons-extended` | 1.7.8 | Apache 2.0 | Icons for retail, inventory, and navigation |
| **Activity Compose** | `androidx.activity:activity-compose` | 1.13.0 | Apache 2.0 | Jetpack Compose integration into Android Activities |
| **Core KTX** | `androidx.core:core-ktx` | 1.18.0 | Apache 2.0 | Kotlin extensions for Android framework APIs |
| **Core Splashscreen** | `androidx.core:core-splashscreen` | 1.0.1 | Apache 2.0 | Modern splash screen presentation |
| **Lifecycle Runtime KTX**| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.10.0 | Apache 2.0 | Coroutine lifecycle scopes |
| **Lifecycle ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.10.0 | Apache 2.0 | ViewModel bindings for Compose |
| **Lifecycle Runtime Compose**| `androidx.lifecycle:lifecycle-runtime-compose` | 2.10.0 | Apache 2.0 | `collectAsStateWithLifecycle()` Flow integration |
| **Navigation 3 Runtime** | `androidx.navigation3:navigation3-runtime` | 1.0.1 | Apache 2.0 | Type-safe declarative app navigation |
| **Navigation 3 UI** | `androidx.navigation3:navigation3-ui` | 1.0.1 | Apache 2.0 | Navigation UI bindings for Compose |
| **DataStore Preferences** | `androidx.datastore:datastore-preferences` | 1.1.1 | Apache 2.0 | Offline key-value store for app settings |
| **Room Runtime** | `androidx.room:room-runtime` | 2.8.5 | Apache 2.0 | SQLite Object Relational Mapping runtime |
| **Room KTX** | `androidx.room:room-ktx` | 2.8.5 | Apache 2.0 | Kotlin Coroutines & Flow support for Room |
| **Room Compiler** | `androidx.room:room-compiler` | 2.8.5 | Apache 2.0 | KSP annotation processor for SQLite queries |
| **Coroutines Core** | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.10.2 | Apache 2.0 | Asynchronous concurrency primitives |
| **Coroutines Android** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | Apache 2.0 | Android Main dispatcher support |
| **JUnit** | `junit:junit` | 4.13.2 | EPL 2.0 | JVM unit testing framework |
| **Coroutines Test** | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.10.2 | Apache 2.0 | Deterministic coroutine testing |
| **AndroidX Test Core** | `androidx.test:core` | 1.7.0 | Apache 2.0 | Core instrumentation test harness |
| **AndroidX Test Ext JUnit**| `androidx.test.ext:junit` | 1.3.0 | Apache 2.0 | JUnit4 extension for Android |
| **AndroidX Espresso Core**| `androidx.test.espresso:espresso-core` | 3.7.0 | Apache 2.0 | UI testing harness |

---

## 2. Dependency Hygiene Rules

1. **No Transitive Network Egress**: None of the selected libraries initiate network sockets or HTTP connections.
2. **Open Source Permissive Licenses**: All libraries are licensed under Apache 2.0 or EPL 2.0, allowing commercial and private deployment without restrictive obligations.
3. **No Dynamic Versions**: No `+` or range operators are used in any build scripts.
