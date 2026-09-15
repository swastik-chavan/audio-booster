package com.techx.audioboost.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.techx.audioboost.MainActivity
import com.techx.audioboost.R
import com.techx.audioboost.audio.AudioProcessingManager
import com.techx.audioboost.model.AppProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service hosting the ongoing Audio Booster notification
 * to keep processing active, provide quick status controls, and monitor app profiles.
 */
class AudioService : Service() {

    companion object {
        const val CHANNEL_ID = "audio_boost_playback"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_OR_UPDATE = "com.techx.audioboost.ACTION_START_OR_UPDATE"
        const val ACTION_STOP_SERVICE = "com.techx.audioboost.ACTION_STOP_SERVICE"
        const val ACTION_TOGGLE_BOOST = "com.techx.audioboost.ACTION_TOGGLE_BOOST"
        const val ACTION_TOGGLE_ULTRA = "com.techx.audioboost.ACTION_TOGGLE_ULTRA"

        const val EXTRA_BOOST_PERCENT = "extra_boost_percent"
        const val EXTRA_IS_ULTRA = "extra_is_ultra"
        const val EXTRA_GAIN_DB = "extra_gain_db"
        const val EXTRA_PRESET_NAME = "extra_preset_name"
        const val EXTRA_DEVICE_NAME = "extra_device_name"

        private var lastUpdateTime = 0L
        private var lastReportedBoost = -1
        private var lastReportedUltra = false

        fun updateNotification(
            context: Context,
            boostPercent: Int,
            isUltra: Boolean,
            gainDb: Float,
            presetName: String,
            deviceName: String
        ) {
            val now = System.currentTimeMillis()
            // Throttle notification updates unless boost level changed significantly or state toggled
            if (boostPercent == lastReportedBoost && isUltra == lastReportedUltra && now - lastUpdateTime < 300L) {
                return
            }
            lastUpdateTime = now
            lastReportedBoost = boostPercent
            lastReportedUltra = isUltra

            val intent = Intent(context, AudioService::class.java).apply {
                action = ACTION_START_OR_UPDATE
                putExtra(EXTRA_BOOST_PERCENT, boostPercent)
                putExtra(EXTRA_IS_ULTRA, isUltra)
                putExtra(EXTRA_GAIN_DB, gainDb)
                putExtra(EXTRA_PRESET_NAME, presetName)
                putExtra(EXTRA_DEVICE_NAME, deviceName)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
            }
        }

        fun stopService(context: Context) {
            lastReportedBoost = -1
            val intent = Intent(context, AudioService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            try {
                context.startService(intent)
            } catch (t: Throwable) {
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var profileMonitorJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startProfileMonitoringIfEnabled()
    }

    private fun startProfileMonitoringIfEnabled() {
        profileMonitorJob?.cancel()
        profileMonitorJob = serviceScope.launch {
            val manager = AudioProcessingManager.getInstance(applicationContext)
            val isProfilesEnabled = manager.settingsRepository.appProfilesEnabled.first()
            if (isProfilesEnabled && manager.appProfileManager.hasUsageAccess()) {
                val profiles = manager.settingsRepository.appProfiles.first()
                manager.appProfileManager.startMonitoring(serviceScope) { foregroundPackage ->
                    manager.handleAppForegroundChanged(foregroundPackage, profiles)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = AudioProcessingManager.getInstance(applicationContext)

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_BOOST -> {
                val currentBoost = manager.effectsState.value.boostPercent
                manager.setBoostPercent(if (currentBoost > 0) 0 else 50)
            }
            ACTION_TOGGLE_ULTRA -> {
                val currentUltra = manager.effectsState.value.isUltra
                manager.toggleUltraMode(!currentUltra)
            }
            ACTION_START_OR_UPDATE -> {
                val boostPercent = intent.getIntExtra(EXTRA_BOOST_PERCENT, 0)
                val isUltra = intent.getBooleanExtra(EXTRA_IS_ULTRA, false)
                val gainDb = intent.getFloatExtra(EXTRA_GAIN_DB, 0f)
                val presetName = intent.getStringExtra(EXTRA_PRESET_NAME) ?: "Flat"
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "Speaker"

                val notification = buildNotification(boostPercent, isUltra, gainDb, presetName, deviceName)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
                    } else {
                        0
                    }
                    if (serviceType != 0) {
                        startForeground(NOTIFICATION_ID, notification, serviceType)
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        boostPercent: Int,
        isUltra: Boolean,
        gainDb: Float,
        presetName: String,
        deviceName: String
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Directly action the service to avoid interrupting foreground user activities
        val toggleIntent = Intent(this, AudioService::class.java).apply {
            action = ACTION_TOGGLE_BOOST
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleUltraIntent = Intent(this, AudioService::class.java).apply {
            action = ACTION_TOGGLE_ULTRA
        }
        val toggleUltraPendingIntent = PendingIntent.getService(
            this,
            2,
            toggleUltraIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (boostPercent == 0) {
            "Audio Booster • Standby"
        } else if (isUltra) {
            "Audio Booster • Ultra $boostPercent% (+${String.format("%.1f", gainDb)} dB)"
        } else {
            "Audio Booster • $boostPercent% (+${String.format("%.1f", gainDb)} dB)"
        }

        val text = "$deviceName • $presetName Preset"

        val toggleLabel = if (boostPercent > 0) "Turn Off" else "Boost 50%"
        val ultraLabel = if (isUltra) "Normal Mode" else "Ultra Mode"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_boost)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(boostPercent > 0)
            .setOnlyAlertOnce(true)
            .addAction(0, toggleLabel, togglePendingIntent)
            .addAction(0, ultraLabel, toggleUltraPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        profileMonitorJob?.cancel()
        val manager = AudioProcessingManager.getInstance(applicationContext)
        manager.appProfileManager.stopMonitoring()
    }
}
