package com.techx.audioboost.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages smooth audio gain transitions during hardware output changes
 * (e.g. headphone unplug, Bluetooth disconnect) to prevent dangerous audio blasts.
 */
class DeviceTransitionManager(
    private val scope: CoroutineScope,
    private val onGainRamp: (targetGainmB: Int) -> Unit
) {

    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    private var transitionJob: Job? = null

    /**
     * Executes a smooth gain transition when an audio routing change is detected.
     *
     * @param currentGainmB the starting gain level before the switch
     * @param targetGainmB the new safe gain level to reach after the switch
     */
    fun handleDeviceChange(currentGainmB: Int, targetGainmB: Int) {
        transitionJob?.cancel()
        transitionJob = scope.launch(Dispatchers.Default) {
            _isTransitioning.value = true

            // 1. Rapidly attenuate down to prevent audio spike (100ms)
            if (currentGainmB > 0) {
                val stepDown = currentGainmB / 3
                onGainRamp(stepDown)
                delay(50)
                onGainRamp(0)
                delay(80)
            }

            // 2. Smoothly ramp up to the new target gain (200ms in 4 steps)
            if (targetGainmB > 0) {
                val steps = 4
                for (i in 1..steps) {
                    val intermediateGain = (targetGainmB * i) / steps
                    onGainRamp(intermediateGain)
                    delay(50)
                }
            } else {
                onGainRamp(0)
            }

            _isTransitioning.value = false
        }
    }

    fun cancel() {
        transitionJob?.cancel()
        _isTransitioning.value = false
    }
}
