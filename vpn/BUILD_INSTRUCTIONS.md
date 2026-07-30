# Build Instructions for VLESS Android VPN Client

This document provides complete step-by-step instructions to compile and build the APK in Android Studio.

## Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
- **JDK 17** configured as project Gradle JDK
- **Android SDK API 34** (Build tools 34.0.0)
- **Xray-core Mobile Library**: `libv2ray.aar` placed inside `app/libs/` directory.

---

## 1. Project Setup
1. Clone or extract the project folder in Android Studio.
2. Ensure file structure:
```text
VlessVpnApp/
├── app/
│   ├── libs/
│   │   └── libv2ray.aar (Xray-core Golang bindings)
│   ├── src/main/java/com/vless/vpn/
│   │   ├── vpn/
│   │   │   ├── VlessConfig.kt
│   │   │   ├── XrayConfigBuilder.kt
│   │   │   ├── XrayManager.kt
│   │   │   └── VpnService.kt
│   │   ├── viewmodel/
│   │   │   └── VpnViewModel.kt
│   │   └── ui/
│   │       ├── MainActivity.kt
│   │       └── MainScreen.kt
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 2. Obtaining / Building `libv2ray.aar`
To run Xray-core on Android, Gomobile creates `libv2ray.aar`:
1. Clone 2dust v2rayNG core binding: `https://github.com/2dust/v2rayNG`
2. Run gomobile build:
   ```bash
   gomobile bind -v -target=android/arm,android/arm64,android/386,android/amd64 github.com/2dust/v2rayNG/v2ray
   ```
3. Move the compiled `libv2ray.aar` into `app/libs/libv2ray.aar`.

---

## 3. Building Debug / Release APK

### Command Line (Gradle)
Run the following terminal commands inside the project root:

- **Build Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
  Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

- **Build Signed Release APK**:
  ```bash
  ./gradlew assembleRelease
  ```
  Output APK location: `app/build/outputs/apk/release/app-release.apk`

---

## 4. Hardcoded VLESS Configuration Summary
- **VLESS Link**: `vless://69af5525-175e-4f19-b213-2c8ab84e7dbe@sakura.proxy.rlwy.net:10322?encryption=none&security=none&type=tcp&headerType=none#TCP`
- **Server Host**: `sakura.proxy.rlwy.net`
- **Port**: `10322`
- **Protocol**: VLESS (TCP)
- **UUID**: `69af5525-175e-4f19-b213-2c8ab84e7dbe`
