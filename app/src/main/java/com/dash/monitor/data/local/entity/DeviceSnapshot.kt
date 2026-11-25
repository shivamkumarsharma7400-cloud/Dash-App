package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_snapshots",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"]),
        Index(value = ["timestamp"])
    ]
)
data class DeviceSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateKey: String,
    val monthKey: String,
    val yearKey: String,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val batteryTempC: Float,
    val availableStorageBytes: Long,
    val totalStorageBytes: Long,
    val availableRamBytes: Long,
    val totalRamBytes: Long,
    val screenUnlockCount: Int
)
