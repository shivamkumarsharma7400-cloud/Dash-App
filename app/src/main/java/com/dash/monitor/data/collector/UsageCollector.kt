package com.dash.monitor.data.collector

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.dash.monitor.data.local.entity.AppUsageDaily
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper

data class UsageCollectionResult(
    val dailyApps: List<AppUsageDaily>,
    val screenUnlockCount: Int
)

class UsageCollector(private val context: Context) {

    fun collectForDay(dayStartMs: Long, dayEndMs: Long): UsageCollectionResult {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return UsageCollectionResult(emptyList(), 0)
        }

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return UsageCollectionResult(emptyList(), 0)

        val pm = context.packageManager
        val events = usageStatsManager.queryEvents(dayStartMs, dayEndMs)
        val event = UsageEvents.Event()

        val foregroundStartMap = mutableMapOf<String, Long>()
        val totalTimeMap = mutableMapOf<String, Long>()
        val launchCountMap = mutableMapOf<String, Int>()
        val lastUsedMap = mutableMapOf<String, Long>()
        var unlockCount = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    foregroundStartMap[pkg] = event.timeStamp
                    launchCountMap[pkg] = (launchCountMap[pkg] ?: 0) + 1
                    lastUsedMap[pkg] = maxOf(lastUsedMap[pkg] ?: 0L, event.timeStamp)
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val start = foregroundStartMap.remove(pkg)
                    if (start != null && event.timeStamp > start) {
                        totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + (event.timeStamp - start)
                    }
                    lastUsedMap[pkg] = maxOf(lastUsedMap[pkg] ?: 0L, event.timeStamp)
                }
                UsageEvents.Event.KEYGUARD_HIDDEN -> {
                    unlockCount++
                }
            }
        }

        // Close any currently open foreground session up to dayEndMs
        val now = minOf(System.currentTimeMillis(), dayEndMs)
        foregroundStartMap.forEach { (pkg, start) ->
            if (now > start) {
                totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + (now - start)
                lastUsedMap[pkg] = maxOf(lastUsedMap[pkg] ?: 0L, now)
            }
        }

        val dateKey = DateKeys.dayKey(dayStartMs)
        val monthKey = DateKeys.monthKey(dayStartMs)
        val yearKey = DateKeys.yearKey(dayStartMs)

        val results = totalTimeMap.entries
            .filter { it.value > 1000L } // ignore <1s noise
            .map { (pkg, duration) ->
                AppUsageDaily(
                    dateKey = dateKey,
                    packageName = pkg,
                    appName = resolveAppName(pm, pkg),
                    foregroundTimeMs = duration,
                    launchCount = launchCountMap[pkg] ?: 1,
                    lastTimeUsed = lastUsedMap[pkg] ?: dayStartMs,
                    monthKey = monthKey,
                    yearKey = yearKey
                )
            }

        return UsageCollectionResult(results, unlockCount)
    }

    private fun resolveAppName(pm: PackageManager, packageName: String): String {
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
    }
}
