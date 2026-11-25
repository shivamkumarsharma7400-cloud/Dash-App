package com.dash.monitor.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.dash.monitor.data.collector.AppPermissionAudit
import com.dash.monitor.data.local.dao.NotificationAppSummary
import com.dash.monitor.data.local.entity.AppEventEntity
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.data.local.entity.NetworkEventEntity
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.InsightPill
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.ProportionalBarRow
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
    notificationSummary: List<NotificationAppSummary>,
    appEvents: List<AppEventEntity>,
    networkEvents: List<NetworkEventEntity>,
    permissionAudits: List<AppPermissionAudit>,
    permissionStatus: PermissionStatus,
    exportMessage: String?,
    onExportBackupJson: () -> Unit,
    onExportCallsCsv: () -> Unit,
    onScanPermissions: () -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestCorePermissions: () -> Unit,
    onRequestNotificationAccess: () -> Unit
) {
    val palette = LocalDashPalette.current
    val maxNotifs = (notificationSummary.maxOfOrNull { it.notifCount } ?: 1).coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "CONTROL VAULT & AUDIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = "${availableYears.size.coerceAtLeast(1)}Y / ${availableMonths.size}M",
                    style = TabularMetricStyle.copy(fontSize = 34.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                    InsightPill("$totalLocationCount PINGS")
                    InsightPill("$snapshotCount SNAPSHOTS")
                    InsightPill("LOCAL SQLITE")
                }
            }
        }

        // Notification Clutter Analytics
        item {
            HairlineCard {
                SectionHeader(
                    title = "Notification Clutter Today",
                    trailing = "${notificationSummary.sumOf { it.notifCount }} TOTAL"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                if (!permissionStatus.notificationListenerGranted) {
                    Text(
                        text = "Enable Notification Access to track which apps interrupt you most.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.inkSecondary
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    OutlinedButton(
                        onClick = onRequestNotificationAccess,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.ink)
                    ) {
                        Text("ENABLE NOTIFICATION LOGGER", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
                } else if (notificationSummary.isEmpty()) {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.BELL,
                        title = "No notifications logged yet",
                        subtitle = "Incoming notifications will be archived here automatically."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(DashSpace.md)) {
                        notificationSummary.take(6).forEach { item ->
                            ProportionalBarRow(
                                title = item.appName,
                                value = "${item.notifCount}",
                                sublabel = item.packageName,
                                fraction = item.notifCount.toFloat() / maxNotifs.toFloat()
                            )
                        }
                    }
                }
            }
        }

        // Privacy & Permission Auditor
        item {
            HairlineCard {
                SectionHeader(
                    title = "Privacy & Permission Auditor",
                    trailing = "${permissionAudits.size} APPS"
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Text(
                    text = "See which installed apps hold Camera, Mic, Location, Contacts, or Call permissions. Tap any app to revoke.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                OutlinedButton(
                    onClick = onScanPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, palette.hairline)
                ) {
                    Text("SCAN INSTALLED APPS", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                }
                if (permissionAudits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    Column(verticalArrangement = Arrangement.spacedBy(DashSpace.md)) {
                        permissionAudits.take(10).forEach { audit ->
                            MetricValueRow(
                                label = audit.appName,
                                value = "${audit.grantedSensitivePerms.size} PERMS",
                                sublabel = audit.grantedSensitivePerms.joinToString(" · "),
                                modifier = Modifier.clickable { onOpenAppSettings(audit.packageName) }
                            )
                        }
                    }
                }
            }
        }

        // App Install/Update/Network Events
        if (appEvents.isNotEmpty() || networkEvents.isNotEmpty()) {
            item {
                HairlineCard {
                    SectionHeader(title = "System & Network Events", trailing = "RECENT")
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    Column(verticalArrangement = Arrangement.spacedBy(DashSpace.md)) {
                        appEvents.take(5).forEach { ev ->
                            MetricValueRow(
                                label = "${ev.eventType}: ${ev.appName}",
                                value = DateKeys.timeLabel(ev.timestamp),
                                sublabel = ev.packageName
                            )
                        }
                        networkEvents.take(5).forEach { net ->
                            MetricValueRow(
                                label = "NET: ${net.transportType}",
                                value = DateKeys.timeLabel(net.timestamp),
                                sublabel = net.detail
                            )
                        }
                    }
                }
            }
        }

        // Export & Backup
        item {
            HairlineCard {
                SectionHeader(title = "Local Export (JSON & CSV)", trailing = "DOWNLOADS/DASH")
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Text(
                    text = "Export your multi-year archive as JSON or Excel-ready Calls CSV.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)
                ) {
                    Button(
                        onClick = onExportBackupJson,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.invertedBg,
                            contentColor = palette.invertedInk
                        )
                    ) {
                        Text("FULL JSON", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onExportCallsCsv,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.ink)
                    ) {
                        Text("CALLS CSV", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
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

        // System Permissions Status
        item {
            HairlineCard {
                SectionHeader(title = "Dash Permissions", trailing = "STATUS")
                Spacer(modifier = Modifier.height(DashSpace.md))
                MetricValueRow(
                    label = "Usage Access (Screen & Data)",
                    value = if (permissionStatus.usageStatsGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                MetricValueRow(
                    label = "Fine Location",
                    value = if (permissionStatus.fineLocationGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                MetricValueRow(
                    label = "Call Log Access",
                    value = if (permissionStatus.callLogGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                MetricValueRow(
                    label = "Notification Logger",
                    value = if (permissionStatus.notificationListenerGranted) "GRANTED" else "MISSING"
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)
                ) {
                    OutlinedButton(
                        onClick = onRequestUsageAccess,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.hairline)
                    ) {
                        Text("USAGE", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
                    OutlinedButton(
                        onClick = onRequestCorePermissions,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, palette.hairline)
                    ) {
                        Text("LOC & CALLS", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                    }
                }
            }
        }
    }
}
