package com.techx.audioboost.audio

import com.techx.audioboost.model.AudioPreset

/**
 * Represents the current audio state of the device to be displayed in the UI.
 */
data class AudioState(
    val currentDeviceName: String = "Phone Speaker",
    val currentDeviceType: DeviceType = DeviceType.SPEAKER,
    val systemVolumePercent: Int = 0,
    val boostPercent: Int = 0,
    val isBoostSupported: Boolean = true,
    val loudnessGainmB: Int = 0,
    val isUltraModeEnabled: Boolean = false,
    val ultraBoostPercent: Int = 0,
    val isUltraModeLocked: Boolean = true,

    // Equalizer
    val isEqualizerSupported: Boolean = true,
    val isEqEnabled: Boolean = true,
    val eqBands: List<EqualizerBand> = emptyList(),
    val eqBandLevels: List<Int> = listOf(0, 0, 0, 0, 0),
    val eqMinLevelmB: Int = -1500,
    val eqMaxLevelmB: Int = 1500,

    // Bass Boost
    val isBassBoostSupported: Boolean = true,
    val isBassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0..1000

    // Virtualizer
    val isVirtualizerSupported: Boolean = true,
    val isVirtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0..1000

    // Limiter & Safety
    val isLimiterEnabled: Boolean = true,
    val isSafetyLimitActive: Boolean = true,
    val limiterReductionmB: Int = 0,
    val safeLimitCeilingmB: Int = 3000,

    // Presets
    val activePresetId: String = AudioPreset.FLAT.id,
    val activePresetName: String = AudioPreset.FLAT.name,
    val presetsList: List<AudioPreset> = AudioPreset.BUILT_IN_PRESETS,

    // Diagnostics / System
    val activeSessionsCount: Int = 1,
    val isMediaPlaying: Boolean = false,
    val isTransitioning: Boolean = false
)

/**
 * Band metadata for Equalizer.
 */
data class EqualizerBand(
    val bandIndex: Int,
    val centerFreqHz: Int,
    val label: String
) {
    companion object {
        fun formatFrequency(milliHz: Int): String {
            val hz = milliHz / 1000
            return if (hz >= 1000) {
                val kHz = hz / 1000f
                if (kHz % 1f == 0f) {
                    "${kHz.toInt()} kHz"
                } else {
                    String.format("%.1f kHz", kHz)
                }
            } else {
                "$hz Hz"
            }
        }
    }
}

/**
 * Supported audio output device types for rendering specific icons.
 */
enum class DeviceType {
    SPEAKER,
    WIRED_HEADSET,
    BLUETOOTH,
    USB,
    UNKNOWN
}
