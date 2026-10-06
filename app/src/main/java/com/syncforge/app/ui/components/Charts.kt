package com.syncforge.app.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncforge.app.data.model.ContributionDay
import com.syncforge.app.data.model.LineSeries
import com.syncforge.app.data.model.SeriesPoint
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import kotlin.math.max
import kotlin.math.min

/**
 * All charts are drawn on Canvas — no chart library, no network. Each animates
 * its data in once on first composition via a shared 0→1 reveal fraction.
 */
@Composable
private fun rememberReveal(durationMillis: Int = 900): Float {
    var play by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { play = true }
    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "chartReveal",
    )
    return progress
}

// ─────────────────────────────────────────────────────────────────────────────
// Bar chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ForgeBarChart(
    points: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    highlightLast: Boolean = true,
    showLabels: Boolean = true,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal()
    if (points.isEmpty()) return
    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)
    var selected by remember { mutableStateOf(if (highlightLast) points.lastIndex else -1) }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().height(height),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEachIndexed { index, point ->
                // Reserve the top ~14% of the plot for the active bar's value label so
                // a tall bar never pushes it out of the card.
                val ratio = ((point.value / maxValue) * reveal * 0.86f).coerceIn(0.02f, 0.86f)
                val isActive = index == selected
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isActive) {
                        Text(
                            point.value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = t.accent,
                        )
                        Spacer(Modifier.height(3.dp))
                    }
                    // fillMaxHeight(fraction) measures against the column, so every bar
                    // grows upward from the shared bottom edge.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (isActive) {
                                    t.accentGradient
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            t.accent.copy(alpha = 0.55f),
                                            t.accent.copy(alpha = 0.18f),
                                        ),
                                    )
                                },
                            )
                            .springPress(pressedScale = 0.92f) { selected = index },
                    )
                }
            }
        }
        if (showLabels) {
            Spacer(Modifier.height(Space.xs))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                points.forEachIndexed { index, point ->
                    Text(
                        point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == selected) t.accent else t.textTertiary,
                        fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Line / area chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ForgeLineChart(
    series: List<LineSeries>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    filled: Boolean = true,
    showGrid: Boolean = true,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal(1_100)
    if (series.isEmpty()) return

    val maxValue = series.flatMap { it.points }.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    val gridColor = t.hairline
    val labels = series.first().points.map { it.label }

    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height

            if (showGrid) {
                repeat(5) { i ->
                    val y = h * i / 4f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f)),
                    )
                }
            }

            series.forEach { line ->
                val color = t.series[line.colorIndex % t.series.size]
                val pts = line.points
                if (pts.size < 2) return@forEach

                // Only draw the revealed prefix so lines "draw on" left to right.
                val visibleCount = (pts.size * reveal).coerceAtLeast(1.02f)
                val path = Path()
                val fill = Path()

                fun pointAt(i: Int): Offset {
                    val x = w * i / (pts.size - 1).toFloat()
                    val y = h - (pts[i].value / maxValue) * h * 0.92f - h * 0.04f
                    return Offset(x, y)
                }

                val whole = visibleCount.toInt().coerceAtMost(pts.size - 1)
                val frac = visibleCount - whole

                path.moveTo(pointAt(0).x, pointAt(0).y)
                fill.moveTo(pointAt(0).x, h)
                fill.lineTo(pointAt(0).x, pointAt(0).y)

                for (i in 1..whole) {
                    val prev = pointAt(i - 1)
                    val cur = pointAt(i)
                    // Cubic smoothing with horizontal control points — a clean "S" between samples.
                    val cx = (prev.x + cur.x) / 2f
                    path.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                    fill.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                }
                if (whole < pts.size - 1 && frac > 0f) {
                    val prev = pointAt(whole)
                    val next = pointAt(whole + 1)
                    val partial = Offset(
                        prev.x + (next.x - prev.x) * frac,
                        prev.y + (next.y - prev.y) * frac,
                    )
                    path.lineTo(partial.x, partial.y)
                    fill.lineTo(partial.x, partial.y)
                }

                val lastX = if (whole < pts.size - 1) {
                    pointAt(whole).x + (pointAt(whole + 1).x - pointAt(whole).x) * frac
                } else {
                    pointAt(pts.size - 1).x
                }
                fill.lineTo(lastX, h)
                fill.close()

                if (filled) {
                    drawPath(
                        path = fill,
                        brush = Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.34f), Color.Transparent),
                        ),
                    )
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )

                // Terminal dot marks the latest value.
                if (reveal > 0.98f) {
                    val last = pointAt(pts.size - 1)
                    drawCircle(color = color.copy(alpha = 0.28f), radius = 9.dp.toPx(), center = last)
                    drawCircle(color = color, radius = 4.dp.toPx(), center = last)
                }
            }
        }

        Spacer(Modifier.height(Space.xs))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = t.textTertiary)
            }
        }
        if (series.size > 1) {
            Spacer(Modifier.height(Space.sm))
            ChartLegend(series.map { it.name to t.series[it.colorIndex % t.series.size] })
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Donut chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ForgeDonutChart(
    points: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    size: Dp = 170.dp,
    centerLabel: String? = null,
    centerValue: String? = null,
    thickness: Dp = 22.dp,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal(1_000)
    val total = points.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.01f)

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            var startAngle = -90f

            // Track sits under the segments so gaps read as intentional.
            drawArc(
                color = t.hairline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            points.forEachIndexed { index, point ->
                val sweep = (point.value / total) * 360f * reveal
                drawArc(
                    color = t.series[index % t.series.size],
                    startAngle = startAngle + 1.5f,
                    sweepAngle = (sweep - 3f).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerValue != null) {
                Text(
                    centerValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.textPrimary,
                )
            }
            if (centerLabel != null) {
                Text(
                    centerLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                )
            }
        }
    }
}

