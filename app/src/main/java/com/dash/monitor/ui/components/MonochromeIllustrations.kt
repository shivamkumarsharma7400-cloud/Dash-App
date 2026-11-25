package com.dash.monitor.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dash.monitor.data.local.entity.HourlyUsageEntity
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.LocalDashPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HeroTelemetryDial(
    screenTimeRatio: Float,
    batteryPercent: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    val animScreen by animateFloatAsState(
        targetValue = screenTimeRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "screenDial"
    )
    val animBattery by animateFloatAsState(
        targetValue = (batteryPercent.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "batteryDial"
    )

    Canvas(modifier = modifier.size(124.dp)) {
        val strokeOuter = 6.dp.toPx()
        val strokeInner = 2.5.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val radiusOuter = (size.minDimension / 2f) - strokeOuter
        val radiusMid = radiusOuter - 12.dp.toPx()

        for (i in 0 until 24) {
            val angleRad = Math.toRadians((i * 15.0) - 90.0)
            val tickLen = if (i % 6 == 0) 6.dp.toPx() else 3.dp.toPx()
            val startR = radiusOuter + 2.dp.toPx()
            val endR = startR - tickLen
            drawLine(
                color = if (i % 6 == 0) palette.ink else palette.hairline,
                start = Offset(
                    center.x + (startR * cos(angleRad)).toFloat(),
                    center.y + (startR * sin(angleRad)).toFloat()
                ),
                end = Offset(
                    center.x + (endR * cos(angleRad)).toFloat(),
                    center.y + (endR * sin(angleRad)).toFloat()
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawArc(
            color = palette.hairline,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - radiusMid, center.y - radiusMid),
            size = Size(radiusMid * 2, radiusMid * 2),
            style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
        )

        drawArc(
            color = palette.ink,
            startAngle = -90f,
            sweepAngle = animScreen * 360f,
            useCenter = false,
            topLeft = Offset(center.x - radiusMid, center.y - radiusMid),
            size = Size(radiusMid * 2, radiusMid * 2),
            style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
        )

        val radiusInner = radiusMid - 13.dp.toPx()
        drawCircle(
            color = palette.hairline,
            radius = radiusInner,
            center = center,
            style = Stroke(width = strokeInner)
        )
        drawArc(
            color = palette.inkSecondary,
            startAngle = -90f,
            sweepAngle = animBattery * 360f,
            useCenter = false,
            topLeft = Offset(center.x - radiusInner, center.y - radiusInner),
            size = Size(radiusInner * 2, radiusInner * 2),
            style = Stroke(width = strokeInner, cap = StrokeCap.Round)
        )

        drawCircle(color = palette.ink, radius = 3.5.dp.toPx(), center = center)
    }
}

@Composable
fun SmoothAreaSparkline(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    if (points.size < 2) return

    val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minVal = (points.minOrNull() ?: 0f)
    val span = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        val w = size.width
        val h = size.height - 8.dp.toPx()
        val stepX = w / (points.size - 1).coerceAtLeast(1)

        val linePath = Path()
        val fillPath = Path()

        val coords = points.mapIndexed { i, v ->
            val x = i * stepX
            val y = h - (((v - minVal) / span) * (h - 10.dp.toPx()))
            Offset(x, y)
        }

        coords.forEachIndexed { idx, pt ->
            if (idx == 0) {
                linePath.moveTo(pt.x, pt.y)
                fillPath.moveTo(pt.x, h)
                fillPath.lineTo(pt.x, pt.y)
            } else {
                val prev = coords[idx - 1]
                val midX = (prev.x + pt.x) / 2f
                linePath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                fillPath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
            }
        }
        fillPath.lineTo(coords.last().x, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(palette.ink.copy(alpha = 0.22f), palette.ink.copy(alpha = 0.0f))
            )
        )
        drawPath(
            path = linePath,
            color = palette.ink,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = palette.ink,
            radius = 4.dp.toPx(),
            center = coords.last()
        )
    }
}

@Composable
fun HourlyHeatmapStrip(
    buckets: List<HourlyUsageEntity>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    val map = buckets.associateBy { it.hour }
    val maxMs = (buckets.maxOfOrNull { it.foregroundTimeMs } ?: 1L).coerceAtLeast(1L)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (hr in 0..23) {
                val ms = map[hr]?.foregroundTimeMs ?: 0L
                val ratio = (ms.toFloat() / maxMs.toFloat()).coerceIn(0f, 1f)
                val alpha = if (ms <= 0L) 0.08f else (0.20f + ratio * 0.80f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.ink.copy(alpha = alpha))
                )
            }
        }
        Spacer(modifier = Modifier.height(DashSpace.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("00:00", style = MaterialTheme.typography.labelSmall, color = palette.inkSecondary)
            Text("12:00", style = MaterialTheme.typography.labelSmall, color = palette.inkSecondary)
            Text("23:00", style = MaterialTheme.typography.labelSmall, color = palette.inkSecondary)
        }
    }
}

