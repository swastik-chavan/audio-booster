package com.techx.audioboost.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build

/**
 * Unified native audio effects engine.
 * Manages LoudnessEnhancer, Equalizer, BassBoost, and Virtualizer
 * across session 0 (global mix) and media-player sessions.
 */
@Suppress("DEPRECATION")
class AudioEngine(private val context: Context) {

    private data class SessionBundle(
        val sessionId: Int,
        var loudnessEnhancer: LoudnessEnhancer? = null,
        var equalizer: Equalizer? = null,
        var bassBoost: BassBoost? = null,
        var virtualizer: Virtualizer? = null
    ) {
        fun release() {
            try {
                loudnessEnhancer?.enabled = false
                loudnessEnhancer?.release()
            } catch (t: Throwable) {
            }
            try {
                equalizer?.enabled = false
                equalizer?.release()
            } catch (t: Throwable) {
            }
            try {
                bassBoost?.enabled = false
                bassBoost?.release()
            } catch (t: Throwable) {
            }
            try {
                virtualizer?.enabled = false
                virtualizer?.release()
            } catch (t: Throwable) {
            }
        }
    }

    private val sessions = mutableMapOf<Int, SessionBundle>()

    // Current target states
    private var currentTargetGainmB = 0
    private var isLoudnessEnabled = false

    private var isEqEnabled = true
    private var currentEqLevels = mutableListOf<Int>()

    private var isBassEnabled = false
    private var currentBassStrength = 0

    private var isVirtEnabled = false
    private var currentVirtStrength = 0

    // Capability probing
    val isLoudnessSupported: Boolean by lazy { probeLoudnessSupport() }
    val isEqualizerSupported: Boolean
    val equalizerBands: List<EqualizerBand>
    val eqMinLevelmB: Int
    val eqMaxLevelmB: Int

    val isBassBoostSupported: Boolean
    val isVirtualizerSupported: Boolean

    private val sessionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                if (sessionId == AudioEffect.ERROR || sessionId <= 0) return

