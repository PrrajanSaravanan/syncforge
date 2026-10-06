package com.syncforge.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.mock.MockUsers
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.ActivityKind
import com.syncforge.app.data.model.AiInsight
import com.syncforge.app.data.model.NodeStatus
import com.syncforge.app.ui.components.AvatarStack
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeProgressBar
import com.syncforge.app.ui.components.GlassCard
import com.syncforge.app.ui.components.GradientCard
import com.syncforge.app.ui.components.ProgressRing
import com.syncforge.app.ui.components.ProjectCard
import com.syncforge.app.ui.components.PullToRefreshBox
import com.syncforge.app.ui.components.SectionHeader
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.SkeletonStatCard
import com.syncforge.app.ui.components.Sparkline
import com.syncforge.app.ui.components.StatCard
import com.syncforge.app.ui.components.StatusTag
import com.syncforge.app.ui.components.TaskCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.screens.activity.activityIcon
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.MetricStyle
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onOpenProject: (String) -> Unit,
    onOpenKanban: (String?) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenProjects: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // FAB retracts to an icon once the user starts scrolling.
    val fabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 40 }
    }

    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            refreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = BottomBarInset,
                ),
                verticalArrangement = Arrangement.spacedBy(Space.lg),
            ) {
                item { DashboardHeader(state.unreadNotifications, onOpenNotifications, onOpenTeam) }

                if (state.loading) {
                    item { LoadingBlock() }
                } else {
                    item { SprintHeroCard(state.sprintProgress, state.sprintCompleted, state.sprintCommitted, state.workspaceHealth) }
                    item { MetricGrid(state, onOpenProjects, onOpenKanban) }
                    item { QuickActionRow(onOpenKanban, onOpenActivity, onOpenAi, onOpenTeam, onMessage) }
                    item { AiInsightCarousel(state.insights, onOpenAi) }

                    if (state.myTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                "Your queue",
                                subtitle = "${state.myTasks.size} open items assigned to you",
                                actionLabel = "Board",
                                onAction = { onOpenKanban(null) },
                                modifier = Modifier.padding(horizontal = Space.gutter),
                            )
                        }
                        items(state.myTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                assignees = MockUsers.users(task.assigneeIds),
                                projectKey = null,
                                expandable = true,
                                onClick = { onOpenKanban(task.projectId) },
                                modifier = Modifier.padding(horizontal = Space.gutter),
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            "Focus projects",
                            subtitle = "Starred workstreams",
                            actionLabel = "All projects",
                            onAction = onOpenProjects,
                            modifier = Modifier.padding(horizontal = Space.gutter),
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = Space.gutter,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                        ) {
                            items(state.focusProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    members = MockUsers.users(project.memberIds),
                                    compact = true,
                                    onClick = { onOpenProject(project.id) },
                                    modifier = Modifier.width(230.dp),
                                )
                            }
                        }
                    }

                    item { NodeStatusCard(state.nodes, onMessage) }

                    item {
                        SectionHeader(
                            "Recent activity",
                            subtitle = "Across all projects",
                            actionLabel = "View all",
                            onAction = onOpenActivity,
                            modifier = Modifier.padding(horizontal = Space.gutter),
                        )
                    }
                    item { ActivityPreview(state.recentActivity, onOpenActivity) }
                }
            }
        }

        // Expanding FAB → the AI assistant.
        DashboardFab(
            expanded = fabExpanded,
            onClick = onOpenAi,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Space.md, bottom = BottomBarInset - 12.dp),
        )
    }
}

@Composable
private fun DashboardHeader(
    unread: Int,
    onOpenNotifications: () -> Unit,
    onOpenTeam: () -> Unit,
) {
    val t = LocalTokens.current
    val me = MockUsers.currentUser

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(t.accent.copy(alpha = 0.12f), Color.Transparent)),
            )
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ForgeAvatar(me, size = 46.dp, showPresence = true)
            Spacer(Modifier.width(Space.sm))
            Column(Modifier.weight(1f)) {
                Eyebrow("Good morning", color = t.accent)
                Spacer(Modifier.height(2.dp))
                Text(
                    me.name.substringBefore(' '),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.textPrimary,
                )
            }
            ForgeIconButton(
                icon = Icons.Rounded.Groups,
                contentDescription = "Team",
                onClick = onOpenTeam,
            )
            Spacer(Modifier.width(Space.xs))
            ForgeIconButton(
                icon = Icons.Rounded.NotificationsNone,
                contentDescription = "Notifications",
                onClick = onOpenNotifications,
                badge = unread,
            )
        }
        Spacer(Modifier.height(Space.xs))
        Text(
            "${MockUsers.onlineCount} teammates online · Sprint 24 ends in 4 days",
            style = MaterialTheme.typography.bodySmall,
            color = t.textTertiary,
        )
    }
}

