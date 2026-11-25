package com.dash.monitor.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateKeys {
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val displayDayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val displayMonthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    fun dayKey(timestampMs: Long = System.currentTimeMillis()): String =
        dayFormat.format(Date(timestampMs))

    fun monthKey(timestampMs: Long = System.currentTimeMillis()): String =
        monthFormat.format(Date(timestampMs))

    fun yearKey(timestampMs: Long = System.currentTimeMillis()): String =
        yearFormat.format(Date(timestampMs))

    fun timeLabel(timestampMs: Long): String =
        timeFormat.format(Date(timestampMs))

    fun displayDay(dayKey: String): String = try {
        val date = dayFormat.parse(dayKey)
        if (date != null) displayDayFormat.format(date) else dayKey
    } catch (_: Exception) {
        dayKey
    }

    fun displayMonth(monthKey: String): String = try {
        val date = monthFormat.parse(monthKey)
        if (date != null) displayMonthFormat.format(date) else monthKey
    } catch (_: Exception) {
        monthKey
    }

    fun startOfDayMs(timestampMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun daysAgoMs(days: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${ms / 1000}s"
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}
