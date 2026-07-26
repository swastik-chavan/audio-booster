<div align="center">

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

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/image.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Screenshots

<div align="center">

<img src="https://github.com/user-attachments/assets/f3840f5d-1a31-432d-8f0f-b6101a3fc5c5" width="250"/>

<img src="https://github.com/user-attachments/assets/40f955be-099a-45e5-91d0-7cccfec80460" width="250"/>

<img src="https://github.com/user-attachments/assets/5004e0f1-815c-4da0-ac68-067643044945" width="250"/>

</div>

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/check-circle.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Tested On Real Devices

This app has been **thoroughly tested on 5 real Android devices** to ensure compatibility and stability:

| Device | Android Version | Tested By | Status |
|--------|---|---|---|
| Samsung Galaxy F13 | 14 | Developer | ✅ Works perfectly |
| OnePlus CE 3 5G | 15 | Developer | ✅ Works perfectly |
| OPPO A59 5G | 15 | Developer | ✅ Works perfectly |
| Google Pixel 4a | 14 | Friend | ✅ Verified |
| iQOO Z10x | 14 | Friend | ✅ Verified |

**Supports:** Android 5.0 (API 21) to Android 15 (API 35)

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/book.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Overview

Audio Booster is a native Android application focused on delivering a clean, premium experience for controlling media volume and applying hardware-based loudness enhancement.

Instead of modifying audio files, the application works directly with Android's audio framework to provide real-time system-wide loudness enhancement while maintaining smooth performance and modern UI/UX standards.

The application has been developed using modern Android architecture with Kotlin, StateFlow, Jetpack Compose, and lifecycle-aware components.

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/spark.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Features

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/speaker-high.svg" width="24" height="24" style="margin-right: 6px; vertical-align: middle;"> Audio

- Real-time Media Volume Control
- LoudnessEnhancer Integration
- Ultra Boost Mode
- Live Audio Session Detection
- Automatic Audio Effect Updates
- Device-aware Audio Handling

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/device-mobile.svg" width="24" height="24" style="margin-right: 6px; vertical-align: middle;"> Device Support

- Phone Speaker
- Wired Headphones
- Bluetooth Earphones
- Bluetooth Speakers
- USB Audio Devices

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/palette.svg" width="24" height="24" style="margin-right: 6px; vertical-align: middle;"> User Interface

- Jetpack Compose UI
- Material 3 Design
- Premium Dark Theme
- Smooth Animations
- Responsive Layout
- Adaptive Components
- Splash Screen
- Custom Slider Component

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/lightning.svg" width="24" height="24" style="margin-right: 6px; vertical-align: middle;"> Performance

- Optimized Compose Recompositions
- Cached Canvas Drawing
- Stable Lambda References
- StateFlow Architecture
- Memory-efficient Audio Management
- Minimal Runtime Allocations

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/play-circle.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Quick Start

### How to Use

| Feature | Steps |
|---------|-------|
| **Adjust Media Volume** | Use the "Media Volume" slider to control system volume (0-100%) |
| **Enable Boost** | Drag the "Boost Volume" slider to apply loudness enhancement |
| **Ultra Boost Mode** | Enable the switch after setting Boost > 0% for advanced amplification |
| **Check Device** | View current audio device and boost status in "System Status" |

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/warning.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Important Safety Notes

- High amplification may damage speakers, headphones, or hearing
- Use moderate boost levels (50-75%) for best results
- Always monitor audio quality while adjusting
- Use headphones at lower volumes to protect your hearing

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/blueprint.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Architecture

```
                    UI (Jetpack Compose)
                             │
                             ▼
                     MainViewModel
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
     AudioController              LoudnessController
              │                             │
              └──────────────┬──────────────┘
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
| BroadcastReceiver for volume monitoring | Most reliable way to detect system volume changes |
| Canvas for custom sliders | Better performance than standard Slider during rapid updates |
| No external dependencies | Simpler, faster, and easier to maintain |

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/stack.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Tech Stack

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

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/folder.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Project Structure

```
app
│
├── audio
│   ├── AudioController.kt       # System volume & device monitoring
│   ├── LoudnessController.kt    # Audio effects management
│   └── AudioState.kt            # UI state data class
│
├── ui
│   ├── screens
│   │   └── MainScreen.kt        # Main UI composable (~1000 lines)
│   │
│   └── theme
│       ├── Color.kt             # Material 3 color palette
│       ├── Theme.kt             # Theme configuration
│       └── Type.kt              # Typography settings
│
├── viewmodel
│   └── MainViewModel.kt         # State management & business logic
│
└── MainActivity.kt              # Activity entry point
```

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/download.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Installation

### Option 1: Download Pre-built APK (Easiest)

Download the latest APK from the [Releases](https://github.com/swastik-chavan/audio-booster/releases/tag/v1.0) section.

1. Download `audio-booster-v1.0.apk`
2. Transfer to your Android device (or direct install from browser)
3. Install the APK
4. Grant `Modify Audio Settings` permission when prompted
5. Open the app and start boosting!

### Option 2: Build from Source

#### Prerequisites
- Android Studio (Latest Stable - Hedgehog+)
- Kotlin 2.0
- JDK 11 or higher
- Android SDK API Level 35

#### Build Steps

```bash
# Clone the repository
git clone https://github.com/swastik-chavan/audio-booster.git
cd audio-booster

