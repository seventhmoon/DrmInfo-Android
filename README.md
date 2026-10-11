# DRM Info for Android

[![Android CI](https://github.com/androidfung/DrmInfo-Android/actions/workflows/android.yml/badge.svg)](https://github.com/androidfung/DrmInfo-Android/actions/workflows/android.yml)
[![Google Play](https://img.shields.io/badge/Google%20Play-Download-brightgreen?logo=google-play)](https://play.google.com/store/apps/details?id=com.androidfung.drminfo)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

An open-source Android application that inspects and displays detailed Digital Rights Management (DRM) module information, hardware capabilities, media decoding/encoding support, audio formats, and display specifications for Android phones, tablets, and Android TV devices.

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.androidfung.drminfo)

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

## ❓ Frequently Asked Questions (FAQ)

### 1. Can DRM Info fix, restore, or upgrade my DRM security level?
No. DRM Info is strictly an informational reporting tool. DRM security levels (such as Widevine L1) require hardware-isolated cryptographic certificates provisioned at the factory by the device manufacturer. A third-party application cannot repair, modify, or restore these security certificates.

### 2. What is the difference between Widevine L1, L2, and L3?
* **L1**: Cryptography and media decoding are completely handled in a hardware-isolated environment (TEE). Major streaming services require L1 for HD (1080p), 4K, and HDR.
* **L2**: Cryptography is performed in hardware, but media processing happens in software.
* **L3**: Both cryptography and media processing are handled in software. Streaming services typically limit L3 devices to Standard Definition (SD, 480p/540p).

### 3. Why did my device downgrade from Widevine L1 to L3?
The most common reasons are:
* Unlocking the bootloader, rooting, or flashing a custom ROM, which breaks Android's hardware keystore integrity checks.
* A corrupted or faulty official OEM software update that affected keystore/TEE provisioning.

### 4. DRM Info shows Widevine L1, why does Netflix / Prime Video still restrict playback to SD?
DRM Info directly reports the hardware security level currently declared by the on-device Android MediaDrm module. However, streaming providers like Netflix maintain server-side revocation and security lists. If a specific device model, chipset, or firmware build is flagged as compromised, vulnerable, or no longer secure, the streaming service's servers will restrict playback resolution to SD regardless of what the device locally reports.

### 5. My screen supports HDR, but streaming apps will not play HDR content. Why?
For HDR streaming to function, all three criteria must be satisfied:
1. The display panel must support the specific HDR standard (HDR10, Dolby Vision, HLG, etc.).
2. The chipset must have a certified hardware video decoder capable of decoding that HDR format.
3. The streaming provider must have certified that specific device model for HDR playback on their platform.

### 6. What does "HDCP Level" mean (Max vs. Connected)?
HDCP (High-bandwidth Digital Content Protection) secures content transmitted across external connections like HDMI or DisplayPort:
* **Max HDCP Level**: The highest revision the phone/tablet hardware supports (e.g., HDCP 2.2 / 2.3).
* **Connected HDCP Level**: The revision currently negotiated with the currently attached display or TV (or "No Digital Output" / "Disconnected" when not plugged into an external display).

### 7. What is Media Performance Class (MPC)?
Introduced in Android 12, Media Performance Class defines a set of baseline multimedia requirements set by Google. It evaluates media codec concurrency, frame drop thresholds during playback/recording, camera initialization speed, and flash storage read/write performance. (Class 12 = Android 12 baseline, Class 13 = Android 13 baseline, etc. A value of 0 means the OEM did not declare a performance class).

### 8. What are "Seamless Refresh Rates"?
These are display refresh rates (e.g., switching between 60Hz, 90Hz, or 120Hz) that the display hardware can transition between dynamically without causing visual flicker or blanking the screen.

### 9. Why doesn't DRM Info list Microsoft PlayReady or other DRM schemes on my phone?
Android supports modular DRM via the `MediaDrm` API, but manufacturers choose which modules to license and integrate. While Google Widevine and CENC ClearKey are standard on Google-certified Android devices, proprietary schemes like Microsoft PlayReady, Marlin, or Verimatrix are only present if specifically bundled by the OEM (common on select Android TVs or carrier devices).

### 10. Does DRM Info collect or upload my device or DRM information?
DRM Info inspects and generates all device specifications entirely on-device using local Android APIs. No device specifications, hardware IDs, or DRM data are collected, transmitted, or stored on external servers. (Internet access is only utilized if ads or consent forms are enabled).

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
