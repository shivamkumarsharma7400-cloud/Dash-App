package com.dash.monitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dash.monitor.data.local.dao.ContactCallSummary
import com.dash.monitor.data.local.entity.CallRecordEntity
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.InsightPill
import com.dash.monitor.ui.components.KpiTile
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.PeriodSelector
import com.dash.monitor.ui.components.ProportionalBarRow
import com.dash.monitor.ui.components.ReportPeriod
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys

@Composable
fun CallsScreen(
    period: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    calls: List<CallRecordEntity>,
    topContacts: List<ContactCallSummary>
) {
    val palette = LocalDashPalette.current
    val totalSec = calls.sumOf { it.durationSeconds }
    val incoming = calls.count { it.callType == 1 }
    val outgoing = calls.count { it.callType == 2 }
    val missed = calls.count { it.callType == 3 || it.callType == 5 }
    val maxContactSec = (topContacts.maxOfOrNull { it.totalDurationSec } ?: 1L).coerceAtLeast(1L)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "CALLS & TALK TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = DateKeys.formatDuration(totalSec * 1000L),
                    style = TabularMetricStyle.copy(fontSize = 34.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                    InsightPill("${calls.size} CALLS")
                    InsightPill("IN $incoming · OUT $outgoing")
                    if (missed > 0) InsightPill("MISSED $missed")
                }
            }
        }

        item {
            PeriodSelector(selected = period, onSelect = onSelectPeriod)
        }

        if (calls.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DashSpace.md)
                ) {
                    val top = topContacts.firstOrNull()
                    KpiTile(
                        label = "Top Contact",
                        value = top?.contactName?.take(12) ?: "--",
                        insight = top?.let { DateKeys.formatDuration(it.totalDurationSec * 1000L) } ?: "0m",
                        modifier = Modifier.weight(1f)
                    )
                    val avgSec = if (calls.isNotEmpty()) totalSec / calls.size else 0L
                    KpiTile(
                        label = "Avg Call",
                        value = DateKeys.formatDuration(avgSec * 1000L),
                        insight = "${topContacts.size} CONTACTS",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                HairlineCard {
                    SectionHeader(title = "Top Contacts by Talk Time", trailing = "SHARE")
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    Column(verticalArrangement = Arrangement.spacedBy(DashSpace.md)) {
                        topContacts.take(8).forEach { contact ->
                            ProportionalBarRow(
                                title = contact.contactName,
                                value = DateKeys.formatDuration(contact.totalDurationSec * 1000L),
                                sublabel = "${contact.callCount} CALLS · ${contact.missedCount} MISSED · ${contact.phoneNumber}",
                                fraction = contact.totalDurationSec.toFloat() / maxContactSec.toFloat()
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Detailed Call Log", trailing = "TIME & DURATION")
            }

            items(calls.take(80), key = { it.callId }) { call ->
                val typeLabel = when (call.callType) {
                    1 -> "INCOMING"
                    2 -> "OUTGOING"
                    3 -> "MISSED"
                    5 -> "REJECTED"
                    else -> "CALL"
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricValueRow(
                        label = call.contactName,
                        value = DateKeys.formatDuration(call.durationSeconds * 1000L),
                        sublabel = "$typeLabel · ${DateKeys.displayDay(call.dateKey)} ${DateKeys.timeLabel(call.timestamp)} · ${call.phoneNumber}"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
                }
            }
        } else {
            item {
                HairlineCard {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.PHONE,
                        title = "No calls recorded in this period",
                        subtitle = "Grant Call Log permission and tap Sync to archive full call reports."
                    )
                }
            }
        }
    }
}
