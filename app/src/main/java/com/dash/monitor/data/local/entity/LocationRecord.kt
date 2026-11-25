package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_records",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"]),
        Index(value = ["timestamp"])
    ]
)
data class LocationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateKey: String,
    val monthKey: String,
    val yearKey: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float,
    val altitudeMeters: Double,
    val provider: String,
    val addressLine: String?
)
