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
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.data.repository.MonitorRepository
import com.dash.monitor.ui.components.EmptyArtKind
import com.dash.monitor.ui.components.HairlineCard
import com.dash.monitor.ui.components.InsightPill
import com.dash.monitor.ui.components.LocationCoordinatePlot
import com.dash.monitor.ui.components.MetricValueRow
import com.dash.monitor.ui.components.MonochromeEmptyIllustration
import com.dash.monitor.ui.components.PeriodSelector
import com.dash.monitor.ui.components.ReportPeriod
import com.dash.monitor.ui.components.SectionHeader
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle
import com.dash.monitor.util.DateKeys
import java.util.Locale

@Composable
fun LocationScreen(
    period: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    locations: List<LocationRecord>
) {
    val palette = LocalDashPalette.current
    val distanceKm = MonitorRepository.calculateDistanceKm(locations)
    val maxSpeedKmh = (locations.maxOfOrNull { it.speedMps } ?: 0f) * 3.6f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DashSpace.lg),
        verticalArrangement = Arrangement.spacedBy(DashSpace.lg)
    ) {
        item {
            Column {
                Text(
                    text = "LOCATION & MOVEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(DashSpace.xs))
                Text(
                    text = String.format(Locale.US, "%.1f km", distanceKm),
                    style = TabularMetricStyle.copy(fontSize = 34.sp, color = palette.ink)
                )
                Spacer(modifier = Modifier.height(DashSpace.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(DashSpace.sm)) {
                    InsightPill("${locations.size} PINGS")
                    InsightPill(String.format(Locale.US, "MAX %.0f KM/H", maxSpeedKmh))
                    InsightPill("100% LOCAL")
                }
            }
        }

        item {
            PeriodSelector(selected = period, onSelect = onSelectPeriod)
        }

        if (locations.isNotEmpty()) {
            item {
                HairlineCard {
                    SectionHeader(
                        title = "Coordinate Trace",
                        trailing = "${locations.size} PTS"
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    LocationCoordinatePlot(records = locations)
                }
            }

            item {
                SectionHeader(title = "Timeline", trailing = "TIME & PLACE")
            }

            items(locations, key = { it.id }) { loc ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricValueRow(
                        label = loc.addressLine ?: String.format(
                            Locale.US,
                            "%.5f, %.5f",
                            loc.latitude,
                            loc.longitude
                        ),
                        value = "${DateKeys.displayDay(loc.dateKey)} · ${DateKeys.timeLabel(loc.timestamp)}",
                        sublabel = String.format(
                            Locale.US,
                            "%.5f, %.5f · ±%.0fm · %s",
                            loc.latitude,
                            loc.longitude,
                            loc.accuracyMeters,
                            loc.provider.uppercase()
                        )
                    )
                    Spacer(modifier = Modifier.height(DashSpace.md))
                    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
                }
            }
        } else {
            item {
                HairlineCard {
                    MonochromeEmptyIllustration(
                        kind = EmptyArtKind.COMPASS,
                        title = "No coordinates in this period",
                        subtitle = "Enable Location permission to log movement locally."
                    )
                }
            }
        }
    }
}
