package com.dash.monitor.data.collector

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.dash.monitor.data.local.entity.AppUsageDaily
import com.dash.monitor.data.local.entity.HourlyUsageEntity
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper
import java.util.Calendar

data class UsageCollectionResult(
    val dailyApps: List<AppUsageDaily>,
    val hourlyBuckets: List<HourlyUsageEntity>,
    val screenUnlockCount: Int
)

class UsageCollector(private val context: Context) {

    fun collectForDay(dayStartMs: Long, dayEndMs: Long): UsageCollectionResult {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return UsageCollectionResult(emptyList(), emptyList(), 0)
        }

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return UsageCollectionResult(emptyList(), emptyList(), 0)

        val pm = context.packageManager
        val events = usageStatsManager.queryEvents(dayStartMs, dayEndMs)
        val event = UsageEvents.Event()

        val foregroundStartMap = mutableMapOf<String, Long>()
        val totalTimeMap = mutableMapOf<String, Long>()
        val launchCountMap = mutableMapOf<String, Int>()
        val lastUsedMap = mutableMapOf<String, Long>()

        val hourlyDuration = LongArray(24)
        val hourlyUnlocks = IntArray(24)
        val hourlyAppTime = Array(24) { mutableMapOf<String, Long>() }
        var unlockCount = 0

        val cal = Calendar.getInstance()

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
                        val dur = event.timeStamp - start
                        totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + dur
                        cal.timeInMillis = start
                        val hr = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
                        hourlyDuration[hr] += dur
                        hourlyAppTime[hr][pkg] = (hourlyAppTime[hr][pkg] ?: 0L) + dur
                    }
                    lastUsedMap[pkg] = maxOf(lastUsedMap[pkg] ?: 0L, event.timeStamp)
                }
                UsageEvents.Event.KEYGUARD_HIDDEN -> {
                    unlockCount++
                    cal.timeInMillis = event.timeStamp
                    val hr = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
                    hourlyUnlocks[hr]++
                }
            }
        }

        val now = minOf(System.currentTimeMillis(), dayEndMs)
        foregroundStartMap.forEach { (pkg, start) ->
            if (now > start) {
                val dur = now - start
                totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + dur
                lastUsedMap[pkg] = maxOf(lastUsedMap[pkg] ?: 0L, now)
                cal.timeInMillis = start
                val hr = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
                hourlyDuration[hr] += dur
                hourlyAppTime[hr][pkg] = (hourlyAppTime[hr][pkg] ?: 0L) + dur
            }
        }

        val dateKey = DateKeys.dayKey(dayStartMs)
        val monthKey = DateKeys.monthKey(dayStartMs)
        val yearKey = DateKeys.yearKey(dayStartMs)

        val results = totalTimeMap.entries
            .filter { it.value > 1000L }
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

        val hourlyList = (0..23).map { hr ->
            val topPkg = hourlyAppTime[hr].maxByOrNull { it.value }?.key
            HourlyUsageEntity(
                dateKey = dateKey,
                hour = hr,
                monthKey = monthKey,
                yearKey = yearKey,
                foregroundTimeMs = hourlyDuration[hr],
                unlockCount = hourlyUnlocks[hr],
                topAppName = topPkg?.let { resolveAppName(pm, it) } ?: ""
            )
        }

        return UsageCollectionResult(results, hourlyList, unlockCount)
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
