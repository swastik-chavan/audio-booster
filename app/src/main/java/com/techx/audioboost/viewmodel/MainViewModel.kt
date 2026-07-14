package com.techx.audioboost.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.techx.audioboost.audio.AudioController
import com.techx.audioboost.audio.AudioState
import com.techx.audioboost.audio.LoudnessController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val audioController = AudioController(application)
    private val loudnessController = LoudnessController(application)

    private val _boostPercent = MutableStateFlow(0)
    private val _isUltraModeEnabled = MutableStateFlow(false)
    private val _ultraBoostPercent = MutableStateFlow(0)

    var hasShownUltraWarning = false

    val uiState: StateFlow<AudioState> = combine(
        audioController.volumeFlow,
        audioController.deviceFlow,
        _boostPercent,
        _isUltraModeEnabled,
        _ultraBoostPercent
    ) { volume, device, boost, ultraEnabled, ultraPercent ->
        val normalGain = calculateNormalGain(boost)
        val ultraGain = if (ultraEnabled) calculateUltraGain(ultraPercent) else 0
        val combinedGain = (normalGain + ultraGain).coerceAtMost(5000)

        AudioState(
            currentDeviceName = device.first,
            currentDeviceType = device.second,
            systemVolumePercent = volume,
            boostPercent = boost,
            isBoostSupported = loudnessController.isSupported,
            loudnessGainmB = combinedGain,
            isUltraModeEnabled = ultraEnabled,
            ultraBoostPercent = ultraPercent,
            isUltraModeLocked = boost == 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AudioState(
            currentDeviceName = audioController.getCurrentDevice().first,
            currentDeviceType = audioController.getCurrentDevice().second,
            systemVolumePercent = audioController.getMediaVolumePercent(),
            boostPercent = 0,
            isBoostSupported = loudnessController.isSupported,
            loudnessGainmB = 0,
            isUltraModeEnabled = false,
            ultraBoostPercent = 0,
            isUltraModeLocked = true
        )
    )

    fun calculateNormalGain(percent: Int): Int {
        return (percent.coerceIn(0, 100) * 2000) / 100
    }

    fun calculateUltraGain(percent: Int): Int {
        val x = percent.coerceIn(0, 100) / 100.0
        val factor = x * x * x
        val maxUltraGainmB = 3000
        return (factor * maxUltraGainmB).toInt()
    }

    private fun updateFinalGain() {
        val normalGain = calculateNormalGain(_boostPercent.value)
        val ultraGain = if (_isUltraModeEnabled.value) calculateUltraGain(_ultraBoostPercent.value) else 0
        val totalGain = (normalGain + ultraGain).coerceAtMost(5000)
        loudnessController.setTargetGain(totalGain)
    }

    fun updateSystemVolume(percent: Int) {
        audioController.setMediaVolumePercent(percent)
    }

    fun updateBoostPercent(percent: Int) {
        if (!loudnessController.isSupported) return
        _boostPercent.value = percent
        if (percent == 0) {
            _isUltraModeEnabled.value = false
        }
        updateFinalGain()
    }

    fun toggleUltraMode(enabled: Boolean) {
        if (!loudnessController.isSupported) return
        if (_boostPercent.value == 0 && enabled) return
        _isUltraModeEnabled.value = enabled
        updateFinalGain()
    }

    fun updateUltraBoostPercent(percent: Int) {
        if (!loudnessController.isSupported) return
        _ultraBoostPercent.value = percent
        updateFinalGain()
    }

    fun onActivityDestroyed(isChangingConfigurations: Boolean) {
        if (!isChangingConfigurations) {
            loudnessController.release()
            audioController.release()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
        loudnessController.release()
    }
}
