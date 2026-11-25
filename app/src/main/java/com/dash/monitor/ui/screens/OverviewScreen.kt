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
import com.dash.monitor.data.local.entity.CallRecordEntity
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.data.local.entity.HourlyUsageEntity
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.data.repository.MonitorRepository
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.HeroTelemetryDial
import com.dash.monitor.ui.components.HourlyHeatmapStrip
import com.dash.monitor.ui.components.InsightPill
import com.dash.monitor.ui.components.KpiTile
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.components.SmoothAreaSparkline
import com.dash.monitor.ui.components.SplitDataMeter
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionStatus
import java.util.Locale

@Composable
fun OverviewScreen(
    todayKey: String,
    todayScreenTimeMs: Long,
    monthScreenTimeMs: Long,
    yearScreenTimeMs: Long,
    hourlyUsage: List<HourlyUsageEntity>,
    todayData: List<DataUsageSummary>,
    todayCalls: List<CallRecordEntity>,
    todayLocations: List<LocationRecord>,
    latestLocation: LocationRecord?,
    latestSnapshot: DeviceSnapshot?,
    todaySnapshots: List<DeviceSnapshot>,
    permissionStatus: PermissionStatus,
    isSyncing: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSyncNow: () -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestCorePermissions: () -> Unit
) {
    val palette = LocalDashPalette.current
    val totalWifi = todayData.sumOf { it.wifiBytes }
    val totalMobile = todayData.sumOf { it.mobileBytes }
    val totalData = totalWifi + totalMobile
    val todayTalkSec = todayCalls.sumOf { it.durationSeconds }
    val distanceKm = MonitorRepository.calculateDistanceKm(todayLocations)
    val peakHour = hourlyUsage.maxByOrNull { it.foregroundTimeMs }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DASH / ANALYTICAL TELEMETRY",
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
                        shape = RoundedCornerShape(8.dp),
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
                        shape = RoundedCornerShape(8.dp),
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

        if (!permissionStatus.allCoreGranted) {
            item {
                HairlineCard {
                    Text(
                        text = "SETUP PERMISSIONS FOR FULL CONTROL",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(DashSpace.sm))
                    Text(
                        text = "Enable Usage Access (Screen & Data) and Location + Call Log so Dash can archive everything locally.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                        if (!permissionStatus.usageStatsGranted) {
                            Button(
                                onClick = onRequestUsageAccess,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.invertedBg,
                                    contentColor = palette.invertedInk
                                )
                            ) {
                                Text("USAGE ACCESS", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (!permissionStatus.fineLocationGranted || !permissionStatus.callLogGranted) {
                            OutlinedButton(
                                onClick = onRequestCorePermissions,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, palette.ink)
                            ) {
                                Text("LOCATION & CALLS", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                            }
                        }
                    }
                }
            }
        }

        // Hero Analytical Card
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
                        Spacer(modifier = Modifier.height(DashSpace.xs))
                        Text(
                            text = DateKeys.formatDuration(todayScreenTimeMs),
                            style = TabularMetricStyle.copy(fontSize = 36.sp, color = palette.ink)
                        )
                        Spacer(modifier = Modifier.height(DashSpace.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.xs)) {
                            InsightPill("M: ${DateKeys.formatDuration(monthScreenTimeMs)}")
                            InsightPill("Y: ${DateKeys.formatDuration(yearScreenTimeMs)}")
                        }
                    }
                    Spacer(modifier = Modifier.width(DashSpace.md))
                    HeroTelemetryDial(
                        screenTimeRatio = (todayScreenTimeMs / (8f * 3_600_000f)).coerceIn(0f, 1f),
                        batteryPercent = latestSnapshot?.batteryLevel ?: 0
                    )
                }
                if (hourlyUsage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DashSpace.lg))
                    SectionHeader(
                        title = "24-Hour Activity Heatmap",
                        trailing = peakHour?.let { "PEAK ${String.format(Locale.US, "%02d:00", it.hour)}" }
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    HourlyHeatmapStrip(buckets = hourlyUsage)
                }
            }
        }

        // 2x2 Analytical KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DashSpace.md)
            ) {
                KpiTile(
                    label = "Talk Time",
                    value = DateKeys.formatDuration(todayTalkSec * 1000L),
                    insight = "${todayCalls.size} CALLS TODAY",
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = "Travelled",
                    value = String.format(Locale.US, "%.1f km", distanceKm),
                    insight = "${todayLocations.size} GPS PINGS",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Data Consumption Split
        item {
            HairlineCard {
                SectionHeader(
                    title = "Network Consumption Today",
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

        // Battery & Device Sparkline
        item {
            HairlineCard {
                SectionHeader(
                    title = "Battery & Device Health",
                    trailing = latestSnapshot?.let { "${it.batteryLevel}% · ${it.batteryTempC}°C" } ?: "--"
                )
                if (todaySnapshots.size >= 2) {
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    SmoothAreaSparkline(
                        points = todaySnapshots.map { it.batteryLevel.toFloat() }
                    )
                }
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Last Position",
                    value = latestLocation?.let { DateKeys.timeLabel(it.timestamp) } ?: "--:--",
                    sublabel = latestLocation?.addressLine
                        ?: latestLocation?.let { String.format(Locale.US, "%.4f, %.4f", it.latitude, it.longitude) }
                        ?: "Waiting for GPS/Network fix"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Free Storage / RAM",
                    value = latestSnapshot?.let { DateKeys.formatBytes(it.availableStorageBytes) } ?: "--",
                    sublabel = latestSnapshot?.let {
                        "RAM ${DateKeys.formatBytes(it.availableRamBytes)} · ${it.screenUnlockCount} UNLOCKS"
                    }
                )
            }
        }
    }
}
