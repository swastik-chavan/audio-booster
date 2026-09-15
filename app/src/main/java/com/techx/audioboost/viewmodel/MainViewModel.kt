package com.techx.audioboost.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.techx.audioboost.audio.AudioProcessingManager
import com.techx.audioboost.audio.AudioState
import com.techx.audioboost.model.AppProfile
import com.techx.audioboost.model.AudioPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val manager = AudioProcessingManager.getInstance(application)
    val settingsRepository = manager.settingsRepository
    val audioEngine = manager.audioEngine
    val safetyLimiter = manager.safetyLimiter
    val hapticManager = manager.hapticManager
    val appProfileManager = manager.appProfileManager

    var hasShownUltraWarning: Boolean
        get() = manager.hasShownUltraWarning
        set(value) {
            manager.hasShownUltraWarning = value
        }

    val uiState: StateFlow<AudioState> = combine(
        manager.audioController.volumeFlow,
        manager.audioController.deviceFlow,
        manager.effectsState
    ) { volume, device, fx ->
        val normalGain = manager.calculateNormalGain(fx.boostPercent)
        val ultraGain = if (fx.isUltra) manager.calculateUltraGain(fx.ultraPercent) else 0

        val limiterResult = safetyLimiter.computeSafeGain(
            requestedNormalGainmB = normalGain,
            requestedUltraGainmB = ultraGain,
            eqBandLevels = fx.eqBands,
            isEqEnabled = fx.isEqEnabled,
            bassBoostStrength = fx.bassStrength,
            isBassBoostEnabled = fx.isBassEnabled,
            deviceType = device.second,
            isLimiterEnabled = fx.isLimiterEnabled,
            isSafetyLimitEnabled = fx.isSafetyLimitEnabled
        )

        val activePreset = fx.allPresets.firstOrNull { it.id == fx.activePresetId }
            ?: AudioPreset.FLAT

        AudioState(
            currentDeviceName = device.first,
            currentDeviceType = device.second,
            systemVolumePercent = volume,
            boostPercent = fx.boostPercent,
            isBoostSupported = audioEngine.isLoudnessSupported,
            loudnessGainmB = limiterResult.effectiveGainmB,
            isUltraModeEnabled = fx.isUltra,
            ultraBoostPercent = fx.ultraPercent,
            isUltraModeLocked = fx.boostPercent == 0,

            isEqualizerSupported = audioEngine.isEqualizerSupported,
            isEqEnabled = fx.isEqEnabled,
            eqBands = audioEngine.equalizerBands,
            eqBandLevels = fx.eqBands,
            eqMinLevelmB = audioEngine.eqMinLevelmB,
            eqMaxLevelmB = audioEngine.eqMaxLevelmB,

            isBassBoostSupported = audioEngine.isBassBoostSupported,
            isBassBoostEnabled = fx.isBassEnabled,
            bassBoostStrength = fx.bassStrength,

            isVirtualizerSupported = audioEngine.isVirtualizerSupported,
            isVirtualizerEnabled = fx.isVirtEnabled,
            virtualizerStrength = fx.virtStrength,

            isLimiterEnabled = fx.isLimiterEnabled,
            isSafetyLimitActive = fx.isSafetyLimitEnabled,
            limiterReductionmB = limiterResult.limiterReductionmB,
            safeLimitCeilingmB = limiterResult.safeCeilingmB,

            activePresetId = activePreset.id,
            activePresetName = activePreset.name,
            presetsList = fx.allPresets,

            activeSessionsCount = audioEngine.getActiveSessionsCount(),
            isMediaPlaying = manager.autoOffManager.isMediaPlaying.value,
            isTransitioning = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AudioState(
            currentDeviceName = manager.audioController.getCurrentDevice().first,
            currentDeviceType = manager.audioController.getCurrentDevice().second,
            systemVolumePercent = manager.audioController.getMediaVolumePercent(),
            boostPercent = manager.effectsState.value.boostPercent,
            isBoostSupported = audioEngine.isLoudnessSupported,
            eqBands = audioEngine.equalizerBands
        )
    )

    fun updateSystemVolume(percent: Int) {
        manager.audioController.setMediaVolumePercent(percent)
    }

    fun updateBoostPercent(percent: Int) {
        manager.setBoostPercent(percent)
    }

    fun toggleUltraMode(enabled: Boolean) {
        manager.toggleUltraMode(enabled)
    }

    fun updateUltraBoostPercent(percent: Int) {
        manager.setUltraBoostPercent(percent)
    }

    fun updateEqualizerBand(bandIndex: Int, levelmB: Int) {
        manager.setEqualizerBand(bandIndex, levelmB)
    }

    fun toggleEqualizer(enabled: Boolean) {
        manager.toggleEqualizer(enabled)
    }

    fun resetEqualizerToFlat() {
        manager.resetEqualizerToFlat()
    }

    fun updateBassBoost(enabled: Boolean, strength: Int) {
        manager.setBassBoost(enabled, strength)
    }

    fun updateVirtualizer(enabled: Boolean, strength: Int) {
        manager.setVirtualizer(enabled, strength)
    }

    fun toggleLimiter(enabled: Boolean) {
        manager.toggleLimiter(enabled)
    }

    fun toggleSafetyLimit(enabled: Boolean) {
        manager.toggleSafetyLimit(enabled)
    }

    fun applyPreset(preset: AudioPreset) {
        manager.applyPreset(preset)
    }

    fun saveCustomPreset(name: String) {
        manager.saveCustomPreset(name)
    }

    fun deleteCustomPreset(presetId: String) {
        manager.deleteCustomPreset(presetId)
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        manager.hapticManager.isEnabled = enabled
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }

    fun setAutoOffMinutes(minutes: Int) {
        manager.autoOffManager.setAutoOffMinutes(minutes)
        viewModelScope.launch {
            settingsRepository.setAutoOffMinutes(minutes)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        manager.setNotificationEnabled(enabled)
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(enabled)
        }
    }

    fun setAppProfilesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAppProfilesEnabled(enabled)
        }
    }

    fun saveAppProfile(profile: AppProfile, currentProfiles: List<AppProfile>) {
        viewModelScope.launch {
            settingsRepository.saveAppProfile(profile, currentProfiles)
        }
    }

    fun deleteAppProfile(packageName: String, currentProfiles: List<AppProfile>) {
        viewModelScope.launch {
            settingsRepository.deleteAppProfile(packageName, currentProfiles)
        }
    }

    fun resetAllSettings() {
        manager.resetAllSettings()
    }

    fun onActivityDestroyed(isChangingConfigurations: Boolean) {
        if (!isChangingConfigurations && manager.effectsState.value.boostPercent == 0) {
            manager.release()
        }
    }
}
