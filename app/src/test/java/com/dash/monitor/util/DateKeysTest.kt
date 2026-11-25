package com.dash.monitor.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DateKeysTest {

    @Test
    fun formatDuration_formatsHoursMinutesAndSeconds() {
        assertEquals("0s", DateKeys.formatDuration(0L))
        assertEquals("45s", DateKeys.formatDuration(45_000L))
        assertEquals("12m", DateKeys.formatDuration(12 * 60_000L))
        assertEquals("2h 15m", DateKeys.formatDuration((2 * 60 + 15) * 60_000L))
    }

    @Test
    fun formatBytes_formatsReadableUnits() {
        assertEquals("512 B", DateKeys.formatBytes(512L))
        assertEquals("2.0 KB", DateKeys.formatBytes(2048L))
        assertEquals("1.5 MB", DateKeys.formatBytes(1_572_864L))
        assertEquals("1.00 GB", DateKeys.formatBytes(1_073_741_824L))
    }
}
