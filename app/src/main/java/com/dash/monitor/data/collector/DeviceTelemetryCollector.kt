package com.dash.monitor.data.collector

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.util.DateKeys

class DeviceTelemetryCollector(private val context: Context) {

    fun captureSnapshot(screenUnlockCount: Int): DeviceSnapshot {
        val now = System.currentTimeMillis()

        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val batteryTempC = tempTenths / 10f

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorage = statFs.blockCountLong * statFs.blockSizeLong
        val availStorage = statFs.availableBlocksLong * statFs.blockSizeLong

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return DeviceSnapshot(
            timestamp = now,
            dateKey = DateKeys.dayKey(now),
            monthKey = DateKeys.monthKey(now),
            yearKey = DateKeys.yearKey(now),
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryTempC = batteryTempC,
            availableStorageBytes = availStorage,
            totalStorageBytes = totalStorage,
            availableRamBytes = memInfo.availMem,
            totalRamBytes = memInfo.totalMem,
            screenUnlockCount = screenUnlockCount
        )
    }
}
