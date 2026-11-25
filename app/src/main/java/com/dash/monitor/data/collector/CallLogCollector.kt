package com.dash.monitor.data.collector

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import com.dash.monitor.data.local.entity.CallRecordEntity
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper

class CallLogCollector(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun collectRecentCalls(limit: Int = 500): List<CallRecordEntity> {
        if (!PermissionHelper.getStatus(context).callLogGranted) return emptyList()

        val result = mutableListOf<CallRecordEntity>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            ) ?: return emptyList()

            cursor.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CallLog.Calls._ID)
                val numIdx = c.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val nameIdx = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val typeIdx = c.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val dateIdx = c.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val durIdx = c.getColumnIndexOrThrow(CallLog.Calls.DURATION)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val callId = c.getLong(idIdx)
                    val number = c.getString(numIdx)?.ifBlank { "Unknown" } ?: "Unknown"
                    val name = c.getString(nameIdx)?.ifBlank { number } ?: number
                    val type = c.getInt(typeIdx)
                    val ts = c.getLong(dateIdx)
                    val duration = c.getLong(durIdx)

                    result.add(
                        CallRecordEntity(
                            callId = callId,
                            timestamp = ts,
                            dateKey = DateKeys.dayKey(ts),
                            monthKey = DateKeys.monthKey(ts),
                            yearKey = DateKeys.yearKey(ts),
                            phoneNumber = number,
                            contactName = name,
                            callType = type,
                            durationSeconds = duration
                        )
                    )
                    count++
                }
            }
        } catch (_: Exception) {
        }

        return result
    }
}
