package com.dash.monitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_records",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["monthKey"]),
        Index(value = ["yearKey"]),
        Index(value = ["phoneNumber"])
    ]
)
data class CallRecordEntity(
    @PrimaryKey val callId: Long,
    val timestamp: Long,
    val dateKey: String,
    val monthKey: String,
    val yearKey: String,
    val phoneNumber: String,
    val contactName: String,
    val callType: Int, // 1=Incoming, 2=Outgoing, 3=Missed, 5=Rejected
    val durationSeconds: Long
)
