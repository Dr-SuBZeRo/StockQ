# StockQ — Latest Stable Technology & Version Research Report

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  
**Auditor**: Senior Android Architect & DevOps Specialist  

---

## 1. Executive Summary

This report documents the latest stable releases of all development tools, SDKs, compilers, build systems, and libraries for StockQ. All versions were queried and verified directly from official vendor feeds (Google Maven, Maven Central, Gradle Services, and official Android documentation).

---

## 2. Toolchain & Runtime Components

| Component | Latest Stable Release | StockQ Selected Release | Selection Justification |
|---|---|---|---|
| **JDK** | Temurin 17.0.20.1 / Corretto 21.0.5 | **Eclipse Temurin 17.0.20.1** | AGP 9.0.x / 9.5.x and Gradle 9.x natively compile and execute under JDK 17 with maximum stability and zero bytecode warnings. |
| **Android SDK Platform** | API 36 (Android 16) | **android-36** | Latest stable platform provided by Google SDK Manager; targetSdk 36 satisfies all current and upcoming Google Play requirements. |
| **Android SDK Build Tools** | 36.0.0 | **36.0.0** | Matches Android API 36 platform requirements. |
| **Android SDK Platform Tools** | 37.0.1 (ADB 1.0.41) | **37.0.1** | Official Google platform tools installed. |
| **Android SDK Cmdline Tools** | 23.0.0 (`cmdline-tools/latest`) | **23.0.0** | Available via Google repository; fully orchestrated by Android CLI. |
| **Android Studio** | Narwhal 2025.1.x / Ladybug 2024.2.x | **Optional / Omitted** | C: drive has only 14.99 GB free. Antigravity IDE 1.107.0 with Android CLI sidecars provides complete standalone development without risk of disk exhaustion. |
| **Gradle** | 9.8.2 (current), 9.1.0 (LTS baseline) | **9.1.0** | Recommended and verified build automation tool for AGP 9.0.1. Already locally cached on system. |
| **Android Gradle Plugin** | 9.5.0 / 9.0.1 | **9.0.1** | Stable, robust build plugin compatible with Gradle 9.1.0 and Kotlin 2.3.20. |
| **Kotlin** | 2.4.21 / 2.3.20 | **2.3.20** | Latest verified stable release with fully harmonized KSP 2.3.12 and Compose compiler plugin alignment. |
| **KSP (Kotlin Symbol Processing)**| 2.3.12 | **2.3.12** | Latest stable KSP release on Maven Central designed for Kotlin 2.3.x symbol processing. |
| **Compose Compiler Plugin** | Bundled in Kotlin 2.3.x (`org.jetbrains.kotlin.plugin.compose`) | **2.3.20** | Jetpack Compose compiler is now an official first-party Kotlin plugin matching the Kotlin compiler version directly. |

---

## 3. Core AndroidX & Architecture Libraries

| Library Module | Maven Coordinate | Latest Verified Stable | StockQ Selected |
|---|---|---|---|
| **Compose BOM** | `androidx.compose:compose-bom` | `2026.09.00` / `2026.03.01` | **2026.03.01** |
| **Compose Material 3** | `androidx.compose.material3:material3` | Managed by BOM | **BOM Managed** |
| **Compose UI** | `androidx.compose.ui:ui` | Managed by BOM | **BOM Managed** |
| **Compose Tooling Preview** | `androidx.compose.ui:ui-tooling-preview` | Managed by BOM | **BOM Managed** |
| **Material Icons Extended**| `androidx.compose.material:material-icons-extended` | `1.7.8` | **1.7.8** |
| **Activity Compose** | `androidx.activity:activity-compose` | `1.13.0` | **1.13.0** |
| **Core KTX** | `androidx.core:core-ktx` | `1.18.0` | **1.18.0** |
| **Core Splashscreen** | `androidx.core:core-splashscreen` | `1.0.1` | **1.0.1** |
| **Lifecycle Runtime KTX**| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.10.0` | **2.10.0** |
| **Lifecycle ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.10.0` | **2.10.0** |
| **Lifecycle Runtime Compose** | `androidx.lifecycle:lifecycle-runtime-compose` | `2.10.0` | **2.10.0** |
| **Navigation 3 / Compose** | `androidx.navigation3:navigation3-runtime` | `1.0.1` | **1.0.1** |
| **Navigation 3 UI** | `androidx.navigation3:navigation3-ui` | `1.0.1` | **1.0.1** |
| **Room Runtime** | `androidx.room:room-runtime` | `2.8.5` | **2.8.5** |
| **Room KTX** | `androidx.room:room-ktx` | `2.8.5` | **2.8.5** |
| **Room Compiler** | `androidx.room:room-compiler` | `2.8.5` | **2.8.5** (via KSP) |
| **DataStore Preferences** | `androidx.datastore:datastore-preferences` | `1.1.1` | **1.1.1** |
| **Coroutines Core** | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `1.10.2` | **1.10.2** |
| **Coroutines Android** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.10.2` | **1.10.2** |
| **Coroutines Test** | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `1.10.2` | **1.10.2** |
| **JUnit** | `junit:junit` | `4.13.2` | **4.13.2** |
| **AndroidX Test Core** | `androidx.test:core` | `1.7.0` | **1.7.0** |
| **AndroidX Test Ext JUnit** | `androidx.test.ext:junit` | `1.3.0` | **1.3.0** |
| **AndroidX Espresso Core** | `androidx.test.espresso:espresso-core` | `3.7.0` | **3.7.0** |

---

## 4. Omitted & Restricted Dependencies (Offline-First Policy)

In strict adherence to the offline-first privacy requirements for StockQ:
- **No Cloud Services**: Firebase, AWS Amplify, Supabase, Google Cloud SDKs are excluded.
- **No Networking / HTTP Clients**: Retrofit, OkHttp, Ktor-client, Volley are excluded.
- **No Analytics / Telemetry**: Firebase Analytics, Mixpanel, Segment, Flurry are excluded.
- **No Advertising SDKs**: Google AdMob, Unity Ads, AppLovin are excluded.
- **No Third-Party Authentication**: Firebase Auth, OAuth2 social login SDKs are excluded.
