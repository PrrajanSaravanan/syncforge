package com.syncforge.app.ui.screens.projects

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.mock.MockUsers
import com.syncforge.app.data.model.Attachment
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.components.AvatarStack
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.ChartLegend
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeDonutChart
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeProgressBar
import com.syncforge.app.ui.components.LabelChip
import com.syncforge.app.ui.components.MiniStat
import com.syncforge.app.ui.components.ProgressRing
import com.syncforge.app.ui.components.ScreenSkeleton
import com.syncforge.app.ui.components.StatusTag
import com.syncforge.app.ui.components.TaskCard
import com.syncforge.app.ui.components.presenceColor
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.screens.activity.ActivityRow
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.ProjectDetailViewModel
import kotlinx.collections.immutable.persistentListOf
import com.syncforge.app.data.model.SeriesPoint

private val tabs = listOf("Overview", "Tasks", "Members", "Timeline", "Activity", "Files")

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
    onOpenKanban: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val viewModel: ProjectDetailViewModel = viewModel(
        factory = ProjectDetailViewModel.factory(projectId),
        key = projectId,
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = LocalTokens.current
    var tabIndex by remember { mutableIntStateOf(0) }

    val project = state.project

    Box(Modifier.fillMaxSize()) {
        if (state.loading || project == null) {
            Column(Modifier.statusBarsPadding().padding(top = Space.xxl)) {
                ScreenSkeleton()
            }
        } else {
            val accent = t.series[project.accentIndex % t.series.size]

            Column(Modifier.fillMaxSize()) {
                ProjectHero(project, accent, state.lead, onBack, onMessage)

                // Tab rail
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                ) {
                    items(tabs.size) { index ->
                        ProjectTab(
                            label = tabs[index],
                            selected = tabIndex == index,
                            accent = accent,
                            onClick = { tabIndex = index },
                        )
                    }
                }

                Spacer(Modifier.height(Space.sm))

                // Tab content slides in the direction of travel.
                AnimatedContent(
                    targetState = tabIndex,
                    transitionSpec = {
                        val forward = targetState > initialState
                        (slideInHorizontally(tween(300)) { if (forward) it / 6 else -it / 6 } +
                            fadeIn(tween(280))) togetherWith
                            (slideOutHorizontally(tween(200)) { if (forward) -it / 8 else it / 8 } +
                                fadeOut(tween(160))) using SizeTransform(clip = false)
                    },
                    label = "projectTab",
                    modifier = Modifier.weight(1f),
                ) { index ->
                    when (index) {
                        0 -> OverviewTab(project, accent, state.statusCounts, state.members)
                        1 -> TasksTab(state.tasks, onOpenKanban)
                        2 -> MembersTab(state.members, state.lead)
                        3 -> TimelineTab(project, accent)
                        4 -> ActivityTab(state.activity)
                        else -> FilesTab(state.attachments, onMessage)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectHero(
    project: Project,
    accent: Color,
    lead: User?,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val t = LocalTokens.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.05f), Color.Transparent),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ForgeIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, contentDescription = "Back")
            Spacer(Modifier.weight(1f))
            ForgeIconButton(
                Icons.Rounded.MoreHoriz,
                { onMessage("Project settings are read-only in this prototype") },
                contentDescription = "More options",
            )
        }

        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    project.key.substringAfter('-').take(2),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(Space.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.textPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        project.key,
                        style = com.syncforge.app.ui.theme.CodeStyle,
                        color = t.textTertiary,
                    )
                    Spacer(Modifier.width(Space.xs))
                    StatusTag(project.status.label, t.statusColor(project.status.label))
                }
            }
        }

        Spacer(Modifier.height(Space.sm))
        Text(
            project.description,
            style = MaterialTheme.typography.bodyMedium,
            color = t.textSecondary,
        )

        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${(project.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = t.textPrimary,
            )
            Spacer(Modifier.width(Space.xs))
            ForgeProgressBar(
                progress = project.progress,
                modifier = Modifier.weight(1f),
                brush = Brush.horizontalGradient(listOf(accent.copy(alpha = 0.65f), accent)),
                height = 7.dp,
            )
            Spacer(Modifier.width(Space.xs))
            Text(
                project.dueLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (project.daysRemaining <= 10) t.warning else t.textTertiary,
            )
        }
    }
}

