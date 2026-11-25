package com.dash.monitor.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dash.monitor.data.local.dao.DataUsageSummary
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.HeroTelemetryDial
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.components.SplitDataMeter
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionStatus

@Composable
fun OverviewScreen(
    todayKey: String,
    todayScreenTimeMs: Long,
    monthScreenTimeMs: Long,
    yearScreenTimeMs: Long,
    todayData: List<DataUsageSummary>,
    latestLocation: LocationRecord?,
    todayLocationCount: Int,
    latestSnapshot: DeviceSnapshot?,
    permissionStatus: PermissionStatus,
    isSyncing: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSyncNow: () -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestLocationAccess: () -> Unit
) {
    val palette = LocalDashPalette.current
    val totalWifi = todayData.sumOf { it.wifiBytes }
    val totalMobile = todayData.sumOf { it.mobileBytes }
    val totalData = totalWifi + totalMobile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        // Editorial Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DASH / LOCAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(DashSpace.xs))
                    Text(
                        text = DateKeys.displayDay(todayKey),
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.ink
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                    OutlinedButton(
                        onClick = onToggleTheme,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, palette.hairline),
                        contentPadding = PaddingValues(horizontal = DashSpace.md, vertical = DashSpace.sm)
                    ) {
                        Text(
                            text = if (isDarkTheme) "INK" else "PAPER",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.ink
                        )
                    }
                    Button(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.invertedBg,
                            contentColor = palette.invertedInk
                        ),
                        contentPadding = PaddingValues(horizontal = DashSpace.md, vertical = DashSpace.sm)
                    ) {
                        Text(
                            text = if (isSyncing) "SYNCING" else "SYNC",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Permission Gate Banner if incomplete
        if (!permissionStatus.allCoreGranted) {
            item {
                HairlineCard {
                    Text(
                        text = "PERMISSIONS REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(DashSpace.sm))
                    Text(
                        text = "Grant Usage Access for screen time & data usage, and Location for local movement logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                        if (!permissionStatus.usageStatsGranted) {
                            Button(
                                onClick = onRequestUsageAccess,
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.invertedBg,
                                    contentColor = palette.invertedInk
                                )
                            ) {
                                Text("USAGE ACCESS", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (!permissionStatus.fineLocationGranted) {
                            OutlinedButton(
                                onClick = onRequestLocationAccess,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, palette.ink)
                            ) {
                                Text("LOCATION", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                            }
                        }
                    }
                }
            }
        }

        // Hero Card with custom Canvas Dial
        item {
            HairlineCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SCREEN TIME TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(DashSpace.sm))
                        Text(
                            text = DateKeys.formatDuration(todayScreenTimeMs),
                            style = TabularMetricStyle.copy(
                                fontSize = 34.sp,
                                color = palette.ink
                            )
                        )
                        Spacer(modifier = Modifier.height(DashSpace.md))
                        Text(
                            text = "MONTH: ${DateKeys.formatDuration(monthScreenTimeMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(DashSpace.xs))
                        Text(
                            text = "YEAR: ${DateKeys.formatDuration(yearScreenTimeMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(DashSpace.lg))
                    HeroTelemetryDial(
                        screenTimeRatio = (todayScreenTimeMs / (8f * 3_600_000f)).coerceIn(0f, 1f),
                        batteryPercent = latestSnapshot?.batteryLevel ?: 0
                    )
                }
            }
        }

        // Data Usage Summary
        item {
            HairlineCard {
                SectionHeader(
                    title = "Data Consumption Today",
                    trailing = DateKeys.formatBytes(totalData)
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                SplitDataMeter(wifiBytes = totalWifi, mobileBytes = totalMobile)
                Spacer(modifier = Modifier.height(DashSpace.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WI-FI: ${DateKeys.formatBytes(totalWifi)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.ink
                    )
                    Text(
                        text = "MOBILE: ${DateKeys.formatBytes(totalMobile)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary
                    )
                }
            }
        }

        // Location & Device Health
        item {
            HairlineCard {
                SectionHeader(
                    title = "Location & Device",
                    trailing = "$todayLocationCount PINGS TODAY"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Last Recorded Position",
                    value = if (latestLocation != null) DateKeys.timeLabel(latestLocation.timestamp) else "--:--",
                    sublabel = latestLocation?.addressLine
                        ?: latestLocation?.let {
                            String.format("%.4f, %.4f", it.latitude, it.longitude)
                        }
                        ?: "No coordinates recorded yet"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Battery & Thermal",
                    value = latestSnapshot?.let { "${it.batteryLevel}%" } ?: "--",
                    sublabel = latestSnapshot?.let {
                        val state = if (it.isCharging) "CHARGING" else "DISCHARGING"
                        "$state · ${it.batteryTempC}°C · ${it.screenUnlockCount} UNLOCKS"
                    }
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Free Storage / RAM",
                    value = latestSnapshot?.let { DateKeys.formatBytes(it.availableStorageBytes) } ?: "--",
                    sublabel = latestSnapshot?.let {
                        "RAM AVAIL: ${DateKeys.formatBytes(it.availableRamBytes)}"
                    }
                )
            }
        }
    }
}
