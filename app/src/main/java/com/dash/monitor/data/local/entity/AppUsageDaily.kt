package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "app_usage_daily",
    primaryKeys = ["dateKey", "packageName"],
    indices = [
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"])
    ]
)
data class AppUsageDaily(
    val dateKey: String,
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val launchCount: Int,
    val lastTimeUsed: Long,
    val monthKey: String,
    val yearKey: String
)