@Composable
private fun ProjectTab(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val t = LocalTokens.current
    val underline by animateDpAsState(
        targetValue = if (selected) 18.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabUnderline",
    )
    Column(
        Modifier
            .clip(Radii.pill)
            .springPress(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = Space.sm, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) t.textPrimary else t.textTertiary,
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .width(underline)
                .height(2.5.dp)
                .clip(Radii.pill)
                .background(accent),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tabs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(
    project: Project,
    accent: Color,
    statusCounts: Map<TaskStatus, Int>,
    members: List<User>,
) {
    val t = LocalTokens.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                ForgeCard(Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProgressRing(
                            progress = project.healthScore / 100f,
                            size = 78.dp,
                            color = when {
                                project.healthScore >= 80 -> t.success
                                project.healthScore >= 65 -> t.warning
                                else -> t.danger
                            },
                            valueText = "${project.healthScore}",
                            label = "health",
                        )
                    }
                }
                ForgeCard(Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Eyebrow("Sprint")
                        Text(
                            project.sprint,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = t.textPrimary,
                        )
                        Eyebrow("Velocity")
                        Text(
                            "${project.velocity} pts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                }
            }
        }

        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Task distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                    )
                    Spacer(Modifier.height(Space.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Centre total must equal the segments beside it — count the
                        // tracked tasks rather than the project's headline figure.
                        val tracked = statusCounts.values.sum()
                        ForgeDonutChart(
                            points = TaskStatus.entries.map {
                                SeriesPoint(it.label, (statusCounts[it] ?: 0).toFloat())
                            },
                            size = 128.dp,
                            thickness = 17.dp,
                            centerValue = "$tracked",
                            centerLabel = if (tracked == 1) "task" else "tasks",
                        )
                        Spacer(Modifier.width(Space.md))
                        ChartLegend(
                            entries = TaskStatus.entries.mapIndexed { i, s ->
                                s.label to t.series[i % t.series.size]
                            },
                            values = TaskStatus.entries.map { "${statusCounts[it] ?: 0}" },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Delivery signals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                    )
                    Spacer(Modifier.height(Space.sm))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat("Open", "${project.openIssues}")
                        MiniStat("Done", "${project.taskDone}")
                        MiniStat("Coverage", "${project.coverage}%")
                        MiniStat("Budget", "${(project.budgetUsed * 100).toInt()}%")
                    }
                    Spacer(Modifier.height(Space.md))
                    LabeledMeter("Budget consumed", project.budgetUsed, if (project.budgetUsed > 0.75f) t.warning else accent)
                    Spacer(Modifier.height(Space.xs))
                    LabeledMeter("Test coverage", project.coverage / 100f, t.success)
                }
            }
        }

        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                Column {
                    Eyebrow("Repository")
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        project.repo,
                        style = com.syncforge.app.ui.theme.CodeStyle,
                        color = accent,
                    )
                    Spacer(Modifier.height(Space.sm))
                    Eyebrow("Stack")
                    Spacer(Modifier.height(Space.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        project.stack.forEach { LabelChip(it) }
                    }
                    Spacer(Modifier.height(Space.sm))
                    Eyebrow("Team")
                    Spacer(Modifier.height(Space.xs))
                    AvatarStack(members, max = 6, size = 32.dp)
                }
            }
        }
    }
}

@Composable
private fun LabeledMeter(label: String, value: Float, color: Color) {
    val t = LocalTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = t.textSecondary,
            modifier = Modifier.width(120.dp),
        )
        ForgeProgressBar(
            progress = value,
            modifier = Modifier.weight(1f),
            height = 6.dp,
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color)),
        )
        Spacer(Modifier.width(Space.xs))
        Text(
            "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = t.textPrimary,
        )
    }
}

