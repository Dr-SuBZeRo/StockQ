# StockQ — Image Asset Validation & Audit Report

**Product**: StockQ  
**Application ID**: `com.stockq`  
**Developer**: SuBZeRo  
**Audit Date**: October 30, 2026  
**Auditor**: Antigravity Assistant & Senior Design Systems Engineer  

---

## 1. Asset Inventory & Technical Properties

Every image file located in `G:\StockQ\images` was inspected programmatically using `System.Drawing`:

| Relative Path | Dimensions (W × H) | Aspect Ratio | Format | Pixel Format | Alpha Channel | File Size | Primary Purpose | Suitability Assessment |
|---|---|---|---|---|---|---|---|---|
| `images/app-icon/stockq_app_icon_1024.png` | 1254 × 1254 | 1:1 | PNG | `Format24bppRgb` | **No** (Opaque) | 1,386.41 KB | Google Play 512×512 Store icon & master graphic | **Suitable** for store listing (after 512×512 export). High resolution, crisp details, solid dark backdrop. |
| `images/app-icon/stockq_adaptive_foreground.png` | 1254 × 1254 | 1:1 | PNG | `Format24bppRgb` | **No** (Opaque near-white #FCFCFC) | 1,511.45 KB | Android Adaptive Icon Foreground Layer | **Needs Processing**: Adaptive foreground layers must have an alpha channel so the icon can float over the adaptive background layer and fit inside circular/squircle launcher masks without edge clipping. |
| `images/logo/stockq_logo_light.png` | 1983 × 793 | 2.5:1 | PNG | `Format24bppRgb` | **No** (Opaque white #FEFEFE) | 1,180.25 KB | Brand Header Logo for Light Theme | **Suitable with care**: Designed for white/light surfaces. If placed on tinted surfaces, alpha channel or card framing is required. |
| `images/logo/stockq_logo_dark.png` | 1983 × 793 | 2.5:1 | PNG | `Format24bppRgb` | **No** (Opaque deep navy #001B48) | 1,483.01 KB | Brand Header Logo for Dark Theme | **Suitable with care**: Matches deep navy theme container (`#0F172A`). |
| `images/logo/stockq_brand_combined.png` | 1536 × 1024 | 1.5:1 | PNG | `Format24bppRgb` | **No** (Opaque) | 1,631.36 KB | Combined Brand Identity & Marketing Lockup | **Promotional asset**: High visual quality, suitable for About/Welcome header card or marketing deck. |
| `images/promotional/stockq_play_feature_graphic.png` | 1791 × 878 | 2.04:1 | PNG | `Format24bppRgb` | **No** (Opaque) | 1,706.30 KB | Google Play Store Feature Graphic | **Non-Standard Dimensions**: Current size is 1791×878 px. Google Play strictly requires **1024 × 500 px**. Aspect ratio (2.0398:1) is very close to 1024:500 (2.048:1). Must be exported to exact 1024×500 px. |
| `images/promotional/stockq_promotional_poster.png` | 1055 × 1491 | 0.71:1 (A4-ish portrait) | PNG | `Format24bppRgb` | **No** (Opaque) | 1,763.19 KB | Printable A4 / Retail Shop Promotional Poster | **Suitable**: High-res vertical banner for print/brochures. Kept out of runtime APK. |
| `images/promotional/stockq_promotional_banner.png` | 1536 × 1024 | 1.5:1 (3:2 landscape) | PNG | `Format24bppRgb` | **No** (Opaque) | 1,780.39 KB | Social Media / Web Landscape Banner | **Suitable**: Kept out of runtime APK. |

---

## 2. In-Depth Component Analysis & Risks

### 2.1 Launcher Icon & Adaptive Foreground
- **Issue**: `stockq_adaptive_foreground.png` is an opaque 24-bit PNG with an off-white background (`#FCFCFC`).
- **Android Adaptive Icon Requirement**: Android adaptive icons require a foreground layer with **transparent padding** in the outer 18dp boundary (the safe area is the inner 66dp of a 108dp canvas, or 72dp circle). If an opaque square is used as a foreground layer, launchers rendering round, teardrop, or squircle masks will clip the edges abruptly or render an unsightly white box.
- **Action Plan**:
  1. Generate multi-density mipmaps (`mdpi` 48px, `hdpi` 72px, `xhdpi` 96px, `xxhdpi` 144px, `xxxhdpi` 192px) with transparent padding.
  2. Maintain `stockq_app_icon_1024.png` as the high-resolution master asset in `images/app-icon/`.
  3. Ensure the original master files in `images/` remain untouched.

### 2.2 Google Play Feature Graphic
- **Current Dimension**: 1791 × 878 px.
- **Official Google Play Console Specification**: Exactly **1024 × 500 px**, JPEG or 24-bit PNG (no alpha), up to 15 MB.
- **Action Plan**: Export a compliant 1024 × 500 px version to `images/promotional/export/stockq_play_feature_graphic_1024x500.png` using high-quality bicubic interpolation without modifying the master file.

### 2.3 Logo Integration in Application UI
- **Light & Dark Logos**: Because both logos have solid backgrounds (`#FEFEFE` and `#001B48`), they should be integrated cleanly within matching container cards or header banners (e.g. inside `WelcomeScreen` and `About` dialog), or rendered using themed surface cards.

