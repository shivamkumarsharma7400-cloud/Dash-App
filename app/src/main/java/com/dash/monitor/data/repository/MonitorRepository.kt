package com.dash.monitor.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dash.monitor.data.collector.DeviceTelemetryCollector
import com.dash.monitor.data.collector.LocationTracker
import com.dash.monitor.data.collector.NetworkUsageCollector
import com.dash.monitor.data.collector.UsageCollector
import com.dash.monitor.data.local.DashDatabase
import com.dash.monitor.data.local.dao.MonitorDao
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.util.DateKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class MonitorRepository(private val context: Context) {

    private val db = DashDatabase.getInstance(context)
    val dao: MonitorDao = db.monitorDao()

    private val usageCollector = UsageCollector(context)
    private val networkCollector = NetworkUsageCollector(context)
    private val telemetryCollector = DeviceTelemetryCollector(context)
    private val locationTracker = LocationTracker(context)

    suspend fun syncRecentDays(daysBack: Int = 6) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        var latestUnlocks = 0

        // Backfill recent days before Android's UsageStats buffer expires
        for (offset in daysBack downTo 0) {
            val dayStart = DateKeys.daysAgoMs(offset)
            val dayEnd = if (offset == 0) now else dayStart + 86_400_000L - 1L

            val usageResult = usageCollector.collectForDay(dayStart, dayEnd)
            if (usageResult.dailyApps.isNotEmpty()) {
                dao.upsertAppUsage(usageResult.dailyApps)
            }
            if (offset == 0) {
                latestUnlocks = usageResult.screenUnlockCount
            }

            val networkItems = networkCollector.collectForDay(dayStart, dayEnd)
            if (networkItems.isNotEmpty()) {
                dao.upsertDataUsage(networkItems)
            }
        }

        val snapshot = telemetryCollector.captureSnapshot(latestUnlocks)
        dao.insertDeviceSnapshot(snapshot)

        locationTracker.getLastKnownLocation()?.let { loc ->
            dao.insertLocation(loc)
        }
    }

    suspend fun saveLocation(record: LocationRecord) = withContext(Dispatchers.IO) {
        dao.insertLocation(record)
    }

    suspend fun exportLocalBackupJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val locations = dao.getAllLocations()
            val appUsage = dao.getAllAppUsage()
            val dataUsage = dao.getAllDataUsage()
            val snapshots = dao.getAllSnapshots()

            val root = JSONObject().apply {
                put("exportedAt", System.currentTimeMillis())
                put("exportedDate", DateKeys.dayKey())
                put("locationCount", locations.size)
                put("appUsageCount", appUsage.size)
                put("dataUsageCount", dataUsage.size)
                put("snapshotCount", snapshots.size)

                put("locations", JSONArray().apply {
                    locations.forEach { loc ->
                        put(JSONObject().apply {
                            put("timestamp", loc.timestamp)
                            put("date", loc.dateKey)
                            put("lat", loc.latitude)
                            put("lng", loc.longitude)
                            put("accuracyM", loc.accuracyMeters)
                            put("provider", loc.provider)
                            put("address", loc.addressLine ?: "")
                        })
                    }
                })

                put("appUsageDaily", JSONArray().apply {
                    appUsage.forEach { app ->
                        put(JSONObject().apply {
                            put("date", app.dateKey)
                            put("package", app.packageName)
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
                            put("package", net.packageName)
                            put("appName", net.appName)
                            put("wifiBytes", net.totalWifiBytes)
                            put("mobileBytes", net.totalMobileBytes)
                        })
                    }
                })

                put("deviceSnapshots", JSONArray().apply {
                    snapshots.forEach { snap ->
                        put(JSONObject().apply {
                            put("timestamp", snap.timestamp)
                            put("date", snap.dateKey)
                            put("batteryPct", snap.batteryLevel)
                            put("charging", snap.isCharging)
                            put("tempC", snap.batteryTempC)
                            put("availStorageBytes", snap.availableStorageBytes)
                            put("availRamBytes", snap.availableRamBytes)
                            put("unlocks", snap.screenUnlockCount)
                        })
                    }
                })
            }

            val fileName = "dash_backup_${DateKeys.dayKey()}_${System.currentTimeMillis() / 1000}.json"
            val jsonBytes = root.toString(2).toByteArray(Charsets.UTF_8)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Dash")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: return@withContext Result.failure(IllegalStateException("Unable to create export file"))

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonBytes)
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
                FileOutputStream(file).use { it.write(jsonBytes) }
                Result.success(file.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