@Composable
private fun TasksTab(tasks: List<Task>, onOpenKanban: () -> Unit) {
    val t = LocalTokens.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radii.card)
                    .background(t.accentMuted)
                    .springPress(pressedScale = 0.99f, onClick = onOpenKanban)
                    .padding(Space.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.ViewKanban, null, tint = t.accent, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(Space.xs))
                Text(
                    "Open in kanban board",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = t.accent,
                )
            }
        }
        items(tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                assignees = MockUsers.users(task.assigneeIds),
                expandable = true,
                onClick = onOpenKanban,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MembersTab(members: List<User>, lead: User?) {
    val t = LocalTokens.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        items(members, key = { it.id }) { user ->
            ForgeCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ForgeAvatar(user, size = 44.dp, showPresence = true)
                    Spacer(Modifier.width(Space.sm))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                user.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = t.textPrimary,
                            )
                            if (user.id == lead?.id) {
                                Spacer(Modifier.width(Space.xs))
                                StatusTag("Lead", t.accent)
                            }
                        }
                        Text(
                            user.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = t.textSecondary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(presenceColor(user.presence)),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${user.presence.label} · ${user.location}",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${user.tasksOpen}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.textPrimary,
                        )
                        Text(
                            "open",
                            style = MaterialTheme.typography.labelSmall,
                            color = t.textTertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineTab(project: Project, accent: Color) {
    val t = LocalTokens.current
    // Milestones derived from the project's own progress, so the phases stay consistent.
    val milestones = listOf(
        Triple("Kickoff & scoping", 0.1f, "Completed"),
        Triple("Architecture review", 0.3f, "Completed"),
        Triple("Implementation", 0.65f, if (project.progress >= 0.65f) "Completed" else "In Progress"),
        Triple("Hardening & QA", 0.85f, if (project.progress >= 0.85f) "Completed" else "Todo"),
        Triple("Release ${project.dueLabel}", 1f, if (project.progress >= 1f) "Completed" else "Todo"),
    )

    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
    ) {
        items(milestones.size) { index ->
            val (label, at, status) = milestones[index]
            val reached = project.progress >= at
            Row(Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(30.dp),
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (reached) accent else t.hairlineStrong),
                    )
                    if (index != milestones.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(62.dp)
                                .background(if (reached) accent.copy(alpha = 0.4f) else t.hairline),
                        )
                    }
                }
                Spacer(Modifier.width(Space.sm))
                Column(Modifier.weight(1f).padding(bottom = Space.md)) {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reached) t.textPrimary else t.textTertiary,
                    )
                    Spacer(Modifier.height(4.dp))
                    StatusTag(status, t.statusColor(status))
                }
            }
        }
    }
}

@Composable
private fun ActivityTab(events: List<com.syncforge.app.data.model.ActivityEvent>) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        items(events, key = { it.id }) { event ->
            ActivityRow(
                event = event,
                actor = MockUsers.user(event.actorId),
                projectName = null,
                showConnector = false,
            )
        }
    }
}

@Composable
private fun FilesTab(attachments: List<Attachment>, onMessage: (String) -> Unit) {
    val t = LocalTokens.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        items(attachments, key = { it.id }) { file ->
            ForgeCard(
                Modifier.fillMaxWidth(),
                onClick = { onMessage("${file.name} is a mock attachment") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = when (file.kind) {
                        "figma" -> Icons.Rounded.Image to t.series[4]
                        "image" -> Icons.Rounded.Image to t.series[1]
                        "log" -> Icons.Rounded.Terminal to t.series[3]
                        "zip" -> Icons.Rounded.AttachFile to t.series[2]
                        else -> Icons.Rounded.Description to t.series[0]
                    }
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(Space.sm))
                    Column(Modifier.weight(1f)) {
                        Text(
                            file.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${file.kind.uppercase()} · ${file.sizeLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = t.textTertiary,
                        )
                    }
                }
            }
        }
    }
}
