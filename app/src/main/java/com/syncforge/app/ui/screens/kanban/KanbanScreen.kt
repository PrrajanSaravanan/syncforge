package com.syncforge.app.ui.screens.kanban

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.Priority
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeSearchBar
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.TaskCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.KanbanViewModel

/** Column identity: accent colour and a WIP soft-limit used for the header pill. */
private fun columnAccent(status: TaskStatus, series: List<Color>): Color = when (status) {
    TaskStatus.Todo -> series[6]
    TaskStatus.InProgress -> series[0]
    TaskStatus.Review -> series[2]
    TaskStatus.Completed -> series[3]
}

@Composable
fun KanbanScreen(
    initialProjectId: String?,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: KanbanViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val columns by viewModel.columns.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    var filtersOpen by remember { mutableStateOf(false) }

    // Deep link from a project screen scopes the board to that project.
    LaunchedEffect(initialProjectId) {
        if (initialProjectId != null) viewModel.setProject(initialProjectId)
    }

    val activeProject = filters.projectId?.let { viewModel.project(it) }
    val totalVisible = columns.values.sumOf { it.size }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ForgeIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, contentDescription = "Back")
                    Spacer(Modifier.width(Space.sm))
                    Column(Modifier.weight(1f)) {
                        Eyebrow(activeProject?.key ?: "All projects", color = t.accent)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Board",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.textPrimary,
                        )
                    }
                    ForgeIconButton(
                        icon = Icons.Rounded.FilterAlt,
                        contentDescription = "Filters",
                        tint = if (filtersOpen || filters.priority != null || filters.mineOnly) {
                            t.accent
                        } else {
                            null
                        },
                        onClick = { filtersOpen = !filtersOpen },
                    )
                }

                Spacer(Modifier.height(Space.xxs))
                Text(
                    "$totalVisible tasks · ${columns[TaskStatus.InProgress]?.size ?: 0} in flight",
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textTertiary,
                )

                Spacer(Modifier.height(Space.sm))
                ForgeSearchBar(
                    value = filters.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = "Search tasks or refs",
                )
                Spacer(Modifier.height(Space.sm))

                AnimatedVisibility(
                    visible = filtersOpen,
                    enter = fadeIn(tween(200)) + expandVertically(tween(240)),
                    exit = fadeOut(tween(140)) + shrinkVertically(tween(180)),
                ) {
                    Column {
                        Eyebrow("Priority")
                        Spacer(Modifier.height(Space.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                            ForgeChip(
                                label = "Mine",
                                selected = filters.mineOnly,
                                onClick = viewModel::toggleMineOnly,
                            )
                            Priority.entries.forEach { p ->
                                ForgeChip(
                                    label = p.label,
                                    selected = filters.priority == p,
                                    dotColor = t.priorityColor(p.label),
                                    onClick = {
                                        viewModel.setPriority(if (filters.priority == p) null else p)
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(Space.sm))
                        Eyebrow("Project")
                        Spacer(Modifier.height(Space.xs))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                            item {
                                ForgeChip(
                                    label = "All",
                                    selected = filters.projectId == null,
                                    onClick = { viewModel.setProject(null) },
                                )
                            }
                            items(projects, key = { it.id }) { p ->
                                ForgeChip(
                                    label = p.key,
                                    selected = filters.projectId == p.id,
                                    onClick = {
                                        viewModel.setProject(if (filters.projectId == p.id) null else p.id)
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(Space.sm))
                    }
                }
            }

            // ── Board ────────────────────────────────────────────────────────
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    items(3) {
                        Column(
                            Modifier.width(276.dp),
                            verticalArrangement = Arrangement.spacedBy(Space.xs),
                        ) {
                            repeat(3) { SkeletonListCard(lines = 3) }
                        }
                    }
                }
            } else if (totalVisible == 0) {
                EmptyState(
                    icon = Icons.Rounded.Inbox,
                    title = "No tasks on this board",
                    message = "Nothing matches the current filters. Clear them to see all 100 tasks across the workspace.",
                    actionLabel = "Clear filters",
                    onAction = viewModel::clearFilters,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    modifier = Modifier.weight(1f),
                ) {
                    items(TaskStatus.entries, key = { it.name }) { status ->
                        KanbanColumn(
                            status = status,
                            tasks = columns[status].orEmpty(),
                            accent = columnAccent(status, t.series),
                            onTaskClick = viewModel::openTask,
                            resolveAssignees = { ids -> ids.map(viewModel::user) },
                            projectKeyFor = { id -> viewModel.project(id)?.key },
                            onAdd = { onMessage("Task creation is not wired up in this prototype") },
                            modifier = Modifier.width(276.dp),
                        )
                    }
                }
            }
        }

        // Detail sheet for the tapped card.
        selectedTask?.let { task ->
            TaskDetailSheet(
                task = task,
                assignees = task.assigneeIds.map(viewModel::user),
                reporter = viewModel.user(task.reporterId),
                projectName = viewModel.project(task.projectId)?.name.orEmpty(),
                onDismiss = viewModel::closeTask,
                onMove = { status ->
                    viewModel.moveTask(task.id, status)
                    onMessage("Moved ${task.ref} to ${status.label}")
                },
                onToggleChecklist = { itemId -> viewModel.toggleChecklistItem(task.id, itemId) },
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    status: TaskStatus,
    tasks: List<Task>,
    accent: Color,
    onTaskClick: (String) -> Unit,
    resolveAssignees: (List<String>) -> List<com.syncforge.app.data.model.User>,
    projectKeyFor: (String) -> String?,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current

    Column(
        modifier
            .fillMaxHeight()
            .clip(Radii.cardLarge)
            .background(t.surface1.copy(alpha = if (t.isDark) 0.55f else 1f))
            .padding(Space.xs),
    ) {
        // Column header: colour rail, name, count and a WIP hint.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.xs, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(Space.xs))
            Text(
                status.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = t.textPrimary,
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .clip(Radii.pill)
                    .background(accent.copy(alpha = 0.16f))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(
                    "${tasks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add task to ${status.label}",
                tint = t.textTertiary,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .springPress(pressedScale = 0.85f, onClick = onAdd)
                    .padding(6.dp),
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(Radii.pill)
                .background(
                    Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.1f))),
                ),
        )

        Spacer(Modifier.height(Space.xs))

        if (tasks.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(t.hairline),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Inbox,
                        null,
                        tint = t.textTertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.height(Space.xs))
                Text(
                    "Nothing in ${status.label.lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Space.xs),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        assignees = resolveAssignees(task.assigneeIds),
                        projectKey = projectKeyFor(task.projectId),
                        expandable = false,
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
