package com.techx.audioboost.audio

import android.content.Context
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Monitors media playback and handles automatic power-down when audio is stopped.
 * Avoids killing processing during transient pauses (e.g. track change, navigation).
 */
class AutoOffManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onAutoOffTriggered: () -> Unit
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isMediaPlaying = MutableStateFlow(false)
    val isMediaPlaying: StateFlow<Boolean> = _isMediaPlaying.asStateFlow()

    private var timeoutMinutes: Int = 0
    private var countdownJob: Job? = null
    private var monitorJob: Job? = null

    private var playbackCallback: Any? = null

    init {
        startMonitoring()
    }

    fun setAutoOffMinutes(minutes: Int) {
        timeoutMinutes = minutes
        if (minutes <= 0) {
            countdownJob?.cancel()
            countdownJob = null
        }
    }

    private fun startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
            val callback = object : AudioManager.AudioPlaybackCallback() {
                override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                    val isPlaying = configs?.any { config ->
                        val usage = config.audioAttributes?.usage ?: 0
                        usage == android.media.AudioAttributes.USAGE_MEDIA ||
                                usage == android.media.AudioAttributes.USAGE_GAME
                    } ?: false
                    handlePlaybackStateChanged(isPlaying)
                }
            }
            playbackCallback = callback
            try {
                audioManager.registerAudioPlaybackCallback(callback, null)
            } catch (t: Throwable) {
            }
        }

        // Lightweight periodic fallback probe (every 5 seconds) to ensure compatibility across all devices
        monitorJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val isMusicPlaying = audioManager?.isMusicActive ?: false
                handlePlaybackStateChanged(isMusicPlaying)
                delay(5000)
            }
        }
    }

    private fun handlePlaybackStateChanged(isPlaying: Boolean) {
        _isMediaPlaying.value = isPlaying

        if (isPlaying) {
            // Cancel active countdown if playback resumed
            countdownJob?.cancel()
            countdownJob = null
        } else if (timeoutMinutes > 0 && countdownJob == null) {
            // Start countdown only if auto-off is enabled
            countdownJob = scope.launch(Dispatchers.Default) {
                delay(timeoutMinutes * 60 * 1000L)
                if (!_isMediaPlaying.value) {
                    onAutoOffTriggered()
                }
            }
        }
    }

    fun release() {
        monitorJob?.cancel()
        countdownJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null && playbackCallback != null) {
            try {
                audioManager.unregisterAudioPlaybackCallback(playbackCallback as AudioManager.AudioPlaybackCallback)
            } catch (t: Throwable) {
            }
        }
    }
}
