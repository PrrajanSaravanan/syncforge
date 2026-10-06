package com.syncforge.app.ui.screens.projects

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.syncforge.app.data.mock.MockUsers
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.ProjectStatus
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeSearchBar
import com.syncforge.app.ui.components.ProjectCard
import com.syncforge.app.ui.components.PullToRefreshBox
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.ProjectSort
import com.syncforge.app.viewmodel.ProjectsViewModel

@Composable
fun ProjectsScreen(
    onOpenProject: (String) -> Unit,
    onOpenKanban: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: ProjectsViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────────
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
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Workspace", color = t.accent)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Projects",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.textPrimary,
                        )
                    }
                    ForgeIconButton(
                        icon = if (filters.gridMode) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                        contentDescription = if (filters.gridMode) "Switch to list" else "Switch to grid",
                        onClick = viewModel::toggleGrid,
                    )
                    Spacer(Modifier.width(Space.xs))
                    Box {
                        ForgeIconButton(
                            icon = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort projects",
                            onClick = { sortMenuOpen = true },
                        )
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                        ) {
                            ProjectSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option.label,
                                            fontWeight = if (option == filters.sort) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            color = if (option == filters.sort) t.accent else t.textPrimary,
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSort(option)
                                        sortMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Space.xs))
                Text(
                    "${projects.size} of 15 projects · ${projects.count { it.status == ProjectStatus.AtRisk }} at risk",
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textTertiary,
                )

                Spacer(Modifier.height(Space.md))
                ForgeSearchBar(
                    value = filters.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = "Search projects, keys or stack",
                )
                Spacer(Modifier.height(Space.sm))
            }

            // ── Status filters ───────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = Space.gutter),
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                item {
                    ForgeChip(
                        label = "Starred",
                        selected = filters.starredOnly,
                        onClick = viewModel::toggleStarredOnly,
                        icon = Icons.Rounded.Star,
                    )
                }
                item {
                    ForgeChip(
                        label = "All",
                        selected = filters.status == null,
                        onClick = { viewModel.setStatus(null) },
                    )
                }
                items(ProjectStatus.entries) { status ->
                    ForgeChip(
                        label = status.label,
                        selected = filters.status == status,
                        onClick = {
                            viewModel.setStatus(if (filters.status == status) null else status)
                        },
                        dotColor = t.statusColor(status.label),
                    )
                }
            }

            Spacer(Modifier.height(Space.xs))

            // ── Category filters ─────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = Space.gutter),
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                items(viewModel.categories) { category ->
                    ForgeChip(
                        label = category,
                        selected = filters.category == category,
                        onClick = { viewModel.setCategory(category) },
                    )
                }
            }

            Spacer(Modifier.height(Space.sm))

            // ── Content ──────────────────────────────────────────────────────
            PullToRefreshBox(
                refreshing = refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    loading -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = Space.gutter,
                                vertical = Space.xs,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Space.sm),
                        ) {
                            items(5) { SkeletonListCard(lines = 4) }
                        }
                    }

                    projects.isEmpty() -> {
                        EmptyState(
                            icon = if (filters.query.isNotEmpty()) {
                                Icons.Rounded.SearchOff
                            } else {
                                Icons.Rounded.FilterListOff
                            },
                            title = if (filters.query.isNotEmpty()) {
                                "No projects match \"${filters.query}\""
                            } else {
                                "Nothing matches these filters"
                            },
                            message = "Try a different search term, or clear the active filters to see all 15 projects.",
                            actionLabel = "Clear filters",
                            onAction = viewModel::clearFilters,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        // Grid ↔ list swap cross-fades rather than snapping.
                        AnimatedContent(
                            targetState = filters.gridMode,
                            transitionSpec = {
                                fadeIn(tween(260)) togetherWith fadeOut(tween(180))
                            },
                            label = "layoutMode",
                        ) { grid ->
                            if (grid) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        start = Space.gutter,
                                        end = Space.gutter,
                                        bottom = BottomBarInset,
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                                ) {
                                    items(projects, key = { it.id }) { project ->
                                        ProjectCard(
                                            project = project,
                                            members = MockUsers.users(project.memberIds),
                                            compact = true,
                                            onClick = { onOpenProject(project.id) },
                                            onStarClick = { viewModel.toggleStar(project.id) },
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        start = Space.gutter,
                                        end = Space.gutter,
                                        bottom = BottomBarInset,
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                                ) {
                                    items(projects, key = { it.id }) { project ->
                                        ProjectCard(
                                            project = project,
                                            members = MockUsers.users(project.memberIds),
                                            compact = false,
                                            onClick = { onOpenProject(project.id) },
                                            onStarClick = { viewModel.toggleStar(project.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Board shortcut FAB — jumps to the cross-project kanban.
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Space.md, bottom = BottomBarInset - 12.dp)
                .height(56.dp)
                .clip(Radii.pill)
                .background(t.accentGradient)
                .springPress(onClick = onOpenKanban)
                .padding(horizontal = Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.ViewKanban,
                contentDescription = null,
                tint = t.onAccent,
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(Space.xs))
            Text(
                "Open board",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = t.onAccent,
            )
        }
    }
}
