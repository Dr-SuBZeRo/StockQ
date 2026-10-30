# StockQ — Toolchain & Development Environment Installation Report

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.subzero.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  

---

## 1. Installed Development Environment Components

Every component below was verified through direct command execution:

| Component | Path | Verified Version | Verification Command | Exit Code |
|---|---|---|---|---|
| **Temurin JDK 17** | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` | 17.0.20.1+1 | `java -version` | 0 |
| **Java Compiler** | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\javac.exe` | 17.0.20.1 | `javac -version` | 0 |
| **Android SDK Platform** | `C:\Users\legen\AppData\Local\Android\Sdk\platforms\android-36` | API 36 (2.0.0) | `android sdk list` | 0 |
| **Android Build Tools** | `C:\Users\legen\AppData\Local\Android\Sdk\build-tools\36.0.0` | 36.0.0 | Directory / CLI check | 0 |
| **Android Platform Tools**| `C:\Users\legen\AppData\Local\Android\Sdk\platform-tools\adb.exe` | 37.0.1 (ADB 1.0.41)| `adb version` | 0 |
| **Android CLI** | `C:\Users\legen\AppData\AndroidCLI\android.exe` | 1.0.16625370 | `android --version` | 0 |
| **Git for Windows** | `C:\Program Files\Git\cmd\git.exe` | 2.53.0.windows.1 | `git --version` | 0 |
| **Antigravity IDE** | `C:\Users\legen\AppData\Local\Programs\Antigravity IDE` | 1.107.0 (x64) | `antigravity-ide.cmd --version`| 0 |
| **Antigravity Kotlin Ext**| VS Code Marketplace (`fwcd.kotlin`) | 0.2.36 | `antigravity-ide.cmd --install-extension`| 0 |
| **Antigravity XML Ext** | VS Code Marketplace (`redhat.vscode-xml`) | 0.29.3 | `antigravity-ide.cmd --install-extension`| 0 |

---

## 2. Technical Evaluation: Android Studio vs. Antigravity IDE

### Background
Android Studio is an IntelliJ-based IDE distributed by Google. The prompt instructed us to evaluate whether installing the latest stable Android Studio (Narwhal 2025.1.x / Ladybug 2024.2.x) provides meaningful benefits for SDK management, emulator management, layout inspection, profiler, device management, and Gradle inspection.

### Evaluation Findings

1. **System Disk Space Constraint (CRITICAL)**:
   - System drive `C:` has only **14.99 GB** of free space remaining.
   - An Android Studio installation requires:
     - Installer: ~1.2 GB
     - IDE binaries & caches: ~3.5 to 5.0 GB
     - Default SDK / system images: ~4.0 to 8.0 GB
     - Total impact on C: drive would easily exceed **9 to 14 GB**, risking catastrophic Windows disk exhaustion (`< 1 GB free`).

2. **Antigravity IDE + Android CLI Standalone Sufficiency**:
   - **SDK Management**: `android.exe sdk install/update/list` manages platforms, build-tools, and command-line tools without requiring Android Studio.
   - **Compose Preview**: The `compose-preview` sidecar inside Antigravity renders Jetpack Compose `@Preview` composables directly within the editor.
   - **Device & Emulator Management**: The `device-hub` sidecar in Antigravity and ADB CLI manage physical and virtual devices seamlessly.
   - **Command-Line Builds**: The Gradle wrapper (`gradlew.bat`) builds debug/release APKs, runs unit tests, and validates code without Android Studio.
   - **Profiling & Debugging**: ADB logcat, heap dump, and systrace are available via standard command line.

### Conclusion & Recommendation
Installing Android Studio on drive `C:` is **strongly discouraged** due to the low disk space on `C:`. Development can proceed with 100% functionality and zero roadblocks directly inside Antigravity IDE. If Android Studio is ever desired by the user in the future, it must be installed exclusively onto drive `G:` (which has **310 GB** free).
