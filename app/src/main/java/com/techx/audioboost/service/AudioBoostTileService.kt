package com.techx.audioboost.service

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.techx.audioboost.audio.AudioProcessingManager

/**
 * Android Quick Settings Tile allowing quick toggle of Audio Boost.
 * Real-time state synchronization with the central audio processing manager.
 */
@RequiresApi(Build.VERSION_CODES.N)
class AudioBoostTileService : TileService() {

    companion object {
        fun requestUpdate(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(
                        context,
                        ComponentName(context, AudioBoostTileService::class.java)
                    )
                } catch (t: Throwable) {
                }
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        try {
            val manager = AudioProcessingManager.getInstance(applicationContext)
            val currentBoost = manager.effectsState.value.boostPercent

            if (currentBoost > 0) {
                manager.setBoostPercent(0)
            } else {
                manager.setBoostPercent(50)
            }
            updateTileState()
        } catch (t: Throwable) {
        }
    }

    private fun updateTileState() {
        try {
            val manager = AudioProcessingManager.getInstance(applicationContext)
            val state = manager.effectsState.value
            val currentBoost = state.boostPercent
            val isUltra = state.isUltra

            val tile = qsTile ?: return

            if (currentBoost > 0) {
                tile.state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = if (isUltra) "Ultra $currentBoost%" else "$currentBoost%"
                    tile.contentDescription = if (isUltra) "Audio Boost active, Ultra mode $currentBoost%" else "Audio Boost active, $currentBoost%"
                }
            } else {
                tile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Off"
                    tile.contentDescription = "Audio Boost is off"
                }
            }
            tile.updateTile()
        } catch (t: Throwable) {
        }
    }
}
