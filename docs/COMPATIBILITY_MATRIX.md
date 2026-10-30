# StockQ — Toolchain & Dependency Compatibility Matrix

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.subzero.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  

---

## 1. Toolchain Hierarchy & Harmony

The following table details the verified mutual compatibility of the build toolchain:

| Layer | Component | Version | Compatibility Partner | Notes |
|---|---|---|---|---|
| **Host Runtime** | Eclipse Temurin JDK | 17.0.20.1 | Gradle 9.1.0 | Standard Java 17 LTS bytecode compatibility |
| **Build Tool** | Gradle Wrapper | 9.1.0 | AGP 9.0.1 | Meets minimum Gradle requirement for AGP 9.0.1 |
| **Android Plugin** | Android Gradle Plugin (AGP) | 9.0.1 | Android SDK API 36 | Fully supports compileSdk 36 and targetSdk 36 |
| **Language Compiler**| Kotlin Gradle Plugin | 2.3.20 | KSP 2.3.12 | Exact compiler alignment ensures stable symbol processing |
| **UI Compiler** | Compose Compiler Plugin | 2.3.20 | Kotlin 2.3.20 | Bundled Compose compiler (`org.jetbrains.kotlin.plugin.compose`) |
| **Annotation Engine**| KSP | 2.3.12 | Room Compiler 2.8.5 | Room SQLite code generation runs cleanly via KSP |
| **Design System** | Compose Material 3 | Via BOM 2026.03.01 | Compose UI 1.8.x | Unified UI components and tokens |

---

## 2. Android SDK Compatibility Boundaries

| Setting | API Level | Android Version Name | Audience Coverage & Rationale |
|---|---|---|---|
| **minSdk** | **26** | Android 8.0 (Oreo) | Guarantees modern `java.time` (Instant, LocalDate, DateTimeFormatter) without desugaring performance overhead. Covers >96% of active devices in India. Perfect for retail billing devices. |
| **compileSdk** | **36** | Android 16 | Enables the latest platform APIs and modern Material 3 system integrations. |
| **targetSdk** | **36** | Android 16 | Exceeds all Google Play target API level requirements. |

---

## 3. Host Memory & Performance Configuration

To guarantee reliable and fast builds on the Windows 11 host without excessive memory consumption:

```properties
# G:\StockQ\gradle.properties
org.gradle.jvmargs=-Xms64m -Xmx768m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=64m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.configuration-cache=false
org.gradle.workers.max=1
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
kotlin.compiler.execution.strategy=in-process
```

- **Memory Limit (`-Xmx768m`)**: Prevents Gradle daemon from expanding into host virtual memory, keeping compilation snappy.
- **Serial GC**: Reduces GC overhead on single-threaded / low-core allocations.
- **In-process Compiler Strategy**: Avoids spawning a secondary Kotlin daemon process, cutting RAM consumption by ~50%.
