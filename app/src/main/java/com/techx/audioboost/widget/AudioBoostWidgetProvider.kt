package com.techx.audioboost.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.techx.audioboost.MainActivity
import com.techx.audioboost.R
import com.techx.audioboost.audio.AudioProcessingManager

/**
 * AppWidgetProvider for the 2x1 Compact Home Screen Widget.
 */
class AudioBoostWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_TOGGLE = "com.techx.audioboost.ACTION_WIDGET_TOGGLE"
        const val ACTION_WIDGET_STEP = "com.techx.audioboost.ACTION_WIDGET_STEP"

        fun updateAllWidgets(
            context: Context,
            boostPercent: Int,
            isUltra: Boolean,
            deviceName: String,
            gainDb: Float
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName2x1 = ComponentName(context, AudioBoostWidgetProvider::class.java)
            val ids2x1 = appWidgetManager.getAppWidgetIds(componentName2x1)
            for (id in ids2x1) {
                update2x1Widget(context, appWidgetManager, id, boostPercent)
            }

            val componentName2x2 = ComponentName(context, AudioBoostWidget2x2Provider::class.java)
            val ids2x2 = appWidgetManager.getAppWidgetIds(componentName2x2)
            for (id in ids2x2) {
                AudioBoostWidget2x2Provider.update2x2Widget(
                    context,
                    appWidgetManager,
                    id,
                    boostPercent,
                    isUltra,
                    deviceName,
                    gainDb
                )
            }
        }

        fun update2x1Widget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            boostPercent: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_2x1_layout)

            // Click container to launch app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPi = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPi)

            // Percentage readout
            views.setTextViewText(R.id.widget_boost_percent, "$boostPercent%")

            // Toggle button
            val toggleIntent = Intent(context, AudioBoostWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_TOGGLE
            }
            val togglePi = PendingIntent.getBroadcast(
                context,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_toggle, togglePi)

            if (boostPercent > 0) {
                views.setTextViewText(R.id.widget_btn_toggle, "ON")
                views.setTextColor(R.id.widget_btn_toggle, Color.parseColor("#F5B942"))
            } else {
                views.setTextViewText(R.id.widget_btn_toggle, "OFF")
                views.setTextColor(R.id.widget_btn_toggle, Color.parseColor("#9A9A9A"))
            }

            // Step +10% button
            val stepIntent = Intent(context, AudioBoostWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_STEP
            }
            val stepPi = PendingIntent.getBroadcast(
                context,
                2,
                stepIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_step, stepPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        try {
            val manager = AudioProcessingManager.getInstance(context)
            val boost = manager.effectsState.value.boostPercent
            for (id in appWidgetIds) {
                update2x1Widget(context, appWidgetManager, id, boost)
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
            ACTION_WIDGET_STEP -> {
                val newBoost = (currentBoost + 10).coerceAtMost(100)
                manager.setBoostPercent(newBoost)
            }
        }
    }
}
