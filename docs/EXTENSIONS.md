# StockQ — Antigravity IDE Extension Inventory

**Product**: StockQ  
**Developer**: SuBZeRo  
**Application ID**: `com.stockq`  
**Workspace**: `G:\StockQ`  
**Date**: October 30, 2026  

---

## 1. Extension Strategy for Android & Kotlin Development

To provide the developer with robust IDE features — syntax highlighting, code completion, diagnostics, XML schema validation, and Git tracking — the following extensions are active in Antigravity IDE:

| Extension Name | Publisher | Identifier | Installed Version | Status | Primary Purpose in StockQ |
|---|---|---|---|---|---|
| **Kotlin Language Support** | fwcd | `fwcd.kotlin` | 0.2.36 | Installed | Kotlin Language Server, syntax coloring, diagnostics, type tooltips |
| **XML Language Support** | Red Hat | `redhat.vscode-xml` | 0.29.3 | Installed | AndroidManifest.xml and res/xml schema validation and formatting |
| **Gradle for Java** | Microsoft | `vscjava.vscode-gradle`| 3.18.0 | Installed | Gradle task explorer, syntax highlighting for `.gradle.kts` files |
| **Language Support for Java** | Red Hat | `redhat.java` | 1.58.0 | Installed | Java language server for JVM class analysis |
| **Extension Pack for Java** | Microsoft | `vscjava.vscode-java-pack`| 0.31.2 | Installed | Comprehensive JVM debugging and unit test runner |
| **GitLens** | Eric Amodio / GitKraken | `eamodio.gitlens` | 19.5.0 | Installed | In-editor blame, history, branch graph, and revision auditing |
| **Markdownlint** | David Anson | `davidanson.vscode-markdownlint` | 0.62.1 | Installed | Quality assurance for technical documentation files |
| **Material Icon Theme** | Philipp Kief | `pkief.material-icon-theme` | 5.35.0 | Installed | Clean file-type icons for Kotlin, Gradle, XML, and res files |

---

## 2. Integrated Sidecars & AI Tools

| Tool / Sidecar | Provider | Status | Capabilities |
|---|---|---|---|
| **Android Device Hub** | Google Antigravity Plugin | Active | Manage, mirror, touch, inspect, and deploy to physical and virtual Android devices |
| **Android Compose Preview** | Google Antigravity Plugin | Active | Renders `@Preview` Compose functions directly in the editor pane via `android preview render` |
| **Android Knowledge Base** | Android CLI | Active | Official Android documentation lookup via `android docs` |

---

## 3. Verified Extension Installation

All extensions were installed and verified via `antigravity-ide.cmd --install-extension`:
```powershell
& "C:\Users\legen\AppData\Local\Programs\Antigravity IDE\bin\antigravity-ide.cmd" --install-extension fwcd.kotlin
& "C:\Users\legen\AppData\Local\Programs\Antigravity IDE\bin\antigravity-ide.cmd" --install-extension redhat.vscode-xml
```
Both reported exit code `0`.
