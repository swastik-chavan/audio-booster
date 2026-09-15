package com.techx.audioboost.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.techx.audioboost.MainActivity
import com.techx.audioboost.R
import com.techx.audioboost.audio.AudioProcessingManager

/**
 * AppWidgetProvider for the 2x2 Pro Home Screen Widget.
 */
class AudioBoostWidget2x2Provider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_TOGGLE = "com.techx.audioboost.ACTION_WIDGET_TOGGLE"
        const val ACTION_WIDGET_PLUS = "com.techx.audioboost.ACTION_WIDGET_PLUS"
        const val ACTION_WIDGET_MINUS = "com.techx.audioboost.ACTION_WIDGET_MINUS"

        fun update2x2Widget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            boostPercent: Int,
            isUltra: Boolean,
            deviceName: String,
            gainDb: Float
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_2x2_layout)

            // Click container to launch app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPi = PendingIntent.getActivity(
                context,
                10,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_container, openAppPi)

            // Boost percent
            views.setTextViewText(R.id.widget_2x2_boost_percent, "$boostPercent%")

            // Mode and Gain label
            val modeText = if (boostPercent == 0) {
                "Boost Disabled"
            } else if (isUltra) {
                "Ultra Mode (+${String.format("%.1f", gainDb)} dB)"
            } else {
                "Normal Boost (+${String.format("%.1f", gainDb)} dB)"
            }
            views.setTextViewText(R.id.widget_2x2_mode_label, modeText)

            // Device label
            views.setTextViewText(R.id.widget_2x2_device_label, deviceName)

            // Status badge & toggle button text/color
            if (boostPercent > 0) {
                views.setTextViewText(R.id.widget_2x2_status, "ACTIVE")
                views.setTextColor(R.id.widget_2x2_status, Color.parseColor("#F5B942"))
                views.setTextViewText(R.id.widget_2x2_btn_toggle, "OFF")
                views.setTextColor(R.id.widget_2x2_btn_toggle, Color.parseColor("#FF5C5C"))
            } else {
                views.setTextViewText(R.id.widget_2x2_status, "INACTIVE")
                views.setTextColor(R.id.widget_2x2_status, Color.parseColor("#9A9A9A"))
                views.setTextViewText(R.id.widget_2x2_btn_toggle, "BOOST")
                views.setTextColor(R.id.widget_2x2_btn_toggle, Color.parseColor("#F5B942"))
            }

            // PendingIntents for buttons
            val toggleIntent = Intent(context, AudioBoostWidget2x2Provider::class.java).apply {
                action = ACTION_WIDGET_TOGGLE
            }
            val togglePi = PendingIntent.getBroadcast(
                context,
                11,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_btn_toggle, togglePi)

            val plusIntent = Intent(context, AudioBoostWidget2x2Provider::class.java).apply {
                action = ACTION_WIDGET_PLUS
            }
            val plusPi = PendingIntent.getBroadcast(
                context,
                12,
                plusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_btn_plus, plusPi)

            val minusIntent = Intent(context, AudioBoostWidget2x2Provider::class.java).apply {
                action = ACTION_WIDGET_MINUS
            }
            val minusPi = PendingIntent.getBroadcast(
                context,
                13,
                minusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_btn_minus, minusPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        try {
            val manager = AudioProcessingManager.getInstance(context)
            val state = manager.effectsState.value
            val boost = state.boostPercent
            val isUltra = state.isUltra
            val gainDb = (boost * 20f) / 100f
            val device = manager.audioController.getCurrentDevice().first
            for (id in appWidgetIds) {
                update2x2Widget(context, appWidgetManager, id, boost, isUltra, device, gainDb)
            }
        } catch (t: Throwable) {
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        val manager = AudioProcessingManager.getInstance(context)
        val currentBoost = manager.effectsState.value.boostPercent

        when (action) {
            ACTION_WIDGET_TOGGLE -> {
                val newBoost = if (currentBoost > 0) 0 else 50
                manager.setBoostPercent(newBoost)
            }
            ACTION_WIDGET_PLUS -> {
                val newBoost = (currentBoost + 10).coerceAtMost(100)
                manager.setBoostPercent(newBoost)
            }
            ACTION_WIDGET_MINUS -> {
                val newBoost = (currentBoost - 10).coerceAtLeast(0)
                manager.setBoostPercent(newBoost)
            }
        }
    }
}
