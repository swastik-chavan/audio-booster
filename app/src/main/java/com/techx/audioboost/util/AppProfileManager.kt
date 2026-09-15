package com.techx.audioboost.util

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.techx.audioboost.model.AppProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages per-app audio profiles and accurately handles Android system limitations.
 * Android requires explicit PACKAGE_USAGE_STATS permission to inspect the foreground app.
 */
class AppProfileManager(private val context: Context) {

    private var monitorJob: Job? = null
    private var lastReportedPackage: String? = null

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getForegroundAppPackage(): String? {
        if (!hasUsageAccess()) return null
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
            val time = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(time - 6000, time)
            val event = UsageEvents.Event()
            var currentForeground: String? = null

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    currentForeground = event.packageName
                }
            }
            return currentForeground
        } catch (t: Throwable) {
            return null
        }
    }

    fun getInstalledLaunchableApps(): List<Pair<String, String>> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            pm.queryIntentActivities(intent, 0)
        }

        val apps = mutableListOf<Pair<String, String>>()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName) {
                val label = info.loadLabel(pm).toString()
                apps.add(Pair(pkg, label))
            }
        }
        return apps.distinctBy { it.first }.sortedBy { it.second.lowercase() }
    }

    fun startMonitoring(
        scope: CoroutineScope,
        onForegroundAppChanged: (packageName: String?) -> Unit
    ) {
        monitorJob?.cancel()
        monitorJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (hasUsageAccess()) {
                    val currentPkg = getForegroundAppPackage()
                    if (currentPkg != null && currentPkg != lastReportedPackage && currentPkg != context.packageName) {
                        lastReportedPackage = currentPkg
                        onForegroundAppChanged(currentPkg)
                    }
                }
                delay(2500)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        lastReportedPackage = null
    }
}
