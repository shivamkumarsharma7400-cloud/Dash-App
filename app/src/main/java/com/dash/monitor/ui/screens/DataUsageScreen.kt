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
import com.dash.monitor.data.local.dao.DataPeriodTotal
import com.dash.monitor.data.local.dao.DataUsageSummary
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeBarChart
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.PeriodSelector
import com.dash.monitor.ui.components.ReportPeriod
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.components.SplitDataMeter
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys

@Composable
fun DataUsageScreen(
    period: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    items: List<DataUsageSummary>,
    trend: List<DataPeriodTotal>
) {
    val palette = LocalDashPalette.current
    val totalWifi = items.sumOf { it.wifiBytes }
    val totalMobile = items.sumOf { it.mobileBytes }
    val totalAll = totalWifi + totalMobile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "DATA USAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = DateKeys.formatBytes(totalAll),
                    style = TabularMetricStyle.copy(fontSize = 32.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.md))
                SplitDataMeter(wifiBytes = totalWifi, mobileBytes = totalMobile)
                Spacer(modifier = Modifier.height(DashSpace.sm))
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

        item {
            PeriodSelector(selected = period, onSelect = onSelectPeriod)
        }

        if (trend.isNotEmpty()) {
            item {
                HairlineCard {
                    SectionHeader(
                        title = if (period == ReportPeriod.YEAR) "Monthly Data Trend" else "Daily Data Trend",
                        trailing = "${trend.size} ENTRIES"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    MonochromeBarChart(
                        values = trend.takeLast(14).map {
                            it.periodKey to (it.wifiBytes + it.mobileBytes).toFloat()
                        }
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item {
                HairlineCard {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.SIGNAL,
                        title = "No network traffic archived",
                        subtitle = "Usage Access permission is required to read Wi-Fi and Mobile totals."
                    )
                }
            }
        } else {
            item {
                SectionHeader(title = "Per-App Traffic", trailing = "TOTAL")
            }
            items(items, key = { it.packageName }) { app ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricValueRow(
                        label = app.appName,
                        value = DateKeys.formatBytes(app.totalBytes),
                        sublabel = "WI-FI ${DateKeys.formatBytes(app.wifiBytes)} · MOBILE ${DateKeys.formatBytes(app.mobileBytes)}"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
                }
            }
        }
    }
}
