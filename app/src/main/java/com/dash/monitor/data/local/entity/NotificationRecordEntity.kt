package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_records",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"]),
        Index(value = ["packageName"])
    ]
)
data class NotificationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateKey: String,
    val monthKey: String,
    val yearKey: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String
)