# Build APK (release mode)
./gradlew assembleRelease

# Or open in Android Studio and use:
# Build → Generate APK(s) → release
```

The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/lock.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Permissions

The application only uses permissions required for audio functionality:

- **MODIFY_AUDIO_SETTINGS** - Required to control system volume
- **CHANGE_NOTIFICATION_SETTINGS** - For audio stream management (where applicable on newer Android versions)

**No internet permission is required** for the core functionality. This app works completely offline.

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/bolt.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Performance & Optimizations

### Performance Metrics

- **App Size:** ~2.5 MB (release APK)
- **Minimum RAM Required:** 100 MB free
- **Battery Impact:** Negligible (no background services)
- **Memory Footprint:** ~30-50 MB when running
- **Startup Time:** <1 second

### Optimizations Implemented

| Optimization | Impact | Benefit |
|---|---|---|
| Cached Canvas drawing | Prevents redraw on every frame | Smooth 60 FPS performance |
| Throttled slider updates (25ms) | Reduces rapid state updates | Lower CPU usage |
| StateFlow with WhileSubscribed | Memory-efficient flow sharing | Prevents memory leaks |
| Lazy device monitor initialization | Only loads when needed | Faster startup |
| Stable lambda references | Prevents unnecessary recompositions | Better Compose efficiency |

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/warning-circle.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Known Limitations & Safety Notes

| Limitation | Details | Workaround |
|---|---|---|
| **High boost distortion** | Very high amplification (90-100%) may cause audio distortion | Use moderate levels (50-75% for best quality) |
| **Device compatibility** | May fail or provide low audio quality on some budget devices | Test with lower boost levels first |
| **Rooted devices** | Audio effects may not work on rooted Android installations | Use on non-rooted devices only |
| **Stereo sound issue** | Some devices may have stereo sound processing issues | Report specific device on GitHub Issues |
| **System muting** | Doesn't work when device is in silent/do-not-disturb mode | Disable silent mode to use boost |
| **Requires permission grant** | `MODIFY_AUDIO_SETTINGS` is mandatory | Grant permission at first launch |

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/bug.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Troubleshooting

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/question.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Boost not working?

**Possible Causes:**

1. **Device doesn't support LoudnessEnhancer API**
   - Check Android version (requires API 18+, works best on API 21+)
   - Some budget devices may not include LoudnessEnhancer
   - **Solution:** App displays "Unsupported" message if not available

2. **Permission not granted**
   - **Solution:** Go to `Settings → Apps → Audio Booster → Permissions → Enable "Modify Audio Settings"`

3. **Audio effect not applied to current app**
   - LoudnessEnhancer only affects the media stream (music, videos, games)
   - Does NOT affect calls or system notification sounds
   - **Solution:** Make sure media/music app is actively playing

4. **Silent/Do Not Disturb mode enabled**
   - **Solution:** Disable silent mode first, then use boost

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/question.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> App crashes on startup?

**Solutions:**

1. Check Android version (requires Android 5.0+, API 21+)
2. Uninstall and reinstall the app completely
3. Clear app cache: `Settings → Apps → Audio Booster → Storage → Clear Cache`
4. Restart your device
5. Report issue on GitHub with device model and Android version

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/question.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> High battery drain?

**Solution:**
- Audio Booster uses minimal resources (no background services or wake locks)
- Battery drain is from amplified audio playback itself, not the app
- Using lower boost levels reduces overall power consumption
- Check if another app is consuming battery

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/question.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Audio quality sounds bad or distorted?

**Solutions:**

1. **Lower the boost level** - Start at 30-50% and increase gradually
2. **Check speaker/headphone condition** - Damaged speakers produce distortion
3. **Use medium volume** - Better results at 50-80% system volume + moderate boost
4. **Try different audio apps** - Some apps have better audio quality than others

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/question.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Not working on my device?

Before reporting:
1. Note your device model and Android version
2. Try with a different music app (Spotify, YouTube Music, etc.)
3. Test with lower boost levels first
4. Clear app cache and reinstall

**Still having issues?** [Report a bug](https://github.com/swastik-chavan/audio-booster/issues/new) with full details.

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/target.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Roadmap

### Planned Features & Improvements

We have **many features planned** to make Audio Booster even better:

#### Planned Enhancements (No specific timeline)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/wrench.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Fix and improve stability on edge devices
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/paint-brush.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Material You Dynamic Colors support
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/wave-sine.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Dynamic 7-band Equalizer
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/lightning.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Quick Settings Tile for fast access
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/game-controller.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Per-app audio profiles (auto-boost for games)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/headphones.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Audio preset profiles (Gaming, Music, Movies, Calls)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/chart-bar.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Usage analytics (privacy-first, optional)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/vibrate.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Haptic feedback on interactions
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/app-window.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Widget Support
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/swatches.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Theme customization options
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/globe.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Multi-language support

**Note:** Roadmap is subject to change based on community feedback and testing results.

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/handshake.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Contributing

Contributions are welcome! Whether you want to report bugs, suggest features, or submit code, here's how:

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/bug.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Report a Bug

1. Check if the issue already exists: [Issues](https://github.com/swastik-chavan/audio-booster/issues)
2. If not, [create a new issue](https://github.com/swastik-chavan/audio-booster/issues/new) with:
   - Your device model and Android version
   - Steps to reproduce the issue
   - Expected vs actual behavior
   - Screenshots (if applicable)

#### Example Bug Report:

```
Device: Samsung Galaxy F13, Android 14
Issue: Slider lags when dragging

