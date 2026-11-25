package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "hourly_usage",
    primaryKeys = ["dateKey", "hour"],
    indices = [
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"])
    ]
)
data class HourlyUsageEntity(
    val dateKey: String,
    val hour: Int, // 0..23
    val monthKey: String,
    val yearKey: String,
    val foregroundTimeMs: Long,
    val unlockCount: Int,
    val topAppName: String
)