@Composable
fun MonochromeBarChart(
    values: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    if (values.isEmpty()) return

    val maxVal = (values.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
    ) {
        val count = values.size
        val slotWidth = size.width / count.coerceAtLeast(1)
        val barWidth = (slotWidth * 0.56f).coerceIn(5.dp.toPx(), 24.dp.toPx())
        val baselineY = size.height - 2.dp.toPx()

        for (step in 1..3) {
            val y = baselineY - (baselineY * (step / 4f))
            drawLine(
                color = palette.hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }

        drawLine(
            color = palette.inkSecondary,
            start = Offset(0f, baselineY),
            end = Offset(size.width, baselineY),
            strokeWidth = 1.dp.toPx()
        )

        values.forEachIndexed { index, (_, v) ->
            val normalized = (v / maxVal).coerceIn(0f, 1f)
            val barHeight = (baselineY - 10.dp.toPx()) * normalized
            val left = (index * slotWidth) + (slotWidth - barWidth) / 2f
            val top = baselineY - barHeight
            drawRoundRect(
                color = if (index == values.lastIndex) palette.ink else palette.inkSecondary.copy(alpha = 0.65f),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

@Composable
fun SplitDataMeter(
    wifiBytes: Long,
    mobileBytes: Long,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    val total = (wifiBytes + mobileBytes).coerceAtLeast(1L)
    val animRatio by animateFloatAsState(
        targetValue = wifiBytes.toFloat() / total.toFloat(),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "splitMeter"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        drawRect(color = palette.hairline, size = size)
        val wifiWidth = size.width * animRatio
        if (wifiWidth > 0f) {
            drawRect(color = palette.ink, size = Size(wifiWidth, size.height))
        }
        val mobileWidth = size.width - wifiWidth
        if (mobileWidth > 0f) {
            drawRect(
                color = palette.inkSecondary.copy(alpha = 0.4f),
                topLeft = Offset(wifiWidth, 0f),
                size = Size(mobileWidth, size.height)
            )
        }
    }
}

@Composable
fun LocationCoordinatePlot(
    records: List<LocationRecord>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(184.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val cols = 6
        val rows = 4
        for (c in 1 until cols) {
            val x = size.width * (c / cols.toFloat())
            drawLine(palette.hairline, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        }
        for (r in 1 until rows) {
            val y = size.height * (r / rows.toFloat())
            drawLine(palette.hairline, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        drawRect(palette.hairline, size = size, style = Stroke(width = 1.dp.toPx()))

        if (records.isEmpty()) return@Canvas

        val minLat = records.minOf { it.latitude }
        val maxLat = records.maxOf { it.latitude }
        val minLng = records.minOf { it.longitude }
        val maxLng = records.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).coerceAtLeast(0.001)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.001)
        val pad = 18.dp.toPx()

        val ordered = records.asReversed()
        val path = Path()
        ordered.forEachIndexed { idx, rec ->
            val x = pad + (((rec.longitude - minLng) / lngSpan).toFloat() * (size.width - pad * 2))
            val y = (size.height - pad) - (((rec.latitude - minLat) / latSpan).toFloat() * (size.height - pad * 2))
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path = path, color = palette.inkSecondary, style = Stroke(width = 1.5.dp.toPx()))

        ordered.forEachIndexed { idx, rec ->
            val x = pad + (((rec.longitude - minLng) / lngSpan).toFloat() * (size.width - pad * 2))
            val y = (size.height - pad) - (((rec.latitude - minLat) / latSpan).toFloat() * (size.height - pad * 2))
            val isLatest = idx == ordered.lastIndex
            drawCircle(
                color = if (isLatest) palette.ink else palette.inkSecondary,
                radius = if (isLatest) 5.dp.toPx() else 2.5.dp.toPx(),
                center = Offset(x, y)
            )
            if (isLatest) {
                drawCircle(
                    color = palette.ink,
                    radius = 11.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

enum class EmptyArtKind { COMPASS, HOURGLASS, SIGNAL, LEDGER, PHONE, BELL }

@Composable
fun MonochromeEmptyIllustration(
    kind: EmptyArtKind,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalDashPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DashSpace.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(72.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val stroke = Stroke(width = 1.5.dp.toPx())
                when (kind) {
                    EmptyArtKind.COMPASS -> {
                        drawCircle(palette.hairline, radius = 30.dp.toPx(), center = c, style = stroke)
                        drawCircle(palette.ink, radius = 4.dp.toPx(), center = c, style = stroke)
                        drawLine(palette.ink, Offset(c.x, c.y - 24.dp.toPx()), Offset(c.x, c.y - 8.dp.toPx()), 1.5.dp.toPx())
                        drawLine(palette.inkSecondary, Offset(c.x, c.y + 8.dp.toPx()), Offset(c.x, c.y + 24.dp.toPx()), 1.5.dp.toPx())
                    }
                    EmptyArtKind.HOURGLASS -> {
                        val w = 18.dp.toPx()
                        val h = 22.dp.toPx()
                        val p = Path().apply {
                            moveTo(c.x - w, c.y - h)
                            lineTo(c.x + w, c.y - h)
                            lineTo(c.x - w, c.y + h)
                            lineTo(c.x + w, c.y + h)
                            close()
                        }
                        drawPath(p, palette.ink, style = stroke)
                    }
                    EmptyArtKind.SIGNAL -> {
                        for (i in 1..4) {
                            val barH = (i * 10).dp.toPx()
                            val x = (c.x - 24.dp.toPx()) + (i * 12.dp.toPx())
                            drawLine(
                                color = if (i <= 2) palette.ink else palette.hairline,
                                start = Offset(x, c.y + 20.dp.toPx()),
                                end = Offset(x, c.y + 20.dp.toPx() - barH),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                    EmptyArtKind.LEDGER -> {
                        drawRect(palette.hairline, Offset(c.x - 22.dp.toPx(), c.y - 26.dp.toPx()), Size(44.dp.toPx(), 52.dp.toPx()), style = stroke)
                        for (row in -1..1) {
                            val y = c.y + (row * 12.dp.toPx())
                            drawLine(palette.ink, Offset(c.x - 14.dp.toPx(), y), Offset(c.x + 14.dp.toPx(), y), 1.5.dp.toPx())
                        }
                    }
                    EmptyArtKind.PHONE -> {
                        drawRoundRect(palette.ink, Offset(c.x - 16.dp.toPx(), c.y - 26.dp.toPx()), Size(32.dp.toPx(), 52.dp.toPx()), CornerRadius(6.dp.toPx()), style = stroke)
                        drawCircle(palette.inkSecondary, 2.5.dp.toPx(), Offset(c.x, c.y + 18.dp.toPx()))
                    }
                    EmptyArtKind.BELL -> {
                        drawArc(palette.ink, 180f, 180f, false, Offset(c.x - 18.dp.toPx(), c.y - 16.dp.toPx()), Size(36.dp.toPx(), 32.dp.toPx()), style = stroke)
                        drawLine(palette.ink, Offset(c.x - 22.dp.toPx(), c.y + 10.dp.toPx()), Offset(c.x + 22.dp.toPx(), c.y + 10.dp.toPx()), 1.5.dp.toPx())
                        drawCircle(palette.ink, 3.dp.toPx(), Offset(c.x, c.y + 18.dp.toPx()))
                    }
                }
            }
            Spacer(modifier = Modifier.height(DashSpace.md))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
            Spacer(modifier = Modifier.height(DashSpace.xs))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.inkSecondary)
        }
    }
}
