package com.techx.audioboost.audio

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
    val isUltraModeLocked: Boolean = true
)

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
