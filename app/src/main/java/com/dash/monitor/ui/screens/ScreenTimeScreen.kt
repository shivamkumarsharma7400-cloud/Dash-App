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
import com.dash.monitor.data.local.dao.AppUsageSummary
import com.dash.monitor.data.local.dao.PeriodTotal
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeBarChart
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.PeriodSelector
import com.dash.monitor.ui.components.ReportPeriod
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys

@Composable
fun ScreenTimeScreen(
    period: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    apps: List<AppUsageSummary>,
    trend: List<PeriodTotal>
) {
    val palette = LocalDashPalette.current
    val totalMs = apps.sumOf { it.totalTimeMs }
    val totalLaunches = apps.sumOf { it.totalLaunches }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "SCREEN TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = DateKeys.formatDuration(totalMs),
                    style = TabularMetricStyle.copy(fontSize = 32.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = "$totalLaunches APP LAUNCHES · ${apps.size} APPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
            }
        }

        item {
            PeriodSelector(selected = period, onSelect = onSelectPeriod)
        }

        if (trend.isNotEmpty()) {
            item {
                HairlineCard {
                    SectionHeader(
                        title = if (period == ReportPeriod.YEAR) "Monthly Trend" else "Daily Trend",
                        trailing = "${trend.size} ENTRIES"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    MonochromeBarChart(
                        values = trend.takeLast(14).map { it.periodKey to it.totalValue.toFloat() }
                    )
                    Spacer(modifier = Modifier.height(DashSpace.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val first = trend.takeLast(14).firstOrNull()?.periodKey ?: ""
                        val last = trend.takeLast(14).lastOrNull()?.periodKey ?: ""
                        Text(text = first, style = MaterialTheme.typography.labelSmall, color = palette.inkSecondary)
                        Text(text = last, style = MaterialTheme.typography.labelSmall, color = palette.inkSecondary)
                    }
                }
            }
        }

        if (apps.isEmpty()) {
            item {
                HairlineCard {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.HOURGLASS,
                        title = "No screen time logged yet",
                        subtitle = "Grant Usage Access and tap Sync to archive app usage."
                    )
                }
            }
        } else {
            item {
                SectionHeader(title = "Application Breakdown", trailing = "DURATION")
            }
            items(apps, key = { it.packageName }) { app ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricValueRow(
                        label = app.appName,
                        value = DateKeys.formatDuration(app.totalTimeMs),
                        sublabel = "${app.totalLaunches} LAUNCHES · ${app.packageName}"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
                }
            }
        }
    }
}
