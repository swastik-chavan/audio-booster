package com.techx.audioboost.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.os.Build

class LoudnessController(private val context: Context) {

    private val enhancers = mutableMapOf<Int, LoudnessEnhancer>()
    private var targetGainmB: Int = 0
    private var isEnabled: Boolean = false

    val isSupported: Boolean by lazy {
        checkLoudnessEnhancerSupport()
    }

    private val audioSessionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                if (sessionId == AudioEffect.ERROR || sessionId <= 0) return

                when (intent.action) {
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                        synchronized(enhancers) {
                            if (!enhancers.containsKey(sessionId)) {
                                createAndApplyEnhancer(sessionId)
                            }
                        }
                    }
                    AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                        synchronized(enhancers) {
                            removeAndReleaseEnhancer(sessionId)
                        }
                    }
                }
            } catch (t: Throwable) {
            }
        }
    }

    init {
        try {
            if (isSupported) {
                createAndApplyEnhancer(0)

                val filter = IntentFilter().apply {
                    addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
                    addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(audioSessionReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(audioSessionReceiver, filter)
                }
            }
        } catch (t: Throwable) {
        }
    }

    private fun checkLoudnessEnhancerSupport(): Boolean {
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

    fun setTargetGain(gainmB: Int) {
        targetGainmB = gainmB
        isEnabled = gainmB > 0
        synchronized(enhancers) {
            for (enhancer in enhancers.values) {
                try {
                    if (isEnabled) {
                        enhancer.setTargetGain(targetGainmB)
                        enhancer.enabled = true
                    } else {
                        enhancer.enabled = false
                    }
                } catch (t: Throwable) {
                }
            }
        }
    }

    fun getTargetGain(): Int = targetGainmB

    fun isBoostEnabled(): Boolean = isEnabled

    private fun createAndApplyEnhancer(sessionId: Int) {
        if (sessionId < 0) return
        try {
            val enhancer = LoudnessEnhancer(sessionId)
            if (isEnabled) {
                enhancer.setTargetGain(targetGainmB)
                enhancer.enabled = true
            } else {
                enhancer.enabled = false
            }
            enhancers[sessionId] = enhancer
        } catch (t: Throwable) {
        }
    }

    private fun removeAndReleaseEnhancer(sessionId: Int) {
        val enhancer = enhancers.remove(sessionId)
        if (enhancer != null) {
            try {
                enhancer.enabled = false
                enhancer.release()
            } catch (t: Throwable) {
            }
        }
    }

    fun release() {
        try {
            context.unregisterReceiver(audioSessionReceiver)
        } catch (t: Throwable) {
        }
        synchronized(enhancers) {
            for (enhancer in enhancers.values) {
                try {
                    enhancer.enabled = false
                    enhancer.release()
                } catch (t: Throwable) {
                }
            }
            enhancers.clear()
        }
    }
}
