package com.techx.audioboost

import com.techx.audioboost.audio.AudioSafetyLimiter
import com.techx.audioboost.audio.DeviceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioSafetyLimiterTest {

    private val limiter = AudioSafetyLimiter()

    @Test
    fun `when boost is zero effective gain is zero`() {
        val result = limiter.computeSafeGain(
            requestedNormalGainmB = 0,
            requestedUltraGainmB = 0,
            eqBandLevels = listOf(0, 0, 0, 0, 0),
            isEqEnabled = true,
            bassBoostStrength = 0,
            isBassBoostEnabled = false,
            deviceType = DeviceType.SPEAKER,
            isLimiterEnabled = true,
            isSafetyLimitEnabled = true
        )
        assertEquals(0, result.effectiveGainmB)
        assertEquals(0, result.limiterReductionmB)
        assertEquals(false, result.isLimited)
    }

    @Test
    fun `speaker output ceiling caps excessive ultra gain`() {
        val speakerCeiling = limiter.getSafeGainCeiling(DeviceType.SPEAKER, isUltraMode = true)
        val result = limiter.computeSafeGain(
            requestedNormalGainmB = 2000,
            requestedUltraGainmB = 3000, // Total 5000 mB
            eqBandLevels = listOf(0, 0, 0, 0, 0),
            isEqEnabled = false,
            bassBoostStrength = 0,
            isBassBoostEnabled = false,
            deviceType = DeviceType.SPEAKER,
            isLimiterEnabled = true,
            isSafetyLimitEnabled = true
        )
        assertTrue("Gain should be capped at speaker safe ceiling", result.effectiveGainmB <= speakerCeiling)
        assertTrue("Limiter should be active when capped", result.isLimited)
    }

    @Test
    fun `high positive EQ and bass boost triggers dynamic headroom reduction`() {
        val highBoostEq = listOf(1000, 800, 500, 200, 0) // +10 dB on bass
        val result = limiter.computeSafeGain(
            requestedNormalGainmB = 1500,
            requestedUltraGainmB = 0,
            eqBandLevels = highBoostEq,
            isEqEnabled = true,
            bassBoostStrength = 800, // high bass boost
            isBassBoostEnabled = true,
            deviceType = DeviceType.BLUETOOTH,
            isLimiterEnabled = true,
            isSafetyLimitEnabled = true
        )
        assertTrue("Limiter reduction should be greater than 0 to preserve headroom", result.limiterReductionmB > 0)
        assertTrue("Effective gain should be less than requested to avoid clipping", result.effectiveGainmB < 1500)
    }
}
