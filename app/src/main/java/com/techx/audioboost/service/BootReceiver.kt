package com.techx.audioboost.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.techx.audioboost.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handles device boot completion to restore audio booster state if configured.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val repository = SettingsRepository(context)
                    val boost = repository.boostPercent.first()
                    val isUltra = repository.isUltraEnabled.first()
                    val notificationEnabled = repository.notificationEnabled.first()

                    if (boost > 0 && notificationEnabled) {
                        AudioService.updateNotification(
                            context,
                            boostPercent = boost,
                            isUltra = isUltra,
                            gainDb = (boost * 20f) / 100f,
                            presetName = "Restored",
                            deviceName = "Speaker"
                        )
                    }
                    AudioBoostTileService.requestUpdate(context)
                    com.techx.audioboost.widget.AudioBoostWidgetProvider.updateAllWidgets(
                        context,
                        boostPercent = boost,
                        isUltra = isUltra,
                        deviceName = "Speaker",
                        gainDb = (boost * 20f) / 100f
                    )
                } catch (t: Throwable) {
                }
            }
        }
    }
}
