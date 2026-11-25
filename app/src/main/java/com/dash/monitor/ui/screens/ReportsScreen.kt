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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionStatus

@Composable
fun ReportsScreen(
    availableMonths: List<String>,
    availableYears: List<String>,
    totalLocationCount: Int,
    snapshotCount: Int,
    latestSnapshot: DeviceSnapshot?,
    permissionStatus: PermissionStatus,
    exportMessage: String?,
    onExportBackup: () -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestLocationAccess: () -> Unit
) {
    val palette = LocalDashPalette.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "ARCHIVE & REPORTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = "${availableYears.size.coerceAtLeast(1)}Y / ${availableMonths.size}M",
                    style = TabularMetricStyle.copy(fontSize = 32.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = "LOCAL SQLITE VAULT · ZERO CLOUD UPLOAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
            }
        }

        item {
            HairlineCard {
                SectionHeader(title = "Local Vault Inventory", trailing = "ROOM DB")
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(label = "Saved Location Pings", value = "$totalLocationCount")
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(label = "Device Health Snapshots", value = "$snapshotCount")
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Archived Months",
                    value = if (availableMonths.isEmpty()) "0" else "${availableMonths.size}",
                    sublabel = availableMonths.take(4).joinToString(" · ") { DateKeys.displayMonth(it) }
                )
            }
        }

        item {
            HairlineCard {
                SectionHeader(title = "Device Telemetry", trailing = "LATEST")
                Spacer(modifier = Modifier.height(DashSpace.md))
                if (latestSnapshot == null) {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.LEDGER,
                        title = "No device snapshot yet",
                        subtitle = "Tap Sync on Overview to record battery, RAM & storage."
                    )
                } else {
                    MetricValueRow(
                        label = "Battery Level",
                        value = "${latestSnapshot.batteryLevel}%",
                        sublabel = "${if (latestSnapshot.isCharging) "CHARGING" else "DISCHARGING"} · ${latestSnapshot.batteryTempC}°C"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    MetricValueRow(
                        label = "Internal Storage Free",
                        value = DateKeys.formatBytes(latestSnapshot.availableStorageBytes),
                        sublabel = "TOTAL ${DateKeys.formatBytes(latestSnapshot.totalStorageBytes)}"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    MetricValueRow(
                        label = "RAM Available",
                        value = DateKeys.formatBytes(latestSnapshot.availableRamBytes),
                        sublabel = "TOTAL ${DateKeys.formatBytes(latestSnapshot.totalRamBytes)}"
                    )
                }
            }
        }

        item {
            HairlineCard {
                SectionHeader(title = "Local Backup Export", trailing = "JSON")
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Text(
                    text = "Export your entire multi-month and yearly history (location, screen time, data usage, device health) to Downloads/Dash.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                Button(
                    onClick = onExportBackup,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.invertedBg,
                        contentColor = palette.invertedInk
                    )
                ) {
                    Text("EXPORT FULL LOCAL BACKUP", style = MaterialTheme.typography.labelSmall)
                }
                if (exportMessage != null) {
                    Spacer(modifier = Modifier.height(DashSpace.sm))
                    Text(
                        text = exportMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.ink
                    )
                }
            }
        }

        item {
            HairlineCard {
                SectionHeader(title = "Permissions", trailing = "SYSTEM")
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Usage Access (Screen & Data)",
                    value = if (permissionStatus.usageStatsGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Fine Location",
                    value = if (permissionStatus.fineLocationGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Background Location",
                    value = if (permissionStatus.backgroundLocationGranted) "GRANTED" else "LIMITED"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)
                ) {
                    OutlinedButton(
                        onClick = onRequestUsageAccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, palette.hairline)
                    ) {
                        Text("USAGE SETTINGS", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
                    OutlinedButton(
                        onClick = onRequestLocationAccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, palette.hairline)
                    ) {
                        Text("LOCATION", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
                }
            }
        }
    }
}
