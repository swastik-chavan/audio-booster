<!-- <div align="center">

# Audio Booster

A modern Android audio enhancement application built with **Kotlin**, **Jetpack Compose**, and **Material 3**, designed to provide smooth system volume control and intelligent loudness enhancement.

<br>

[![Android](https://img.shields.io/badge/Android-5.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material-3-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-black?style=for-the-badge)](LICENSE)

</div>

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/picture.png" width="22"/> Screenshots

<div align="center">

<img src="https://github.com/user-attachments/assets/f3840f5d-1a31-432d-8f0f-b6101a3fc5c5" width="250"/>

<img src="https://github.com/user-attachments/assets/40f955be-099a-45e5-91d0-7cccfec80460" width="250"/>

<img src="https://github.com/user-attachments/assets/5004e0f1-815c-4da0-ac68-067643044945" width="250"/>

</div>

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/checkmark.png" width="22"/> Tested On Real Devices

This app has been **thoroughly tested on 5 real Android devices** to ensure compatibility and stability:

| Device | Android Version | Tested By | Status |
|--------|---|---|---|
| Samsung Galaxy F13 | 14 | Developer | ✅ Works perfectly |
| OnePlus CE 3 5G | 15 | Developer | ✅ Works perfectly |
| OPPO A59 5G | 15 | Developer | ✅ Works perfectly |
| Google Pixel 4a | 14 | Tester | ✅ Verified |
| iQOO Z10x | 14 | Tester | ✅ Verified |

**Supports:** Android 5.0 (API 21) to Android 15 (API 35)

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/document.png" width="22"/> Overview

Audio Booster is a native Android application focused on delivering a clean, premium experience for controlling media volume and applying hardware-based loudness enhancement.

Instead of modifying audio files, the application works directly with Android's audio framework to provide real-time system-wide loudness enhancement while maintaining smooth performance and modern UI/UX standards.

The application has been developed using modern Android architecture with Kotlin, StateFlow, Jetpack Compose, and lifecycle-aware components.
---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/light-on.png" width="22"/> Features

### <img src="https://img.icons8.com/ios-filled/20/ffffff/high-volume.png" width="20"/> Audio

- Real-time Media Volume Control
- LoudnessEnhancer Integration
- Ultra Boost Mode
- Live Audio Session Detection
- Automatic Audio Effect Updates
- Device-aware Audio Handling

### <img src="https://img.icons8.com/ios-filled/20/ffffff/smartphone.png" width="20"/> Device Support

- Phone Speaker
- Wired Headphones
- Bluetooth Earphones
- Bluetooth Speakers
- USB Audio Devices

### <img src="https://img.icons8.com/ios-filled/20/ffffff/color-palette.png" width="20"/> User Interface

- Jetpack Compose UI
- Material 3 Design
- Premium Dark Theme
- Smooth Animations
- Responsive Layout
- Adaptive Components
- Splash Screen
- Custom Slider Component

### <img src="https://img.icons8.com/ios-filled/20/ffffff/lightning-bolt.png" width="20"/> Performance

- Optimized Compose Recompositions
- Cached Canvas Drawing
- Stable Lambda References
- StateFlow Architecture
- Memory-efficient Audio Management
- Minimal Runtime Allocations

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/circled-play.png" width="22"/> Quick Start

### How to Use

| Feature | Steps |
|---------|-------|
| **Adjust Media Volume** | Use the **Media Volume** slider to control system volume (0–100%) |
| **Enable Boost** | Drag the **Boost Volume** slider to apply loudness enhancement |
| **Ultra Boost Mode** | Enable the switch after setting Boost > 0% for advanced amplification |
| **Check Device** | View current audio device and boost status in **System Status** |

### <img src="https://img.icons8.com/ios-filled/20/ffffff/error.png" width="20"/> Important Safety Notes

- High amplification may damage speakers, headphones, or hearing
- Use moderate boost levels (50–75%) for best results
- Always monitor audio quality while adjusting
- Use headphones at lower volumes to protect your hearing

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/workflow.png" width="22"/> Architecture

```text
                    UI (Jetpack Compose)
                            │
                            ▼
                     MainViewModel
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
      AudioController          LoudnessController
              │                           │
              └─────────────┬─────────────┘
                            ▼
                 Android Audio Framework
```

### Architecture Explanation

#### Data Flow

1. **AudioController** → Monitors system volume & device changes via BroadcastReceiver
2. **LoudnessController** → Applies audio effects to active sessions
3. **MainViewModel** → Combines data from both controllers via `combine()` StateFlow
4. **MainScreen** → Observes ViewModel state and renders UI

#### Key Design Decisions

| Decision | Reason |
|----------|--------|
| StateFlow for state management | Reactive, lifecycle-aware, integrates seamlessly with Compose |
| BroadcastReceiver for volume monitoring | Reliable way to detect system volume changes |
| Canvas for custom sliders | Better performance than standard Slider during rapid updates |
| No external dependencies | Simpler, faster, and easier to maintain |

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/source-code.png" width="22"/> Tech Stack

| Technology | Version | Usage |
|------------|---------|-------|
| Kotlin | 2.0 | Primary Language |
| Jetpack Compose | 2024.10.01 | UI Toolkit |
| Material 3 | Latest | Design System |
| StateFlow | Latest | State Management |
| ViewModel | Latest | UI Logic & Lifecycle |
| Coroutines | Latest | Asynchronous Operations |
| Android Gradle Plugin | 8.8.1 | Build System |
| AndroidX Core | 1.15.0 | Core Android APIs |
| LoudnessEnhancer API | Android 4.1+ | Audio Processing |
| Android AudioManager | Native | System Volume Control |

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/opened-folder.png" width="22"/> Project Structure

```text
app
│
├── audio
│   ├── AudioController.kt
│   ├── LoudnessController.kt
│   └── AudioState.kt
│
├── ui
│   ├── screens
│   │   └── MainScreen.kt
│   │
│   └── theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── viewmodel
│   └── MainViewModel.kt
│
└── MainActivity.kt
```
---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/download.png" width="22"/> Installation

### Option 1: Download Pre-built APK (Recommended)

Download the latest APK from the **Releases** section.

1. Download `audio-booster-v1.0.apk`
2. Transfer it to your Android device
3. Install the APK
4. Grant **Modify Audio Settings** permission when prompted
5. Launch Audio Booster and enjoy!

### Option 2: Build from Source

#### Requirements

- Android Studio (Latest Stable)
- Kotlin 2.0
- JDK 11+
- Android SDK API 35

#### Build Steps

```bash
# Clone the repository
git clone https://github.com/swastik-chavan/audio-booster.git

cd audio-booster

# Build Release APK
./gradlew assembleRelease
```

Or open the project in Android Studio and select:

```
Build → Generate APK(s) → Generate APK
```

Generated APK:

```
app/build/outputs/apk/release/app-release.apk
```

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/lock-2.png" width="22"/> Permissions

Only the required permissions are requested.

| Permission | Purpose |
|------------|---------|
| MODIFY_AUDIO_SETTINGS | Control system audio volume |
| CHANGE_NOTIFICATION_SETTINGS | Manage audio stream behaviour on supported Android versions |

✅ No Internet permission required.

✅ Works completely offline.

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/flash-on.png" width="22"/> Performance & Optimizations

### Performance Metrics

- APK Size: **~2.5 MB**
- Minimum RAM: **100 MB**
- Startup Time: **< 1 second**
- Runtime Memory: **30–50 MB**
- Background Services: **None**
- Battery Usage: **Minimal**

### Optimizations

| Optimization | Benefit |
|-------------|---------|
| Cached Canvas Drawing | Smooth rendering |
| Slider Update Throttling | Lower CPU usage |
| StateFlow (WhileSubscribed) | Better memory efficiency |
| Lazy Initialization | Faster startup |
| Stable Lambda References | Reduced Compose recomposition |

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/error.png" width="22"/> Known Limitations & Safety Notes

| Limitation | Details | Recommendation |
|------------|---------|----------------|
| High Boost | Very high boost may introduce distortion | Keep boost between 50–75% |
| Device Compatibility | Some low-end devices provide limited enhancement | Test lower boost first |
| Rooted Devices | Audio effects may not behave correctly | Recommended on stock Android |
| Silent Mode | Doesn't affect muted devices | Disable Silent/DND mode |
| Permission Required | MODIFY_AUDIO_SETTINGS is mandatory | Grant permission during first launch |

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/bug.png" width="22"/> Troubleshooting

### Boost Not Working?

Possible reasons:

- LoudnessEnhancer API isn't supported.
- Permission not granted.
- No media is currently playing.
- Device is in Silent or Do Not Disturb mode.

### App Crashes?

Try:

1. Restart your device.
2. Reinstall the app.
3. Clear App Cache.
4. Verify Android version (API 21+).

### Distorted Audio?

- Lower Boost level.
- Use better headphones/speakers.
- Keep system volume between **50–80%**.

### Battery Drain?

Audio Booster itself doesn't keep background services running.

Battery usage mainly comes from audio playback.

### Still Having Problems?

Please open a GitHub Issue including:

- Device model
- Android version
- Steps to reproduce
- Screenshots (if possible)

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/road.png" width="22"/> Roadmap

### Planned Features

The following features are planned for future releases:

- 🎨 Material You Dynamic Colors
- 🎚️ 7-Band Equalizer
- ⚡ Quick Settings Tile
- 🎮 Per-app Audio Profiles
- 🎧 Audio Presets (Music, Gaming, Movies)
- 📊 Privacy-first Usage Analytics
- 📳 Haptic Feedback
- 🪟 Home Screen Widgets
- 🌈 Theme Customization
- 🌍 Multi-language Support
- 🔧 Stability Improvements

> **Note:** Features may change based on community feedback and future Android API updates.

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/handshake.png" width="22"/> Contributing

Contributions are always welcome!

### 🐞 Reporting Bugs

Before opening a new issue:

- Search existing issues first.
- Include your device model.
- Include Android version.
- Explain how to reproduce the bug.
- Attach screenshots if possible.

### 💡 Feature Requests

Want a new feature?

Open a Feature Request and explain:

- What you'd like
- Why it would be useful
- Possible implementation ideas

### 💻 Code Contributions

1. Fork the repository.
2. Create a new branch.

```bash
git checkout -b feature/my-feature
```

3. Commit your changes.

```bash
git commit -m "feat: add new feature"
```

4. Push your branch.

```bash
git push origin feature/my-feature
```

5. Open a Pull Request.

### Coding Guidelines

- Follow Google's Kotlin Style Guide.
- Write meaningful commit messages.
- Test before opening a PR.
- Keep code clean and documented.

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/accessibility.png" width="22"/> Accessibility

Audio Booster follows Material Design accessibility guidelines.

Features include:

- Large touch targets
- High contrast UI
- Readable typography
- Adaptive layouts
- Content descriptions
- Responsive dialogs
- Better usability on small screens

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/document.png" width="22"/> License

This project is licensed under the **MIT License**.

See the **LICENSE** file for more details.

### Credits

Built using:

- Jetpack Compose
- Android Jetpack
- Kotlin
- Material Design 3

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/user.png" width="22"/> Author

**Swastik Chavan**

GitHub:
https://github.com/swastik-chavan

Explore more projects:
https://github.com/swastik-chavan?tab=repositories

### ❤️ Special Thanks

Special thanks to everyone who tested the application and shared valuable feedback.

---

## <img src="https://img.icons8.com/ios-filled/24/ffffff/help.png" width="22"/> Getting Help

If you need assistance:

- 📖 Read this README.
- 🐞 Open a GitHub Issue.
- 💬 Use GitHub Discussions.
- ⭐ Star the repository if you found it useful.

---

<div align="center">

### Built with ❤️ using Kotlin • Jetpack Compose • Material 3

<br>

**Made with ❤️ by Swastik**

If you found this project helpful,

## ⭐ Star this Repository ⭐

</div> --!>

<h1> Under Development </h1>