                when (intent.action) {
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                        synchronized(sessions) {
                            if (!sessions.containsKey(sessionId)) {
                                createSessionBundle(sessionId)
                            }
                        }
                    }
                    AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                        synchronized(sessions) {
                            removeSessionBundle(sessionId)
                        }
                    }
                }
            } catch (t: Throwable) {
            }
        }
    }

    init {
        // Probe Equalizer capabilities
        var eqSupported = false
        val bandsList = mutableListOf<EqualizerBand>()
        var minLevel = -1500
        var maxLevel = 1500

        try {
            val testEq = Equalizer(0, 0)
            val numBands = testEq.numberOfBands.toInt()
            if (numBands > 0) {
                eqSupported = true
                val levelRange = testEq.bandLevelRange
                if (levelRange != null && levelRange.size >= 2) {
                    minLevel = levelRange[0].toInt()
                    maxLevel = levelRange[1].toInt()
                }
                for (b in 0 until numBands) {
                    val centerMilliHz = testEq.getCenterFreq(b.toShort())
                    val label = EqualizerBand.formatFrequency(centerMilliHz)
                    bandsList.add(EqualizerBand(bandIndex = b, centerFreqHz = centerMilliHz / 1000, label = label))
                }
            }
            testEq.release()
        } catch (t: Throwable) {
            eqSupported = false
        }

        isEqualizerSupported = eqSupported
        equalizerBands = if (bandsList.isNotEmpty()) {
            bandsList
        } else {
            // Fallback default 5-band labels if query failed
            listOf(
                EqualizerBand(0, 60, "60 Hz"),
                EqualizerBand(1, 230, "230 Hz"),
                EqualizerBand(2, 910, "910 Hz"),
                EqualizerBand(3, 3600, "3.6 kHz"),
                EqualizerBand(4, 14000, "14 kHz")
            )
        }
        eqMinLevelmB = minLevel
        eqMaxLevelmB = maxLevel

        // Initialize default levels
        currentEqLevels = MutableList(equalizerBands.size) { 0 }

        // Probe BassBoost
        isBassBoostSupported = try {
            val testBb = BassBoost(0, 0)
            val supported = testBb.strengthSupported
            testBb.release()
            supported
        } catch (t: Throwable) {
            false
        }

        // Probe Virtualizer
        isVirtualizerSupported = try {
            val testVirt = Virtualizer(0, 0)
            val supported = testVirt.strengthSupported
            testVirt.release()
            supported
        } catch (t: Throwable) {
            false
        }

        // Initialize session 0
        try {
            createSessionBundle(0)

            val filter = IntentFilter().apply {
                addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
                addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(sessionReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(sessionReceiver, filter)
            }
        } catch (t: Throwable) {
        }
    }

    private fun probeLoudnessSupport(): Boolean {
        return try {
            Class.forName("android.media.audiofx.LoudnessEnhancer")
            val hasEffect = AudioEffect.queryEffects().any {
                it.type == AudioEffect.EFFECT_TYPE_LOUDNESS_ENHANCER
            }
            if (hasEffect) {
                val dummy = LoudnessEnhancer(0)
                dummy.release()
                true
            } else {
                false
            }
        } catch (t: Throwable) {
            false
        }
    }

    private fun createSessionBundle(sessionId: Int) {
        if (sessionId < 0) return
        val bundle = SessionBundle(sessionId)

        // 1. LoudnessEnhancer
        if (isLoudnessSupported) {
            try {
                val enhancer = LoudnessEnhancer(sessionId)
                if (isLoudnessEnabled) {
                    enhancer.setTargetGain(currentTargetGainmB)
                    enhancer.enabled = true
                } else {
                    enhancer.enabled = false
                }
                bundle.loudnessEnhancer = enhancer
            } catch (t: Throwable) {
            }
        }

        // 2. Equalizer
        if (isEqualizerSupported) {
            try {
                val eq = Equalizer(0, sessionId)
                applyEqToInstance(eq)
                bundle.equalizer = eq
            } catch (t: Throwable) {
            }
        }

        // 3. BassBoost
        if (isBassBoostSupported) {
            try {
                val bb = BassBoost(0, sessionId)
                applyBassToInstance(bb)
                bundle.bassBoost = bb
            } catch (t: Throwable) {
            }
        }

        // 4. Virtualizer
        if (isVirtualizerSupported) {
            try {
                val virt = Virtualizer(0, sessionId)
                applyVirtToInstance(virt)
                bundle.virtualizer = virt
            } catch (t: Throwable) {
            }
        }

        sessions[sessionId] = bundle
    }

    private fun removeSessionBundle(sessionId: Int) {
        val bundle = sessions.remove(sessionId)
        bundle?.release()
    }

    private fun applyEqToInstance(eq: Equalizer) {
        try {
            val numBands = eq.numberOfBands.toInt()
            for (i in 0 until numBands) {
                if (i < currentEqLevels.size) {
                    val level = currentEqLevels[i].coerceIn(eqMinLevelmB, eqMaxLevelmB)
                    eq.setBandLevel(i.toShort(), level.toShort())
                }
            }
            eq.enabled = isEqEnabled
        } catch (t: Throwable) {
        }
    }

    private fun applyBassToInstance(bb: BassBoost) {
        try {
            if (bb.strengthSupported) {
                bb.setStrength(currentBassStrength.coerceIn(0, 1000).toShort())
            }
            bb.enabled = isBassEnabled && currentBassStrength > 0
        } catch (t: Throwable) {
        }
    }

    private fun applyVirtToInstance(virt: Virtualizer) {
        try {
            if (virt.strengthSupported) {
                virt.setStrength(currentVirtStrength.coerceIn(0, 1000).toShort())
            }
            virt.enabled = isVirtEnabled && currentVirtStrength > 0
        } catch (t: Throwable) {
        }
    }

    // Public control APIs
    fun setTargetGain(gainmB: Int) {
        currentTargetGainmB = gainmB
        isLoudnessEnabled = gainmB > 0
        synchronized(sessions) {
            for (bundle in sessions.values) {
                try {
                    val enhancer = bundle.loudnessEnhancer ?: continue
                    if (isLoudnessEnabled) {
                        enhancer.setTargetGain(gainmB)
                        enhancer.enabled = true
                    } else {
                        enhancer.enabled = false
                    }
                } catch (t: Throwable) {
                }
            }
        }
    }

    fun setEqualizerState(enabled: Boolean, bandLevels: List<Int>) {
        isEqEnabled = enabled
        currentEqLevels = bandLevels.toMutableList()
        synchronized(sessions) {
            for (bundle in sessions.values) {
                bundle.equalizer?.let { applyEqToInstance(it) }
            }
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        isBassEnabled = enabled
        currentBassStrength = strength
        synchronized(sessions) {
            for (bundle in sessions.values) {
                bundle.bassBoost?.let { applyBassToInstance(it) }
            }
        }
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        isVirtEnabled = enabled
        currentVirtStrength = strength
        synchronized(sessions) {
            for (bundle in sessions.values) {
                bundle.virtualizer?.let { applyVirtToInstance(it) }
            }
        }
    }

    fun getActiveSessionsCount(): Int {
        synchronized(sessions) {
            return sessions.size
        }
    }

    fun release() {
        try {
            context.unregisterReceiver(sessionReceiver)
        } catch (t: Throwable) {
        }
        synchronized(sessions) {
            for (bundle in sessions.values) {
                bundle.release()
            }
            sessions.clear()
        }
    }
}
