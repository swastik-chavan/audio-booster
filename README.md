<div align="center">

# Audio Booster

A modern Android audio enhancement application built with **Kotlin**, **Jetpack Compose**, and **Material 3**, designed to provide smooth system volume control and intelligent loudness enhancement with a premium user experience.

<br>

[![Android](https://img.shields.io/badge/Android-5.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material-3-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-black?style=for-the-badge)](LICENSE)

</div>

---

# Screenshots

<div align="center">

<img src="https://github.com/user-attachments/assets/f3840f5d-1a31-432d-8f0f-b6101a3fc5c5" width="250"/>

<img src="https://github.com/user-attachments/assets/40f955be-099a-45e5-91d0-7cccfec80460" width="250"/>

<img src="https://github.com/user-attachments/assets/5004e0f1-815c-4da0-ac68-067643044945" width="250"/>

</div>

---

# Overview

Audio Booster is a native Android application focused on delivering a clean, premium experience for controlling media volume and applying hardware-based loudness enhancement.

Instead of modifying audio files, the application works directly with Android's audio framework to provide real-time system-wide loudness enhancement while maintaining smooth performance and modern Material 3 design principles.

The application has been developed using modern Android architecture with Kotlin, StateFlow, Jetpack Compose, and lifecycle-aware components.

---

# Features

### Audio

- Real-time Media Volume Control
- LoudnessEnhancer Integration
- Ultra Boost Mode
- Live Audio Session Detection
- Automatic Audio Effect Updates
- Device-aware Audio Handling

### Device Support

- Phone Speaker
- Wired Headphones
- Bluetooth Earphones
- Bluetooth Speakers
- USB Audio Devices

### User Interface

- Jetpack Compose UI
- Material 3 Design
- Premium Dark Theme
- Smooth Animations
- Responsive Layout
- Adaptive Components
- Splash Screen
- Custom Slider Component

### Performance

- Optimized Compose Recompositions
- Cached Canvas Drawing
- Stable Lambda References
- StateFlow Architecture
- Memory-efficient Audio Management
- Minimal Runtime Allocations

---

# Architecture

```
                    UI (Jetpack Compose)
                             │
                             ▼
                     MainViewModel
                             │
          ┌──────────────────┴──────────────────┐
          │                                     │
          ▼                                     ▼
 AudioController                  LoudnessController
          │                                     │
          └──────────────────┬──────────────────┘
                             ▼
                    Android Audio Framework
```

---

# Tech Stack

| Technology | Usage |
|------------|-------|
| Kotlin | Primary Language |
| Jetpack Compose | UI Toolkit |
| Material 3 | Design System |
| StateFlow | State Management |
| ViewModel | UI Logic |
| Coroutines | Asynchronous Operations |
| LoudnessEnhancer API | Audio Processing |
| Android AudioManager | System Volume |
| Gradle Kotlin DSL | Build System |

---

# Project Structure

```
app
│
├── audio
│   ├── AudioController.kt
│   ├── LoudnessController.kt
│   └── AudioState.kt
│
├── ui
│   ├── screens
│   │      └── MainScreen.kt
│   │
│   └── theme
│          ├── Color.kt
│          ├── Theme.kt
│          └── Type.kt
│
├── viewmodel
│      └── MainViewModel.kt
│
└── MainActivity.kt
```

---

# How It Works

1. Detects the currently active output device.
2. Monitors Android media sessions.
3. Applies `LoudnessEnhancer` to active audio sessions.
4. Synchronizes UI state using StateFlow.
5. Updates boost values in real time.
6. Cleans up audio effects automatically when sessions end.

---

# Requirements

| Requirement | Version |
|------------|---------|
| Android Studio | Latest Stable |
| Kotlin | 2.x |
| Min SDK | 21 |
| Target SDK | 35 |
| Compile SDK | 35 |

---

# Build

Clone the repository

```bash
git clone https://github.com/swastik-chavan/audio-booster.git
```

Open the project in Android Studio.

Build

```
Build → Generate APK(s)
```

or

```
Build → Generate App Bundle
```

---

# Permissions

The application only uses permissions required for audio functionality.

- Modify Audio Settings
- Foreground Audio Interaction (where applicable)

No internet permission is required for the core functionality.

---

# Performance Optimizations

- Cached drawing allocations
- Stable Compose callbacks
- State hoisting
- Optimized recomposition scope
- Efficient BroadcastReceiver lifecycle
- Audio session cleanup
- Memory-safe LoudnessEnhancer management
- Lifecycle-aware state management

---

# Accessibility

- Large touch targets
- Material 3 typography
- Adaptive layouts
- Scrollable dialogs
- Content descriptions
- Dynamic device detection

---

# Future Improvements

- Dynamic Equalizer
- Preset Profiles
- Custom Audio Profiles
- Widget Support
- Quick Settings Tile
- Material You Dynamic Colors
- Per-device Profiles
- Usage Analytics (Optional)

---

# License

This project is licensed under the MIT License.

---

# Author

**Swastik Chavan**

GitHub

https://github.com/swastik-chavan

---

<div align="center">

Built with Kotlin • Jetpack Compose • Material 3

</div>

<div align="center">

Made with 🤍 by <b>Swastik</b>

</div>
