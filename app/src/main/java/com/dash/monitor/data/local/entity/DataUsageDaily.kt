package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "data_usage_daily",
    primaryKeys = ["dateKey", "packageName"],
    indices = [
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"])
    ]
)
data class DataUsageDaily(
    val dateKey: String,
    val packageName: String,
    val appName: String,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val monthKey: String,
    val yearKey: String
) {
    val totalWifiBytes: Long get() = wifiRxBytes + wifiTxBytes
    val totalMobileBytes: Long get() = mobileRxBytes + mobileTxBytes
    val totalBytes: Long get() = totalWifiBytes + totalMobileBytes
}
