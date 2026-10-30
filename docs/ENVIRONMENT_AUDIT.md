# StockQ — Environment Audit Report

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.stockq`  
**Workspace**: `G:\StockQ`  
**Audit Date**: October 30, 2026  
**Auditor**: Antigravity Assistant & Senior Android Systems Engineer  

---

## 1. Operating System and Host Architecture

| Parameter | Observed Value | Status |
|---|---|---|
| **OS Caption** | Microsoft Windows 11 Home Single Language | Verified |
| **OS Version** | 10.0.26200 | Verified |
| **Build Number** | 26200 | Verified |
| **Architecture** | 64-bit (x86_64) | Verified |
| **Processor** | Intel(R) Core(TM) i5-10300H CPU @ 2.50GHz (4 cores / 8 threads) | Verified |
| **Hardware Virtualization** | Enabled in Firmware (`VirtualizationFirmwareEnabled: True`) | Verified |
| **PowerShell Version** | 5.1.26100.9711 | Verified |

---

## 2. Storage and Volume Capacity

| Drive | Total Size | Used Space | Free Space | Percent Free | Evaluation / Constraint |
|---|---|---|---|---|---|
| **C:** | 236.93 GB | 221.94 GB | **14.99 GB** | 6.3% | **Constrained**: High risk of disk exhaustion. No large IDEs (e.g. Android Studio) or AVD system images should be installed on C:. |
| **G:** | 488.28 GB | 177.60 GB | **310.68 GB** | 63.6% | **Optimal**: Ample storage for StockQ workspace, Git repository, build outputs, and tooling caches. |

---

## 3. Java Development Kit (JDK) Audit

| Property | Value | Notes |
|---|---|---|
| **Active JDK Provider** | Eclipse Adoptium (Temurin) | Official OpenJDK vendor distribution |
| **Active Version** | OpenJDK 17.0.20.1+1 (64-Bit Server VM, mixed mode, sharing) | Verified via `java -version` |
| **Java Compiler** | javac 17.0.20.1 | Verified via `javac -version` |
| **JAVA_HOME** | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` | Verified in process environment |
| **Alternative JDK** | Amazon Corretto 21.0.5 (`C:\Users\legen\.jdks\corretto-21.0.5`) | Preserved untouched |
| **Compatibility Assessment** | Temurin JDK 17 is the recommended standard runtime for Android Gradle Plugin (AGP) 9.x and Gradle 9.x. No global modification is required. | Fully Compatible |

---

## 4. Android SDK & Command-Line Tooling Audit

| Component | Path / Location | Version | Status |
|---|---|---|---|
| **Android SDK Root** | `C:\Users\legen\AppData\Local\Android\Sdk` | Detected | Ready |
| **SDK Platforms** | `.../platforms/android-36` | API 36 (Android 16, rev 2.0.0) | Installed & Verified |
| **SDK Build-Tools** | `.../build-tools/36.0.0` | 36.0.0 | Installed & Verified |
| **SDK Platform-Tools** | `.../platform-tools/adb.exe` | 37.0.1 (ADB v1.0.41, build 15733141) | Installed & Verified |
| **Android CLI** | `C:\Users\legen\AppData\AndroidCLI\android.exe` | 1.0.16625370 | Installed & Verified |
| **SDK Licenses** | `.../licenses/android-sdk-license` | Accepted (`24333f8a...`) | Verified |
| **Command-Line Tools** | Available via Google repository (`cmdline-tools/latest` 23.0.0) | Managed directly by `android.exe` | Operational |

---

## 5. Gradle & Build Automation Audit

| Item | Details | Status |
|---|---|---|
| **System Gradle in PATH** | None (clean environment) | Verified |
| **Gradle Distribution Cache** | `C:\Users\legen\.gradle\wrapper\dists\gradle-9.1.0-bin` | Cached & Verified |
| **Selected Project Wrapper** | Gradle 9.1.0 (`gradle-9.1.0-bin.zip`) | Recommended for AGP 9.0.1 |
| **JVM Memory Configuration** | Low-footprint profile configured in `gradle.properties` (`-Xms64m -Xmx768m`) | Verified for Windows stability |

---

## 6. Version Control (Git) Audit

| Parameter | Configuration | Status |
|---|---|---|
| **Git Binary** | Git for Windows `2.53.0.windows.1` (`C:\Program Files\Git\cmd\git.exe`) | Verified |
| **Global User Name** | `Dr-SuBZeRo` | Verified |
| **Global User Email** | `legentsurya@gmail.com` | Verified |
| **Default Branch** | `main` | Verified |
| **StockQ Git State** | Clean directory ready for `git init` | Verified |

---

## 7. IDE & Extension Environment Audit

| Environment | Component / Extension | Version | Purpose |
|---|---|---|---|
| **Primary IDE** | Antigravity IDE (VS Code based) | 1.107.0 (x64) | Primary AI-assisted development IDE |
| **Installed Extension** | `redhat.java` | 1.58.0 | Java language server & diagnostics |
| **Installed Extension** | `vscjava.vscode-gradle` | 3.18.0 | Gradle task execution & syntax |
| **Installed Extension** | `vscjava.vscode-java-pack` | 0.31.2 | Java tooling bundle |
| **Installed Extension** | `eamodio.gitlens` | 19.5.0 | Git repository visualization |
| **Installed Extension** | `davidanson.vscode-markdownlint` | 0.62.1 | Documentation linting |
| **Newly Installed Extension** | `fwcd.kotlin` | 0.2.36 | Kotlin Language Server & diagnostics |
| **Newly Installed Extension** | `redhat.vscode-xml` | 0.29.3 | XML language support for Manifest & Resources |
| **Antigravity Sidecars** | `compose-preview`, `device-hub` | Active | Compose UI preview & device management |

---

## 8. Network Connectivity Audit

| Destination | Status Code | Verified Access |
|---|---|---|
| `https://dl.google.com/dl/android/maven2/index.html` | HTTP 200 OK | Google Maven repository reachable |
| `https://repo.maven.apache.org/maven2/` | HTTP 200 OK | Maven Central repository reachable |
| `https://services.gradle.org/distributions/` | HTTP 200 OK | Official Gradle distribution repository reachable |

---

## 9. Connected Devices & Virtualization Audit

| Device / Virtual Machine | Status | Details |
|---|---|---|
| **Physical USB Devices** | None attached | Run `adb devices` upon plugging in phone |
| **Wireless ADB** | None active | Can connect via `adb connect <ip>:<port>` |
| **Hardware Virtualization** | Supported & Enabled | Intel VT-x enabled in firmware |
| **AVD Emulators** | None configured | Emulation optional; if created, must be directed to drive `G:` |

---

## 10. Workspace Isolation Verification

- Target workspace: `G:\StockQ` (fully isolated, empty, exclusive write access).
- No external project source files, configurations, or directories will be referenced, copied, or inspected.
- All technical and architectural decisions are derived from first principles and official documentation.