Steps to reproduce:
1. Open app
2. Drag boost slider quickly
3. Notice delay in UI update

Expected: Smooth slider movement
Actual: 1-2 second lag
```

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/lightbulb.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Suggest a Feature

1. [Open an issue](https://github.com/swastik-chavan/audio-booster/issues/new)
2. Label it as `feature-request`
3. Describe the feature and why it would help

### <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/hammer.svg" width="20" height="20" style="margin-right: 6px; vertical-align: middle;"> Submit Code Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Test on at least 2 different devices
5. Commit with clear messages: `git commit -m "feat: add haptic feedback to sliders"`
6. Push to your fork: `git push origin feature/my-feature`
7. Open a Pull Request with a clear description of your changes

#### Code Style Guidelines

- Follow [Google's Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use meaningful variable names (not `t`, `x`, `temp`)
- Add comments for complex logic
- Format code: `./gradlew ktlintFormat`
- Write unit tests for new features

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/wheelchair.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Accessibility

- Large touch targets (56dp+ per Material guidelines)
- Material 3 typography for readability
- High color contrast (neon on dark background)
- Device status descriptions
- Scrollable dialogs for smaller screens
- Adaptive layouts for different screen sizes
- Content descriptions for UI elements

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/scroll.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

### Open Source Libraries & Credits

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI Toolkit by Google
- [Material Design 3](https://m3.material.io/) - Design system by Google
- [Android Jetpack](https://developer.android.com/jetpack) - Android development libraries by Google
- [Kotlin](https://kotlinlang.org/) - Programming language by JetBrains

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/user-circle.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Author

**Developed by:** Swastik Chavan

- **GitHub:** [@swastik-chavan](https://github.com/swastik-chavan)
- **Portfolio:** [View all projects](https://github.com/swastik-chavan?tab=repositories)

### Special Thanks

Thanks to friends and beta testers who tested the app on their devices and provided valuable feedback!

---

## <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/chat-circle.svg" width="28" height="28" style="margin-right: 8px; vertical-align: middle;"> Getting Help

- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/book-open.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> **Documentation:** Check this README first
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/warning-circle.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> **Report Bugs:** [Open an issue](https://github.com/swastik-chavan/audio-booster/issues/new)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/chat-dots.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> **Ask Questions:** [GitHub Discussions](https://github.com/swastik-chavan/audio-booster/discussions)
- <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/link.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> **GitHub:** [Project Repository](https://github.com/swastik-chavan/audio-booster)

---

<div align="center">

Built with Kotlin • Jetpack Compose • Material 3

</div>

<div align="center">

Made with <img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/heart.svg" width="18" height="18" style="margin: 0 4px; vertical-align: middle;"> by **Swastik**

**[<img src="https://cdn.jsdelivr.net/gh/phosphor-icons/core@main/assets/Light/star.svg" width="18" height="18" style="margin-right: 4px; vertical-align: middle;"> Star us on GitHub](https://github.com/swastik-chavan/audio-booster)** if you find this useful!

</div>