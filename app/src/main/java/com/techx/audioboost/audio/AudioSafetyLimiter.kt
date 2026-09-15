package com.techx.audioboost.audio

import kotlin.math.roundToInt

/**
 * Real distortion protection and hardware-aware safety layer.
 * Coordinates with LoudnessEnhancer, Equalizer, and BassBoost to prevent:
 * - digital clipping
 * - harsh audio crackling
 * - excessive total gain
 * - blown phone speaker drivers or hearing fatigue
 */
class AudioSafetyLimiter {

    data class LimiterResult(
        val effectiveGainmB: Int,
        val limiterReductionmB: Int,
        val safeCeilingmB: Int,
        val isLimited: Boolean
    )

    /**
     * Safe gain ceilings (in millibels, where 1000 mB = 10 dB).
     * Distinguishes physical transducers to protect hardware and user hearing.
     */
    fun getSafeGainCeiling(deviceType: DeviceType, isUltraMode: Boolean): Int {
        return if (!isUltraMode) {
            when (deviceType) {
                DeviceType.SPEAKER -> 1800      // +18.0 dB max on phone speakers in normal mode
                DeviceType.WIRED_HEADSET -> 2000 // +20.0 dB max on headphones in normal mode
                DeviceType.BLUETOOTH -> 2000     // +20.0 dB max on Bluetooth
                DeviceType.USB -> 2000           // +20.0 dB max on USB DAC
                DeviceType.UNKNOWN -> 1600
            }
        } else {
            when (deviceType) {
                DeviceType.SPEAKER -> 3200      // Hard ceiling to prevent speaker voice coil blowout
                DeviceType.WIRED_HEADSET -> 3600 // Ear safety ceiling
                DeviceType.BLUETOOTH -> 4400     // Bluetooth amp ceiling
                DeviceType.USB -> 4600           // USB DAC output ceiling
                DeviceType.UNKNOWN -> 2800
            }
        }
    }

    /**
     * Computes the safe effective gain to pass to LoudnessEnhancer.
     *
     * @param requestedNormalGainmB base normal boost gain
     * @param requestedUltraGainmB additional ultra boost gain
     * @param eqBandLevels current EQ band levels in millibels
     * @param isEqEnabled whether EQ is active
     * @param bassBoostStrength current bass boost strength (0..1000)
     * @param isBassBoostEnabled whether bass boost is active
     * @param deviceType current audio output device
     * @param isLimiterEnabled whether distortion limiter is engaged
     * @param isSafetyLimitEnabled whether hardware device limits are enforced
     */
    fun computeSafeGain(
        requestedNormalGainmB: Int,
        requestedUltraGainmB: Int,
        eqBandLevels: List<Int>,
        isEqEnabled: Boolean,
        bassBoostStrength: Int,
        isBassBoostEnabled: Boolean,
        deviceType: DeviceType,
        isLimiterEnabled: Boolean,
        isSafetyLimitEnabled: Boolean
    ): LimiterResult {
        val totalRequestedGainmB = requestedNormalGainmB + requestedUltraGainmB
        if (totalRequestedGainmB <= 0) {
            return LimiterResult(
                effectiveGainmB = 0,
                limiterReductionmB = 0,
                safeCeilingmB = getSafeGainCeiling(deviceType, requestedUltraGainmB > 0),
                isLimited = false
            )
        }

        // Calculate energy contribution from positive EQ bands
        val maxPositiveEqmB = if (isEqEnabled) {
            eqBandLevels.filter { it > 0 }.maxOrNull() ?: 0
        } else {
            0
        }

        // Bass boost adds substantial energy in the 50-120Hz sub-bass range (up to +6..+12 dB)
        val bassBoostEnergyContributionmB = if (isBassBoostEnabled) {
            ((bassBoostStrength.coerceIn(0, 1000) / 1000f) * 700f).roundToInt()
        } else {
            0
        }

        val totalExtraEnergymB = maxPositiveEqmB + bassBoostEnergyContributionmB

        // Distortion reduction headroom compensation
        val limiterReductionmB = if (isLimiterEnabled && totalExtraEnergymB > 0) {
            // Scale reduction progressively based on requested gain to prevent clipping
            val scaleFactor = (totalRequestedGainmB / 5000f).coerceIn(0.4f, 0.9f)
            (totalExtraEnergymB * scaleFactor).roundToInt()
        } else {
            0
        }

        var candidateGainmB = (totalRequestedGainmB - limiterReductionmB).coerceAtLeast(0)

        // Apply hardware-aware safe limits
        val isUltra = requestedUltraGainmB > 0
        val safeCeilingmB = if (isSafetyLimitEnabled) {
            getSafeGainCeiling(deviceType, isUltra)
        } else {
            5000 // Absolute maximum
        }

        val wasCappedByHardware = candidateGainmB > safeCeilingmB
        val effectiveGainmB = candidateGainmB.coerceAtMost(safeCeilingmB)

        val totalReduction = (totalRequestedGainmB - effectiveGainmB).coerceAtLeast(0)

        return LimiterResult(
            effectiveGainmB = effectiveGainmB,
            limiterReductionmB = totalReduction,
            safeCeilingmB = safeCeilingmB,
            isLimited = isLimiterEnabled && (limiterReductionmB > 0 || wasCappedByHardware)
        )
    }
}