@Composable
fun ChartLegend(
    entries: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
    values: List<String>? = null,
) {
    val t = LocalTokens.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        entries.forEachIndexed { index, (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(Space.xs))
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                if (values != null && index < values.size) {
                    Text(
                        values[index],
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sparkline + ring + contribution grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color? = null,
    height: Dp = 38.dp,
) {
    val t = LocalTokens.current
    val stroke = color ?: t.accent
    if (values.size < 2) return
    val minV = values.min()
    val maxV = values.max()
    val range = (maxV - minV).coerceAtLeast(0.001f)

    Canvas(modifier.fillMaxWidth().height(height)) {
        val path = Path()
        val fill = Path()
        values.forEachIndexed { i, v ->
            val x = size.width * i / (values.size - 1)
            val y = size.height - ((v - minV) / range) * size.height * 0.85f - size.height * 0.08f
            if (i == 0) {
                path.moveTo(x, y)
                fill.moveTo(x, size.height)
                fill.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fill.lineTo(x, y)
            }
        }
        fill.lineTo(size.width, size.height)
        fill.close()
        drawPath(fill, Brush.verticalGradient(listOf(stroke.copy(alpha = 0.28f), Color.Transparent)))
        drawPath(path, stroke, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Circular progress ring with an animated sweep and centered value. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    thickness: Dp = 9.dp,
    color: Color? = null,
    label: String? = null,
    valueText: String? = null,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal(950)
    val target = progress.coerceIn(0f, 1f) * reveal
    val ringColor = color ?: t.accent

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = t.hairlineStrong,
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(ringColor.copy(alpha = 0.55f), ringColor, ringColor),
                ),
                startAngle = -90f,
                sweepAngle = 360f * target,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                valueText ?: "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = t.textPrimary,
            )
            if (label != null) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = t.textTertiary)
            }
        }
    }
}

/** GitHub-style contribution heatmap. Scrolls horizontally when weeks overflow. */
@Composable
fun ContributionGrid(
    days: List<ContributionDay>,
    modifier: Modifier = Modifier,
    cell: Dp = 7.dp,
    gap: Dp = 2.dp,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal(1_200)
    val weeks = (days.maxOfOrNull { it.week } ?: 0) + 1
    val byCell = remember(days) { days.associateBy { it.week to it.weekday } }

    val levelColors = listOf(
        t.hairline,
        t.accent.copy(alpha = 0.28f),
        t.accent.copy(alpha = 0.52f),
        t.accent.copy(alpha = 0.76f),
        t.accent,
    )

    Canvas(
        modifier.height(cell * 7 + gap * 6),
    ) {
        val gapPx = gap.toPx()
        // Fit the whole year into the available width, then use that same step for
        // rows so cells stay square instead of stretching into slivers.
        val step = min(cell.toPx() + gapPx, size.width / weeks)
        val drawSize = (step - gapPx).coerceAtLeast(2f)
        val visibleWeeks = (weeks * reveal).toInt()
        // Centre the 7 rows in the height the caller reserved.
        val yOffset = ((size.height - (7 * step - gapPx)) / 2f).coerceAtLeast(0f)

        for (w in 0 until visibleWeeks) {
            for (d in 0 until 7) {
                val level = byCell[w to d]?.level ?: 0
                drawRoundRect(
                    color = levelColors[level.coerceIn(0, 4)],
                    topLeft = Offset(w * step, yOffset + d * step),
                    size = Size(drawSize, drawSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f),
                )
            }
        }
    }
}

/** Horizontal stacked bar — one row per category, used for language mix. */
@Composable
fun StackedBar(
    points: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
) {
    val t = LocalTokens.current
    val reveal = rememberReveal(900)
    val total = points.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.01f)

    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(Radii.pill),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        points.forEachIndexed { index, point ->
            Box(
                Modifier
                    .weight((point.value / total) * reveal + 0.0001f)
                    .fillMaxSize()
                    .background(t.series[index % t.series.size]),
            )
        }
    }
}

/** Small delta indicator: "+12%" in success/danger tone. */
@Composable
fun DeltaBadge(delta: Float, modifier: Modifier = Modifier, suffix: String = "%") {
    val t = LocalTokens.current
    val positive = delta >= 0
    val color = if (positive) t.success else t.danger
    Row(
        modifier
            .clip(Radii.chip)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            (if (positive) "+" else "") + "${delta.toInt()}$suffix",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/** Draws a rounded-rect grid guide — shared by chart backgrounds. */
internal fun DrawScope.drawChartFrame(color: Color, rect: Rect = Rect(Offset.Zero, size)) {
    drawRoundRect(
        color = color,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1f),
    )
}