@Composable
private fun SprintHeroCard(
    progress: Float,
    completed: Int,
    committed: Int,
    health: Int,
) {
    val t = LocalTokens.current

    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "SPRINT 24",
                        style = com.syncforge.app.ui.theme.EyebrowStyle,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$completed of $committed story points closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                ProgressRing(
                    progress = progress,
                    size = 84.dp,
                    thickness = 8.dp,
                    color = Color.White,
                    label = "sprint",
                )
            }

            Spacer(Modifier.height(Space.md))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                HeroStat("Health", "$health", Modifier.weight(1f))
                HeroStat("Velocity", "47", Modifier.weight(1f))
                HeroStat("Ends in", "4d", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(vertical = Space.xs, horizontal = Space.sm),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun MetricGrid(
    state: com.syncforge.app.viewmodel.DashboardState,
    onOpenProjects: () -> Unit,
    onOpenKanban: (String?) -> Unit,
) {
    val t = LocalTokens.current
    Column(
        Modifier.padding(horizontal = Space.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            StatCard(
                label = "Projects",
                value = "${state.projectCount}",
                icon = Icons.Rounded.Workspaces,
                accent = t.series[0],
                caption = "${state.activeProjects} active",
                onClick = onOpenProjects,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Tasks tracked",
                value = "${state.taskTotal}",
                icon = Icons.Rounded.ViewKanban,
                accent = t.series[1],
                delta = 12f,
                onClick = { onOpenKanban(null) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            StatCard(
                label = "Completed",
                value = "${state.completed}",
                icon = Icons.Rounded.CheckCircleOutline,
                accent = t.success,
                spark = listOf(6f, 9f, 8f, 14f, 18f, 22f, 27f, 31f, 38f),
                onClick = { onOpenKanban(null) },
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Pending",
                value = "${state.pending}",
                icon = Icons.Rounded.PendingActions,
                accent = t.warning,
                caption = "${state.overdue} overdue",
                onClick = { onOpenKanban(null) },
                modifier = Modifier.weight(1f),
            )
        }
        ActiveDevelopersCard(state.activeDevelopers, state.totalDevelopers)
    }
}

@Composable
private fun ActiveDevelopersCard(active: Int, total: Int) {
    val t = LocalTokens.current
    ForgeCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Active developers")
                Spacer(Modifier.height(Space.xxs))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$active", style = MetricStyle, color = t.textPrimary)
                    Text(
                        " / $total",
                        style = MaterialTheme.typography.titleMedium,
                        color = t.textTertiary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(Modifier.height(Space.xs))
                AvatarStack(MockUsers.all.take(7), max = 7, size = 28.dp)
            }
            Spacer(Modifier.width(Space.sm))
            Column(horizontalAlignment = Alignment.End) {
                Sparkline(
                    values = listOf(4f, 6f, 5f, 7f, 8f, 7f, 9f, 8f),
                    color = t.series[3],
                    modifier = Modifier.width(96.dp),
                )
                Spacer(Modifier.height(Space.xs))
                StatusTag("Live", t.success, icon = Icons.Rounded.Bolt)
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    onOpenKanban: (String?) -> Unit,
    onOpenActivity: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenTeam: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val t = LocalTokens.current
    val actions = listOf(
        QuickAction("Board", Icons.Rounded.ViewKanban, t.series[0]) { onOpenKanban(null) },
        QuickAction("Activity", Icons.Rounded.HistoryToggleOff, t.series[1], onOpenActivity),
        QuickAction("Forge AI", Icons.Rounded.AutoAwesome, t.series[2], onOpenAi),
        QuickAction("Team", Icons.Rounded.Groups, t.series[3], onOpenTeam),
        QuickAction("New task", Icons.Rounded.Bolt, t.series[4]) {
            onMessage("Task creation is not wired up in this prototype")
        },
    )

    Column {
        Eyebrow(
            "Quick actions",
            modifier = Modifier.padding(horizontal = Space.gutter),
        )
        Spacer(Modifier.height(Space.xs))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Space.gutter,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            items(actions) { action ->
                Column(
                    Modifier
                        .width(84.dp)
                        .clip(Radii.card)
                        .background(t.surface2)
                        .springPress(onClick = action.onClick)
                        .padding(vertical = Space.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(action.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(action.icon, null, tint = action.color, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        action.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = t.textSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
)

@Composable
private fun AiInsightCarousel(insights: List<AiInsight>, onOpenAi: () -> Unit) {
    val t = LocalTokens.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                null,
                tint = t.series[2],
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Eyebrow("AI insights", color = t.series[2])
        }
        Spacer(Modifier.height(Space.xs))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Space.gutter,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            items(insights, key = { it.id }) { insight ->
                GlassCard(
                    modifier = Modifier.width(290.dp),
                    strong = true,
                    glow = t.series[2],
                    onClick = onOpenAi,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusTag(insight.tag, t.series[2])
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${insight.confidence}% confidence",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                        }
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            insight.headline,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = t.textPrimary,
                        )
                        Spacer(Modifier.height(Space.xxs))
                        Text(
                            insight.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = t.textSecondary,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(Space.xs))
                        ForgeProgressBar(
                            progress = insight.confidence / 100f,
                            height = 4.dp,
                            brush = Brush.horizontalGradient(
                                listOf(t.series[2].copy(alpha = 0.6f), t.series[2]),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeStatusCard(nodes: List<NodeStatus>, onMessage: (String) -> Unit) {
    val t = LocalTokens.current
    val degraded = nodes.count { it.state != "Healthy" }

    ForgeCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Cloud, null, tint = t.textSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Space.xs))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Node status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                    )
                    Text(
                        if (degraded == 0) "All regions healthy" else "$degraded region needs attention",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (degraded == 0) t.success else t.warning,
                    )
                }
                StatusTag(
                    if (degraded == 0) "Operational" else "Degraded",
                    if (degraded == 0) t.success else t.warning,
                )
            }
            Spacer(Modifier.height(Space.sm))
            nodes.take(4).forEach { node ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .springPress(pressedScale = 0.99f) {
                            onMessage("${node.name} · ${node.latencyMs}ms · load ${(node.load * 100).toInt()}%")
                        }
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(t.statusColor(node.state)),
                    )
                    Spacer(Modifier.width(Space.xs))
                    Column(Modifier.weight(1f)) {
                        Text(
                            node.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary,
                            maxLines = 1,
                        )
                        Text(
                            node.region,
                            style = MaterialTheme.typography.labelSmall,
                            color = t.textTertiary,
                        )
                    }
                    ForgeProgressBar(
                        progress = node.load,
                        modifier = Modifier.width(56.dp),
                        height = 5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                t.statusColor(node.state).copy(alpha = 0.6f),
                                t.statusColor(node.state),
                            ),
                        ),
                    )
                    Spacer(Modifier.width(Space.xs))
                    Text(
                        "${node.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (node.latencyMs > 120) t.warning else t.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityPreview(events: List<ActivityEvent>, onOpenActivity: () -> Unit) {
    val t = LocalTokens.current
    ForgeCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter),
        onClick = onOpenActivity,
    ) {
        Column {
            events.forEachIndexed { index, event ->
                val actor = MockUsers.user(event.actorId)
                Row(Modifier.fillMaxWidth()) {
                    // Timeline rail: dot plus a connector that stops at the last row.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(26.dp),
                    ) {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(t.statusColor(if (event.succeeded) "active" else "warning").copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                activityIcon(event.kind),
                                null,
                                tint = if (event.succeeded) t.info else t.warning,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        if (index != events.lastIndex) {
                            Box(
                                Modifier
                                    .width(1.5.dp)
                                    .height(30.dp)
                                    .background(t.hairlineStrong),
                            )
                        }
                    }
                    Spacer(Modifier.width(Space.xs))
                    Column(Modifier.weight(1f).padding(bottom = if (index == events.lastIndex) 0.dp else Space.xs)) {
                        Text(
                            event.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                actor.name.substringBefore(' '),
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textSecondary,
                            )
                            Text(
                                " · ${event.timeLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                            if (event.meta.isNotEmpty()) {
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    event.meta,
                                    style = com.syncforge.app.ui.theme.CodeStyle,
                                    color = t.accent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardFab(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab",
    )

    Row(
        modifier
            .scale(scale)
            .height(56.dp)
            .clip(Radii.pill)
            .background(t.accentGradient)
            .springPress(onClick = onClick)
            .padding(horizontal = if (expanded) Space.md else Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = "Open Forge AI",
            tint = t.onAccent,
            modifier = Modifier.size(23.dp),
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(220)) + androidx.compose.animation.expandHorizontally(tween(260)),
            exit = androidx.compose.animation.fadeOut(tween(160)) +
                androidx.compose.animation.shrinkHorizontally(tween(200)),
        ) {
            Row {
                Spacer(Modifier.width(Space.xs))
                Text(
                    "Ask Forge AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = t.onAccent,
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        Modifier.padding(horizontal = Space.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        SkeletonListCard(lines = 4)
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            SkeletonStatCard(Modifier.weight(1f))
            SkeletonStatCard(Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            SkeletonStatCard(Modifier.weight(1f))
            SkeletonStatCard(Modifier.weight(1f))
        }
        SkeletonListCard()
        SkeletonListCard(lines = 5)
    }
}
