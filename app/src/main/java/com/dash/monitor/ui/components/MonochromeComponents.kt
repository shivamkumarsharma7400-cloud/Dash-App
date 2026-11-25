package com.dash.monitor.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.theme.TabularMetricStyle

enum class ReportPeriod(val label: String) {
    DAY("DAY"),
    MONTH("MONTH"),
    YEAR("YEAR")
}

@Composable
fun HairlineCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalDashPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, palette.hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(DashSpace.lg),
            content = content
        )
    }
}

@Composable
fun KpiTile(
    label: String,
    value: String,
    insight: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Surface(
        modifier = modifier,
        color = palette.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, palette.hairline),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(DashSpace.lg)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(DashSpace.sm))
            Text(
                text = value,
                style = TabularMetricStyle.copy(fontSize = 22.sp, color = palette.ink),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(DashSpace.xs))
            Text(
                text = insight,
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InsightPill(
    text: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.hairline, RoundedCornerShape(6.dp))
            .padding(horizontal = DashSpace.sm, vertical = DashSpace.xs)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = palette.ink
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(DashSpace.sm))
        HorizontalDivider(thickness = 1.dp, color = palette.hairline)
    }
}

@Composable
fun PeriodSelector(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface, RoundedCornerShape(8.dp))
            .border(1.dp, palette.hairline, RoundedCornerShape(8.dp))
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ReportPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) palette.invertedBg else palette.surface)
                    .clickable { onSelect(period) }
                    .padding(vertical = DashSpace.sm + 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) palette.invertedInk else palette.inkSecondary
                )
            }
        }
    }
}

@Composable
fun ProportionalBarRow(
    title: String,
    value: String,
    sublabel: String,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    val animFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "barRow"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = TabularMetricStyle.copy(fontSize = 14.sp, color = palette.ink)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.ink)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MetricValueRow(
    label: String,
    value: String,
    sublabel: String? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.ink
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
            }
        }
        Text(
            text = value,
            style = TabularMetricStyle.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                color = palette.ink
            )
        )
    }
}
