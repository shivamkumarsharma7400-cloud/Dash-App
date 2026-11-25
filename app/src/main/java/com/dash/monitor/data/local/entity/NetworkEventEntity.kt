package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "network_events",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["timestamp"])
    ]
)
data class NetworkEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateKey: String,
    val monthKey: String,
    val yearKey: String,
    val transportType: String, // WIFI, MOBILE, OFFLINE, BLUETOOTH
    val detail: String
)
