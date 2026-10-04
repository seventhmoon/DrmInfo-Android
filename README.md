# DRM Info for Android

[![Android CI](https://github.com/androidfung/DrmInfo-Android/actions/workflows/android.yml/badge.svg)](https://github.com/androidfung/DrmInfo-Android/actions/workflows/android.yml)
[![Google Play](https://img.shields.io/badge/Google%20Play-Download-brightgreen?logo=google-play)](https://play.google.com/store/apps/details?id=com.androidfung.drminfo)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

An open-source Android application that inspects and displays detailed Digital Rights Management (DRM) module information, hardware capabilities, media decoding/encoding support, audio formats, and display specifications for Android phones, tablets, and Android TV devices.

---

## ✨ Features

### 🔒 DRM Architecture & Security
Inspects installed DRM modular schemes via the Android `MediaDrm` API:
* **Google Widevine Modular DRM**:
  * Security Level (L1, L2, L3)
  * Vendor, Version, and Description
  * Algorithms and System ID
  * Maximum and Connected HDCP Levels
  * Usage Reporting Support & Max Sessions
* **ClearKey CDM**
* **Microsoft PlayReady** (where available)
* **Marlin DRM, Adobe Primetime, Huawei WisePlay, Verimatrix VCAS™** (if supported by device)

### 🎬 Media & Display
* **Media Performance Class (MPC)**: Detects Android Media Performance Class level (e.g., Class 12, 13, 14, 15).
* **Display Refresh Rates**: Supported display modes and seamless alternative refresh rates (Hz).
* **HDR Capabilities**: Supported High Dynamic Range formats (Dolby Vision, HDR10, HDR10+, HLG).
* **Color Gamut**: Wide Color Gamut (WCG) support.
* **Hardware Codecs**: Complete list of hardware-accelerated video decoders and encoders (AVC, HEVC, VP9, AV1, Dolby Vision, etc.).

### 🔊 Audio & Surround Sound
* **Supported Audio Formats**: Probes supported surround and immersive audio passthrough formats (Dolby Atmos, Dolby Digital Plus, TrueHD, DTS, DTS-HD, AC-4, etc.).
* **Spatial Audio & Head Tracking**: Detection of Spatializer availability and dynamic head tracking capabilities (Android 13+).

### ⚙️ Hardware & System
* **Device Identity**: Manufacturer, model, product, brand, board/hardware.
* **Processor (SoC)**: SoC manufacturer, SoC model, and supported ABIs.
* **Android OS**: OS version, release codename, SDK level, build fingerprint, security patch level, and quarterly release identifiers.

### 📺 Cross-Platform Support
* **Mobile & Tablet**: Modern Jetpack Compose UI with adaptive layouts, hero highlight badges, and quick-scroll navigation.
* **Android TV / Google TV**: Tailored 10-foot UI designed with Compose for TV (Material 3) and D-pad navigation.
* **Report Sharing**: Export and share system capabilities as formatted plain-text reports.

---

## 🛠️ Tech Stack & Architecture

* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Compose for TV (Material 3)](https://developer.android.com/develop/ui/compose/tv)
* **Architecture**: MVVM with Kotlin Coroutines & StateFlow
* **Language**: 100% [Kotlin](https://kotlinlang.org/)
* **Build System**: Gradle Version Catalogs (`libs.versions.toml`), Android Gradle Plugin 8.x+
* **Min SDK**: API 24 (Android 7.0 Nougat)
* **Target / Compile SDK**: API 37+ (Android 16 Baklava ready)

---

## 🚀 Building & Developing

### Prerequisites
* Android Studio Ladybug (2024.2.1) or newer
* JDK 17
* Android SDK Platform and Tools (compileSdk 37)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/androidfung/DrmInfo-Android.git
cd DrmInfo-Android

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build release bundle
./gradlew bundleRelease
```

---

## 📄 License

```text
DRM Info for Android
Copyright (C) 2017-2025 Fung Lam

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
