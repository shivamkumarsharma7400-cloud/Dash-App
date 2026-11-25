package com.dash.monitor.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dash.monitor.data.collector.AppPermissionAudit
import com.dash.monitor.data.collector.CallLogCollector
import com.dash.monitor.data.collector.DeviceTelemetryCollector
import com.dash.monitor.data.collector.LocationTracker
import com.dash.monitor.data.collector.NetworkUsageCollector
import com.dash.monitor.data.collector.PermissionAuditor
import com.dash.monitor.data.collector.UsageCollector
import com.dash.monitor.data.local.DashDatabase
import com.dash.monitor.data.local.dao.MonitorDao
import com.dash.monitor.data.local.entity.AppLimitEntity
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.data.local.entity.NetworkEventEntity
import com.dash.monitor.util.DateKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MonitorRepository(private val context: Context) {

    private val db = DashDatabase.getInstance(context)
    val dao: MonitorDao = db.monitorDao()

    private val usageCollector = UsageCollector(context)
    private val networkCollector = NetworkUsageCollector(context)
    private val telemetryCollector = DeviceTelemetryCollector(context)
    private val locationTracker = LocationTracker(context)
    private val callLogCollector = CallLogCollector(context)
    private val permissionAuditor = PermissionAuditor(context)

    suspend fun syncRecentDays(daysBack: Int = 6) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        var latestUnlocks = 0

        for (offset in daysBack downTo 0) {
            val dayStart = DateKeys.daysAgoMs(offset)
            val dayEnd = if (offset == 0) now else dayStart + 86_400_000L - 1L

            val usageResult = usageCollector.collectForDay(dayStart, dayEnd)
            if (usageResult.dailyApps.isNotEmpty()) {
                dao.upsertAppUsage(usageResult.dailyApps)
            }
            if (usageResult.hourlyBuckets.isNotEmpty()) {
                dao.upsertHourlyUsage(usageResult.hourlyBuckets)
            }
            if (offset == 0) {
                latestUnlocks = usageResult.screenUnlockCount
            }

            val networkItems = networkCollector.collectForDay(dayStart, dayEnd)
            if (networkItems.isNotEmpty()) {
                dao.upsertDataUsage(networkItems)
            }
        }

        val calls = callLogCollector.collectRecentCalls(limit = 500)
        if (calls.isNotEmpty()) {
            dao.upsertCalls(calls)
        }

        val snapshot = telemetryCollector.captureSnapshot(latestUnlocks)
        dao.insertDeviceSnapshot(snapshot)

        recordActiveNetworkState(now)

        locationTracker.getLastKnownLocation()?.let { loc ->
            dao.insertLocation(loc)
        }
    }

    private suspend fun recordActiveNetworkState(now: Long) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val type = when {
            caps == null -> "OFFLINE"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "BLUETOOTH"
            else -> "OTHER"
        }
        val detail = when (type) {
            "WIFI" -> "Connected via Wi-Fi"
            "MOBILE" -> "Connected via Cellular"
            "OFFLINE" -> "No active network"
            else -> type
        }
        dao.insertNetworkEvent(
            NetworkEventEntity(
                timestamp = now,
                dateKey = DateKeys.dayKey(now),
                monthKey = DateKeys.monthKey(now),
                yearKey = DateKeys.yearKey(now),
                transportType = type,
                detail = detail
            )
        )
    }

    suspend fun saveLocation(record: LocationRecord) = withContext(Dispatchers.IO) {
        dao.insertLocation(record)
    }

    suspend fun scanAppPermissions(): List<AppPermissionAudit> = withContext(Dispatchers.IO) {
        permissionAuditor.scanInstalledApps()
    }

    suspend fun setAppLimit(packageName: String, appName: String, limitMinutes: Int) = withContext(Dispatchers.IO) {
        if (limitMinutes <= 0) {
            dao.deleteAppLimit(packageName)
        } else {
            dao.upsertAppLimit(AppLimitEntity(packageName, appName, limitMinutes))
        }
    }

    suspend fun exportLocalBackupJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val locations = dao.getAllLocations()
            val appUsage = dao.getAllAppUsage()
            val dataUsage = dao.getAllDataUsage()
            val snapshots = dao.getAllSnapshots()
            val calls = dao.getAllCalls()
            val notifications = dao.getAllNotifications()

            val root = JSONObject().apply {
                put("exportedAt", System.currentTimeMillis())
                put("exportedDate", DateKeys.dayKey())
                put("locationCount", locations.size)
                put("appUsageCount", appUsage.size)
                put("dataUsageCount", dataUsage.size)
                put("callCount", calls.size)
                put("notificationCount", notifications.size)
                put("snapshotCount", snapshots.size)

                put("calls", JSONArray().apply {
                    calls.forEach { call ->
                        put(JSONObject().apply {
                            put("timestamp", call.timestamp)
                            put("date", call.dateKey)
                            put("contact", call.contactName)
                            put("number", call.phoneNumber)
                            put("type", call.callType)
                            put("durationSec", call.durationSeconds)
                        })
                    }
                })
                put("locations", JSONArray().apply {
                    locations.forEach { loc ->
                        put(JSONObject().apply {
                            put("timestamp", loc.timestamp)
                            put("date", loc.dateKey)
                            put("lat", loc.latitude)
                            put("lng", loc.longitude)
                            put("accuracyM", loc.accuracyMeters)
                            put("address", loc.addressLine ?: "")
                        })
                    }
                })
                put("appUsageDaily", JSONArray().apply {
                    appUsage.forEach { app ->
                        put(JSONObject().apply {
                            put("date", app.dateKey)
                            put("appName", app.appName)
                            put("foregroundMs", app.foregroundTimeMs)
                            put("launches", app.launchCount)
                        })
                    }
                })
                put("dataUsageDaily", JSONArray().apply {
                    dataUsage.forEach { net ->
                        put(JSONObject().apply {
                            put("date", net.dateKey)
                            put("appName", net.appName)
                            put("wifiBytes", net.totalWifiBytes)
                            put("mobileBytes", net.totalMobileBytes)
                        })
                    }
                })
                put("notifications", JSONArray().apply {
                    notifications.forEach { n ->
                        put(JSONObject().apply {
                            put("timestamp", n.timestamp)
                            put("app", n.appName)
                            put("title", n.title)
                            put("text", n.text)
                        })
                    }
                })
            }

            val fileName = "dash_backup_${DateKeys.dayKey()}_${System.currentTimeMillis() / 1000}.json"
            writeToDownloads(fileName, "application/json", root.toString(2).toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportCallsCsv(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val calls = dao.getAllCalls()
            val sb = StringBuilder()
            sb.appendLine("Date,Time,Contact,Number,Type,DurationSeconds")
            calls.forEach { c ->
                val typeStr = when (c.callType) {
                    1 -> "INCOMING"
                    2 -> "OUTGOING"
                    3 -> "MISSED"
                    5 -> "REJECTED"
                    else -> "OTHER"
                }
                val safeContact = c.contactName.replace(",", " ")
                sb.appendLine("${c.dateKey},${DateKeys.timeLabel(c.timestamp)},$safeContact,${c.phoneNumber},$typeStr,${c.durationSeconds}")
            }
            val fileName = "dash_calls_${DateKeys.dayKey()}.csv"
            writeToDownloads(fileName, "text/csv", sb.toString().toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeToDownloads(fileName: String, mimeType: String, bytes: ByteArray): Result<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Dash")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return Result.failure(IllegalStateException("Unable to create export file"))

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
            }
            Result.success("Downloads/Dash/$fileName")
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Dash"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            Result.success(file.absolutePath)
        }
    }

    companion object {
        fun calculateDistanceKm(records: List<LocationRecord>): Double {
            if (records.size < 2) return 0.0
            val ordered = records.sortedBy { it.timestamp }
            var totalKm = 0.0
            for (i in 1 until ordered.size) {
                val prev = ordered[i - 1]
                val curr = ordered[i]
                totalKm += haversineKm(prev.latitude, prev.longitude, curr.latitude, curr.longitude)
            }
            return totalKm
        }

        private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
            return 2 * r * atan2(sqrt(a), sqrt(1 - a))
        }
    }
}
