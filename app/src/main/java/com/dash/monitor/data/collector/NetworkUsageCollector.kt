package com.dash.monitor.data.collector

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import com.dash.monitor.data.local.entity.DataUsageDaily
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper

class NetworkUsageCollector(private val context: Context) {

    fun collectForDay(dayStartMs: Long, dayEndMs: Long): List<DataUsageDaily> {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return emptyList()
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                ?: return emptyList()

        val pm = context.packageManager
        val uidPackages = buildUidPackageMap(pm)

        val wifiByPkg = queryTypeByPackage(
            networkStatsManager,
            ConnectivityManager.TYPE_WIFI,
            dayStartMs,
            dayEndMs,
            uidPackages
        )

        val mobileByPkg = queryTypeByPackage(
            networkStatsManager,
            ConnectivityManager.TYPE_MOBILE,
            dayStartMs,
            dayEndMs,
            uidPackages
        )

        val allPkgs = (wifiByPkg.keys + mobileByPkg.keys).toSet()
        val dateKey = DateKeys.dayKey(dayStartMs)
        val monthKey = DateKeys.monthKey(dayStartMs)
        val yearKey = DateKeys.yearKey(dayStartMs)

        return allPkgs.mapNotNull { pkg ->
            val wifi = wifiByPkg[pkg] ?: Pair(0L, 0L)
            val mobile = mobileByPkg[pkg] ?: Pair(0L, 0L)
            val total = wifi.first + wifi.second + mobile.first + mobile.second
            if (total <= 0L) return@mapNotNull null

            DataUsageDaily(
                dateKey = dateKey,
                packageName = pkg,
                appName = resolveLabel(pm, pkg),
                wifiRxBytes = wifi.first,
                wifiTxBytes = wifi.second,
                mobileRxBytes = mobile.first,
                mobileTxBytes = mobile.second,
                monthKey = monthKey,
                yearKey = yearKey
            )
        }
    }

    private fun queryTypeByPackage(
        manager: NetworkStatsManager,
        networkType: Int,
        startMs: Long,
        endMs: Long,
        uidPackages: Map<Int, String>
    ): Map<String, Pair<Long, Long>> {
        val result = mutableMapOf<String, Pair<Long, Long>>()
        try {
            val stats = manager.querySummary(networkType, null, startMs, endMs)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val pkg = uidPackages[bucket.uid] ?: "system.uid.${bucket.uid}"
                val prev = result[pkg] ?: Pair(0L, 0L)
                result[pkg] = Pair(
                    prev.first + bucket.rxBytes,
                    prev.second + bucket.txBytes
                )
            }
            stats.close()
        } catch (_: Exception) {
            // SecurityException or unsupported network type on emulator/device
        }
        return result
    }

    private fun buildUidPackageMap(pm: PackageManager): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            map.putIfAbsent(app.uid, app.packageName)
        }
        return map
    }

    private fun resolveLabel(pm: PackageManager, pkg: String): String {
        if (pkg.startsWith("system.uid.")) return "System (${pkg.removePrefix("system.uid.")})"
        return try {
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            pkg.substringAfterLast('.')
        }
    }
}
