package com.syncforge.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.ChartLegend
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeBarChart
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeDonutChart
import com.syncforge.app.ui.components.ForgeLineChart
import com.syncforge.app.ui.components.ForgeProgressBar
import com.syncforge.app.ui.components.MiniStat
import com.syncforge.app.ui.components.PullToRefreshBox
import com.syncforge.app.ui.components.SectionHeader
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.StackedBar
import com.syncforge.app.ui.components.StatCard
import com.syncforge.app.ui.components.StatusTag
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    onOpenActivity: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: AnalyticsViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val data = state.data

    PullToRefreshBox(
        refreshing = state.refreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = BottomBarInset),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(t.accent.copy(alpha = 0.10f), Color.Transparent),
                            ),
                        )
                        .statusBarsPadding()
                        .padding(horizontal = Space.gutter)
                        .padding(top = Space.sm),
                ) {
                    Eyebrow("Insights", color = t.accent)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Delivery metrics across 15 projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = t.textTertiary,
                    )
                    Spacer(Modifier.height(Space.sm))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        items(viewModel.ranges) { range ->
                            ForgeChip(
                                label = range,
                                selected = state.range == range,
                                onClick = { viewModel.setRange(range) },
                            )
                        }
                    }
                }
            }

            if (state.loading || data == null) {
                item {
                    Column(
                        Modifier.padding(horizontal = Space.gutter),
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                    ) {
                        repeat(4) { SkeletonListCard(lines = 5) }
                    }
                }
            } else {
                // ── Headline DORA-style metrics ──────────────────────────────
                item {
                    Column(
                        Modifier.padding(horizontal = Space.gutter),
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            StatCard(
                                label = "Cycle time",
                                value = "${data.cycleTimeDays}d",
                                icon = Icons.Rounded.Timer,
                                accent = t.series[0],
                                delta = -18f,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Review wait",
                                value = "${data.reviewTimeHours}h",
                                icon = Icons.Rounded.Speed,
                                accent = t.series[4],
                                delta = 6f,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            StatCard(
                                label = "Merge rate",
                                value = "${(data.mergeRate * 100).toInt()}%",
                                icon = Icons.Rounded.RocketLaunch,
                                accent = t.success,
                                spark = data.deployFrequency.map { it.value },
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Deploys / wk",
                                value = "${data.deployFrequency.last().value.toInt()}",
                                icon = Icons.Rounded.RocketLaunch,
                                accent = t.series[2],
                                delta = 24f,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Weekly productivity ──────────────────────────────────────
                item {
                    ChartCard(
                        title = "Weekly productivity",
                        subtitle = "Tasks closed per day",
                    ) {
                        ForgeBarChart(points = data.weeklyProductivity, height = 150.dp)
                    }
                }

                // ── Sprint velocity ──────────────────────────────────────────
                item {
                    ChartCard(
                        title = "Sprint velocity",
                        subtitle = "Story points completed across the last 7 sprints",
                    ) {
                        ForgeBarChart(points = data.sprintVelocity, height = 150.dp)
                    }
                }

                // ── Burndown ─────────────────────────────────────────────────
                item {
                    ChartCard(
                        title = "Sprint 24 burndown",
                        subtitle = "Actual vs. ideal remaining points",
                    ) {
                        ForgeLineChart(series = data.burndown, height = 170.dp)
                    }
                }

                // ── Task distribution ────────────────────────────────────────
                item {
                    ChartCard(
                        title = "Task completion",
                        subtitle = "Distribution across all 100 tracked tasks",
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ForgeDonutChart(
                                points = data.taskDistribution,
                                size = 150.dp,
                                centerValue = "100",
                                centerLabel = "tasks",
                            )
                            Spacer(Modifier.width(Space.md))
                            ChartLegend(
                                entries = data.taskDistribution.mapIndexed { i, p ->
                                    p.label to t.series[i % t.series.size]
                                },
                                values = data.taskDistribution.map { "${it.value.toInt()}" },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Language mix ─────────────────────────────────────────────
                item {
                    ChartCard(
                        title = "Codebase mix",
                        subtitle = "Share of committed lines by language",
                    ) {
                        Column {
                            StackedBar(points = data.languageMix, height = 16.dp)
                            Spacer(Modifier.height(Space.sm))
                            ChartLegend(
                                entries = data.languageMix.mapIndexed { i, p ->
                                    p.label to t.series[i % t.series.size]
                                },
                                values = data.languageMix.map { "${it.value.toInt()}%" },
                            )
                        }
                    }
                }

                // ── Sprint reports ───────────────────────────────────────────
                item {
                    SectionHeader(
                        "Sprint reports",
                        subtitle = "Committed vs. completed",
                        actionLabel = "Activity",
                        onAction = onOpenActivity,
                        modifier = Modifier.padding(horizontal = Space.gutter),
                    )
                }
                items(data.sprintReports, key = { it.name }) { report ->
                    ForgeCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.gutter),
                        onClick = {
                            onMessage("${report.name}: ${report.completed}/${report.committed} points")
                        },
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        report.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = t.textPrimary,
                                    )
                                    Text(
                                        report.dateRange,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = t.textTertiary,
                                    )
                                }
                                val ratio = report.completed.toFloat() / report.committed
                                StatusTag(
                                    "${(ratio * 100).toInt()}%",
                                    if (ratio >= 0.9f) t.success else t.warning,
                                )
                            }
                            Spacer(Modifier.height(Space.sm))
                            ForgeProgressBar(
                                progress = report.completed.toFloat() / report.committed,
                                height = 7.dp,
                            )
                            Spacer(Modifier.height(Space.sm))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                MiniStat("Committed", "${report.committed}")
                                MiniStat("Completed", "${report.completed}")
                                MiniStat("Carried", "${report.carriedOver}")
                            }
                        }
                    }
                }

                // ── Throughput leaders ───────────────────────────────────────
                item {
                    SectionHeader(
                        "Throughput leaders",
                        subtitle = "Relative delivery rate vs. team median",
                        modifier = Modifier.padding(horizontal = Space.gutter),
                    )
                }
                item {
                    ForgeCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.gutter),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                            state.throughputLeaders.forEachIndexed { index, user ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == 0) {
                                                    t.warning.copy(alpha = 0.2f)
                                                } else {
                                                    t.hairline
                                                },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) t.warning else t.textTertiary,
                                        )
                                    }
                                    Spacer(Modifier.width(Space.xs))
                                    ForgeAvatar(user, size = 30.dp)
                                    Spacer(Modifier.width(Space.xs))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            user.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = t.textPrimary,
                                            maxLines = 1,
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        ForgeProgressBar(
                                            progress = user.throughput,
                                            height = 4.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    t.series[index % t.series.size].copy(alpha = 0.6f),
                                                    t.series[index % t.series.size],
                                                ),
                                            ),
                                        )
                                    }
                                    Spacer(Modifier.width(Space.xs))
                                    Text(
                                        "${(user.throughput * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = t.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Project progress ─────────────────────────────────────────
                item {
                    ChartCard(
                        title = "Project progress",
                        subtitle = "Top projects by completion",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                            state.projectProgress.forEachIndexed { index, (key, progress) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        key,
                                        style = com.syncforge.app.ui.theme.CodeStyle,
                                        color = t.textSecondary,
                                        modifier = Modifier.width(72.dp),
                                    )
                                    ForgeProgressBar(
                                        progress = progress,
                                        modifier = Modifier.weight(1f),
                                        height = 8.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                t.series[index % t.series.size].copy(alpha = 0.55f),
                                                t.series[index % t.series.size],
                                            ),
                                        ),
                                    )
                                    Spacer(Modifier.width(Space.xs))
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = t.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    val t = LocalTokens.current
    ForgeCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter),
        contentPadding = Space.md,
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = t.textPrimary,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary,
            )
            Spacer(Modifier.height(Space.md))
            content()
        }
    }
}
