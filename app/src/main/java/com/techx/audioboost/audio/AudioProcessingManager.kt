package com.techx.audioboost.audio

import android.app.Application
import android.content.Context
import com.techx.audioboost.data.SettingsRepository
import com.techx.audioboost.model.AudioPreset
import com.techx.audioboost.service.AudioBoostTileService
import com.techx.audioboost.service.AudioService
import com.techx.audioboost.util.AppProfileManager
import com.techx.audioboost.util.HapticManager
import com.techx.audioboost.widget.AudioBoostWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Internal state representation for audio effects and profiles.
 */
data class InternalEffectsState(
    val boostPercent: Int = 0,
    val isUltra: Boolean = false,
    val ultraPercent: Int = 0,
    val isEqEnabled: Boolean = true,
    val eqBands: List<Int> = emptyList(),
    val isBassEnabled: Boolean = false,
    val bassStrength: Int = 0,
    val isVirtEnabled: Boolean = false,
    val virtStrength: Int = 0,
    val isLimiterEnabled: Boolean = true,
    val isSafetyLimitEnabled: Boolean = true,
    val activePresetId: String = AudioPreset.FLAT.id,
    val allPresets: List<AudioPreset> = AudioPreset.BUILT_IN_PRESETS
)

/**
 * Centralized Audio Processing Manager.
 * Ensures that all control paths (UI, Widgets, Quick Settings Tile, Notification, App Profiles)
 * route through the exact same audio engine and centralized safety limiter.
 */
class AudioProcessingManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AudioProcessingManager? = null

        fun getInstance(context: Context): AudioProcessingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioProcessingManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val settingsRepository = SettingsRepository(context)
    val audioEngine = AudioEngine(context)
    val safetyLimiter = AudioSafetyLimiter()
    val hapticManager = HapticManager(context)
    val appProfileManager = AppProfileManager(context)

    private val _effectsState = MutableStateFlow(
        InternalEffectsState(
            eqBands = List(audioEngine.equalizerBands.size) { 0 }
        )
    )
    val effectsState: StateFlow<InternalEffectsState> = _effectsState.asStateFlow()

    private val deviceTransitionManager = DeviceTransitionManager(scope) { rampGainmB ->
        audioEngine.setTargetGain(rampGainmB)
    }

    val audioController = AudioController(context) { newDevice ->
        val fx = _effectsState.value
        val currentGain = currentEffectiveGainmB
        val targetSafeGain = safetyLimiter.computeSafeGain(
            requestedNormalGainmB = calculateNormalGain(fx.boostPercent),
            requestedUltraGainmB = if (fx.isUltra) calculateUltraGain(fx.ultraPercent) else 0,
            eqBandLevels = fx.eqBands,
            isEqEnabled = fx.isEqEnabled,
            bassBoostStrength = fx.bassStrength,
            isBassBoostEnabled = fx.isBassEnabled,
            deviceType = newDevice.second,
            isLimiterEnabled = fx.isLimiterEnabled,
            isSafetyLimitEnabled = fx.isSafetyLimitEnabled
        ).effectiveGainmB
        deviceTransitionManager.handleDeviceChange(currentGain, targetSafeGain)
        syncExternalControls()
    }

    val autoOffManager = AutoOffManager(context, scope) {
        setBoostPercent(0)
    }

    var hasShownUltraWarning = false
    private var isNotificationEnabled: Boolean = true
    private var currentEffectiveGainmB = 0
    private var lastActivePresetBeforeAppProfile: String? = null

    init {
        scope.launch {
            loadSettingsAndInit()
        }
    }

    private suspend fun loadSettingsAndInit() {
        val boost = settingsRepository.boostPercent.first()
        val isUltra = settingsRepository.isUltraEnabled.first()
        val ultraPercent = settingsRepository.ultraBoostPercent.first()
        val warningShown = settingsRepository.hasShownUltraWarning.first()

        val isEq = settingsRepository.isEqEnabled.first()
        val eqBands = settingsRepository.eqBandLevels.first()

        val isBass = settingsRepository.isBassBoostEnabled.first()
        val bassStrength = settingsRepository.bassBoostStrength.first()

        val isVirt = settingsRepository.isVirtualizerEnabled.first()
        val virtStrength = settingsRepository.virtualizerStrength.first()

        val isLimiter = settingsRepository.isLimiterEnabled.first()
        val isSafety = settingsRepository.safetyLimitEnabled.first()

        val presetId = settingsRepository.activePresetId.first()
        val customs = settingsRepository.customPresets.first()

        val haptics = settingsRepository.hapticsEnabled.first()
        val autoOff = settingsRepository.autoOffMinutes.first()
        val notifEnabled = settingsRepository.notificationEnabled.first()

        hasShownUltraWarning = warningShown
        isNotificationEnabled = notifEnabled
        hapticManager.isEnabled = haptics
        autoOffManager.setAutoOffMinutes(autoOff)

        val resolvedBands = if (eqBands.size >= audioEngine.equalizerBands.size) {
            eqBands
        } else {
            List(audioEngine.equalizerBands.size) { 0 }
        }

        _effectsState.value = InternalEffectsState(
            boostPercent = boost,
            isUltra = isUltra,
            ultraPercent = ultraPercent,
            isEqEnabled = isEq,
            eqBands = resolvedBands,
            isBassEnabled = isBass,
            bassStrength = bassStrength,
            isVirtEnabled = isVirt,
            virtStrength = virtStrength,
            isLimiterEnabled = isLimiter,
            isSafetyLimitEnabled = isSafety,
            activePresetId = presetId,
            allPresets = AudioPreset.BUILT_IN_PRESETS + customs
        )

        // Monitor app profiles if enabled and granted
        scope.launch {
            settingsRepository.appProfilesEnabled.collect { enabled ->
                if (enabled && appProfileManager.hasUsageAccess()) {
                    val profiles = settingsRepository.appProfiles.first()
                    appProfileManager.startMonitoring(scope) { fgPackage ->
                        handleAppForegroundChanged(fgPackage, profiles)
                    }
                } else {
                    appProfileManager.stopMonitoring()
                }
            }
        }

        applyEngineSettings()
    }

    fun calculateNormalGain(percent: Int): Int {
        return (percent.coerceIn(0, 100) * 2000) / 100
    }

    fun calculateUltraGain(percent: Int): Int {
        val x = percent.coerceIn(0, 100) / 100.0
        val factor = x * x * x
        val maxUltraGainmB = 3000
        return (factor * maxUltraGainmB).toInt()
    }

    fun applyEngineSettings() {
        val fx = _effectsState.value
        val normalGain = calculateNormalGain(fx.boostPercent)
        val ultraGain = if (fx.isUltra) calculateUltraGain(fx.ultraPercent) else 0

        val limiterResult = safetyLimiter.computeSafeGain(
            requestedNormalGainmB = normalGain,
            requestedUltraGainmB = ultraGain,
            eqBandLevels = fx.eqBands,
            isEqEnabled = fx.isEqEnabled,
            bassBoostStrength = fx.bassStrength,
            isBassBoostEnabled = fx.isBassEnabled,
            deviceType = audioController.getCurrentDevice().second,
            isLimiterEnabled = fx.isLimiterEnabled,
            isSafetyLimitEnabled = fx.isSafetyLimitEnabled
        )

        currentEffectiveGainmB = limiterResult.effectiveGainmB
        audioEngine.setTargetGain(limiterResult.effectiveGainmB)
        audioEngine.setEqualizerState(fx.isEqEnabled, fx.eqBands)
        audioEngine.setBassBoost(fx.isBassEnabled, fx.bassStrength)
        audioEngine.setVirtualizer(fx.isVirtEnabled, fx.virtStrength)

        syncExternalControls()
    }

    fun setNotificationEnabled(enabled: Boolean) {
        isNotificationEnabled = enabled
        syncExternalControls()
    }

    private fun syncExternalControls() {
        val fx = _effectsState.value
        val gainDb = currentEffectiveGainmB / 100f
        val presetName = fx.allPresets.firstOrNull { it.id == fx.activePresetId }?.name ?: "Flat"
        val deviceName = audioController.getCurrentDevice().first

        // Sync Ongoing Notification
        if (fx.boostPercent > 0 && isNotificationEnabled) {
            AudioService.updateNotification(
                context,
                fx.boostPercent,
                fx.isUltra,
                gainDb,
                presetName,
                deviceName
            )
        } else {
            AudioService.stopService(context)
        }

        // Sync Quick Settings Tile
        AudioBoostTileService.requestUpdate(context)

        // Sync Home Screen Widgets
        AudioBoostWidgetProvider.updateAllWidgets(
            context,
            fx.boostPercent,
            fx.isUltra,
            deviceName,
            gainDb
        )
    }

    fun setBoostPercent(percent: Int) {
        if (!audioEngine.isLoudnessSupported) return
        val prev = _effectsState.value.boostPercent
        val isUltra = if (percent == 0) false else _effectsState.value.isUltra

        _effectsState.update { it.copy(boostPercent = percent, isUltra = isUltra) }

        if ((prev < 100 && percent >= 100) || (prev >= 100 && percent < 100)) {
            hapticManager.performUltraThresholdClick()
        } else if (percent / 10 != prev / 10) {
            hapticManager.performStepTick()
        }
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveBoost(percent, isUltra, _effectsState.value.ultraPercent)
        }
    }

    fun toggleUltraMode(enabled: Boolean) {
        if (!audioEngine.isLoudnessSupported) return
        if (_effectsState.value.boostPercent == 0 && enabled) return

        _effectsState.update { it.copy(isUltra = enabled) }

        if (enabled) {
            hapticManager.performUltraThresholdClick()
        } else {
            hapticManager.performToggleClick()
        }
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveBoost(_effectsState.value.boostPercent, enabled, _effectsState.value.ultraPercent)
            if (hasShownUltraWarning) {
                settingsRepository.setHasShownUltraWarning(true)
            }
        }
    }

    fun setUltraBoostPercent(percent: Int) {
        if (!audioEngine.isLoudnessSupported) return
        val prev = _effectsState.value.ultraPercent

        _effectsState.update { it.copy(ultraPercent = percent) }

        if (percent / 10 != prev / 10) {
            hapticManager.performStepTick()
        }
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveBoost(_effectsState.value.boostPercent, _effectsState.value.isUltra, percent)
        }
    }

    fun setEqualizerBand(bandIndex: Int, levelmB: Int) {
        val bands = _effectsState.value.eqBands.toMutableList()
        if (bandIndex in bands.indices) {
            bands[bandIndex] = levelmB
            _effectsState.update { it.copy(eqBands = bands) }
            applyEngineSettings()
            scope.launch {
                settingsRepository.saveEqualizer(_effectsState.value.isEqEnabled, bands)
            }
        }
    }

    fun toggleEqualizer(enabled: Boolean) {
        _effectsState.update { it.copy(isEqEnabled = enabled) }
        hapticManager.performToggleClick()
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveEqualizer(enabled, _effectsState.value.eqBands)
        }
    }

    fun resetEqualizerToFlat() {
        val flatBands = List(audioEngine.equalizerBands.size) { 0 }
        _effectsState.update { it.copy(eqBands = flatBands) }
        hapticManager.performStepTick()
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveEqualizer(_effectsState.value.isEqEnabled, flatBands)
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        _effectsState.update { it.copy(isBassEnabled = enabled, bassStrength = strength) }
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveBassBoost(enabled, strength)
        }
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        _effectsState.update { it.copy(isVirtEnabled = enabled, virtStrength = strength) }
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveVirtualizer(enabled, strength)
        }
    }

    fun toggleLimiter(enabled: Boolean) {
        _effectsState.update { it.copy(isLimiterEnabled = enabled) }
        hapticManager.performToggleClick()
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveLimiterSettings(enabled, _effectsState.value.isSafetyLimitEnabled)
        }
    }

    fun toggleSafetyLimit(enabled: Boolean) {
        _effectsState.update { it.copy(isSafetyLimitEnabled = enabled) }
        hapticManager.performToggleClick()
        applyEngineSettings()
        scope.launch {
            settingsRepository.saveLimiterSettings(_effectsState.value.isLimiterEnabled, enabled)
        }
    }

    fun applyPreset(preset: AudioPreset) {
        val mappedBands = if (preset.bandLevels.size == audioEngine.equalizerBands.size) {
            preset.bandLevels
        } else {
            List(audioEngine.equalizerBands.size) { i ->
                if (i < preset.bandLevels.size) preset.bandLevels[i] else 0
            }
        }

        _effectsState.update {
            it.copy(
                activePresetId = preset.id,
                boostPercent = preset.boostPercent,
                isUltra = preset.isUltraEnabled,
                ultraPercent = preset.ultraBoostPercent,
                eqBands = mappedBands,
                isBassEnabled = preset.bassBoostEnabled,
                bassStrength = preset.bassBoostStrength,
                isVirtEnabled = preset.virtualizerEnabled,
                virtStrength = preset.virtualizerStrength,
                isLimiterEnabled = preset.limiterEnabled
            )
        }

        hapticManager.performStepTick()
        applyEngineSettings()

        scope.launch {
            settingsRepository.saveBoost(preset.boostPercent, preset.isUltraEnabled, preset.ultraBoostPercent)
            settingsRepository.saveEqualizer(_effectsState.value.isEqEnabled, mappedBands)
            settingsRepository.saveBassBoost(preset.bassBoostEnabled, preset.bassBoostStrength)
            settingsRepository.saveVirtualizer(preset.virtualizerEnabled, preset.virtualizerStrength)
            settingsRepository.saveLimiterSettings(preset.limiterEnabled, _effectsState.value.isSafetyLimitEnabled)
            settingsRepository.setActivePresetId(preset.id)
        }
    }

    fun handleAppForegroundChanged(packageName: String?, configuredProfiles: List<com.techx.audioboost.model.AppProfile>) {
        if (packageName == null) return
        val matchingProfile = configuredProfiles.firstOrNull { it.packageName == packageName }

        if (matchingProfile != null) {
            val targetPreset = _effectsState.value.allPresets.firstOrNull { it.id == matchingProfile.presetId }
            if (targetPreset != null && _effectsState.value.activePresetId != targetPreset.id) {
                if (lastActivePresetBeforeAppProfile == null) {
                    lastActivePresetBeforeAppProfile = _effectsState.value.activePresetId
                }
                applyPreset(targetPreset)
            }
        } else if (lastActivePresetBeforeAppProfile != null) {
            // Restore previous preset when leaving profiled app
            val previousPreset = _effectsState.value.allPresets.firstOrNull { it.id == lastActivePresetBeforeAppProfile }
            if (previousPreset != null) {
                applyPreset(previousPreset)
            }
            lastActivePresetBeforeAppProfile = null
        }
    }

    fun saveCustomPreset(name: String) {
        val fx = _effectsState.value
        val newPreset = AudioPreset(
            id = "custom_${java.util.UUID.randomUUID()}",
            name = name.trim(),
            isCustom = true,
            boostPercent = fx.boostPercent,
            isUltraEnabled = fx.isUltra,
            ultraBoostPercent = fx.ultraPercent,
            bandLevels = fx.eqBands,
            bassBoostEnabled = fx.isBassEnabled,
            bassBoostStrength = fx.bassStrength,
            virtualizerEnabled = fx.isVirtEnabled,
            virtualizerStrength = fx.virtStrength,
            limiterEnabled = fx.isLimiterEnabled
        )
        val currentCustoms = fx.allPresets.filter { it.isCustom }
        val updatedPresets = AudioPreset.BUILT_IN_PRESETS + currentCustoms + newPreset

        _effectsState.update {
            it.copy(
                allPresets = updatedPresets,
                activePresetId = newPreset.id
            )
        }

        hapticManager.performToggleClick()
        scope.launch {
            settingsRepository.saveCustomPreset(newPreset, currentCustoms)
        }
    }

    fun deleteCustomPreset(presetId: String) {
        val fx = _effectsState.value
        val currentCustoms = fx.allPresets.filter { it.isCustom }
        val updatedPresets = fx.allPresets.filter { it.id != presetId }
        val newActiveId = if (fx.activePresetId == presetId) AudioPreset.FLAT.id else fx.activePresetId

        _effectsState.update {
            it.copy(
                allPresets = updatedPresets,
                activePresetId = newActiveId
            )
        }

        scope.launch {
            settingsRepository.deleteCustomPreset(presetId, currentCustoms)
        }
    }

    fun resetAllSettings() {
        scope.launch {
            settingsRepository.resetAllSettings()
            val flatBands = List(audioEngine.equalizerBands.size) { 0 }
            _effectsState.value = InternalEffectsState(
                boostPercent = 0,
                isUltra = false,
                ultraPercent = 0,
                isEqEnabled = true,
                eqBands = flatBands,
                isBassEnabled = false,
                bassStrength = 0,
                isVirtEnabled = false,
                virtStrength = 0,
                isLimiterEnabled = true,
                isSafetyLimitEnabled = true,
                activePresetId = AudioPreset.FLAT.id,
                allPresets = AudioPreset.BUILT_IN_PRESETS
            )
            applyEngineSettings()
        }
    }

    fun release() {
        audioEngine.release()
        audioController.release()
        autoOffManager.release()
        deviceTransitionManager.cancel()
    }
}
