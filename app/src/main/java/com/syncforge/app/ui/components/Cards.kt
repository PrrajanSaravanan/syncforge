package com.syncforge.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.MetricStyle
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

/**
 * Metric tile. Icon badge, big figure, caption and an optional delta or sparkline.
 * This is the densest reusable unit in the app — the dashboard is built from it.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color? = null,
    delta: Float? = null,
    caption: String? = null,
    spark: List<Float>? = null,
    onClick: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    val tone = accent ?: t.accent

    ForgeCard(modifier, contentPadding = Space.md, onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tone.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = tone, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.weight(1f))
                if (delta != null) DeltaBadge(delta)
            }
            Spacer(Modifier.height(Space.sm))
            Text(value, style = MetricStyle, color = t.textPrimary)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = t.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (spark != null) {
                Spacer(Modifier.height(Space.xs))
                Sparkline(spark, color = tone, height = 30.dp)
            }
            if (caption != null) {
                Spacer(Modifier.height(Space.xxs))
                Text(caption, style = MaterialTheme.typography.labelSmall, color = tone)
            }
        }
    }
}

/**
 * Project card. `compact` renders the grid variant (two per row); the full
 * variant adds description, stack chips and the member stack.
 */
@Composable
fun ProjectCard(
    project: Project,
    members: List<User>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
    onStarClick: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    val accent = t.series[project.accentIndex % t.series.size]
    val statusColor = t.statusColor(project.status.label)

    ForgeCard(
        modifier = modifier,
        contentPadding = Space.md,
        borderColor = if (project.starred) accent.copy(alpha = 0.34f) else null,
        onClick = onClick,
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                // Monogram tile carries the project's identity color.
                Box(
                    Modifier
                        .size(if (compact) 32.dp else 46.dp)
                        .clip(RoundedCornerShape(if (compact) 11.dp else 14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(accent, accent.copy(alpha = 0.55f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        project.key.substringAfter('-').take(2),
                        style = if (compact) {
                            MaterialTheme.typography.labelMedium
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(if (compact) Space.xs else Space.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        project.name,
                        // Grid cells are narrow, so titles wrap on whole words and
                        // shrink a step rather than hyphenating mid-word.
                        style = if (compact) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                        maxLines = if (compact) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        project.key,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                }
                if (onStarClick != null) {
                    Icon(
                        if (project.starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = if (project.starred) "Unstar project" else "Star project",
                        tint = if (project.starred) t.warning else t.textTertiary,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .springPress(pressedScale = 0.85f, onClick = onStarClick)
                            .padding(5.dp),
                    )
                }
            }

            if (!compact) {
                Spacer(Modifier.height(Space.sm))
                Text(
                    project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(Space.sm))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusTag(project.status.label, statusColor)
                StatusTag(project.priority.label, t.priorityColor(project.priority.label))
                if (!compact) {
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Schedule, null,
                            tint = if (project.daysRemaining <= 10) t.warning else t.textTertiary,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            project.dueLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (project.daysRemaining <= 10) t.warning else t.textTertiary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${(project.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = t.textPrimary,
                )
                Spacer(Modifier.width(Space.xs))
                ForgeProgressBar(
                    progress = project.progress,
                    modifier = Modifier.weight(1f),
                    brush = Brush.horizontalGradient(listOf(accent.copy(alpha = 0.7f), accent)),
                    height = 6.dp,
                )
            }

            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(members, max = if (compact) 3 else 4, size = 26.dp)
                Spacer(Modifier.weight(1f))
                Text(
                    "${project.taskDone}/${project.taskTotal}",
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                )
            }
        }
    }
}

/**
 * Task card used on the kanban board and in task lists. Tapping the chevron
 * expands the description inline via animateContentSize.
 */
@Composable
fun TaskCard(
    task: Task,
    assignees: List<User>,
    modifier: Modifier = Modifier,
    projectKey: String? = null,
    expandable: Boolean = false,
    onClick: () -> Unit,
) {
    val t = LocalTokens.current
    var expanded by remember { mutableStateOf(false) }
    val priorityColor = t.priorityColor(task.priority.label)
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevron",
    )

    ForgeCard(
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        contentPadding = Space.sm,
        tone = 3,
        onClick = onClick,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Priority is encoded as a colored rail, not color alone —
                // the label text carries the same information.
                Box(
                    Modifier
                        .width(3.dp)
                        .height(15.dp)
                        .clip(Radii.pill)
                        .background(priorityColor),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    task.ref,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = t.textTertiary,
                )
                if (projectKey != null) {
                    Spacer(Modifier.width(6.dp))
                    LabelChip(projectKey)
                }
                Spacer(Modifier.weight(1f))
                if (task.overdue) {
                    StatusTag("Overdue", t.danger)
                } else {
                    Text(
                        task.dueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = t.textPrimary,
                maxLines = if (expanded) 5 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = t.textSecondary,
                    )
                    if (task.branch.isNotEmpty()) {
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            task.branch,
                            style = com.syncforge.app.ui.theme.CodeStyle,
                            color = t.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (task.labels.isNotEmpty()) {
                Spacer(Modifier.height(Space.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    task.labels.take(3).forEach { LabelChip(it) }
                }
            }

            if (task.subtasksTotal > 0) {
                Spacer(Modifier.height(Space.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Subtask completion is progress, not severity — colouring it by
                    // priority made every critical card read as an error state.
                    ForgeProgressBar(
                        progress = task.subtaskProgress,
                        modifier = Modifier.weight(1f),
                        height = 4.dp,
                        brush = Brush.horizontalGradient(
                            if (task.subtaskProgress >= 1f) {
                                listOf(t.success, t.success)
                            } else {
                                listOf(t.accent.copy(alpha = 0.7f), t.accent)
                            },
                        ),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${task.subtasksDone}/${task.subtasksTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                }
            }

            Spacer(Modifier.height(Space.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(assignees, max = 3, size = 22.dp, overlap = 7.dp)
                Spacer(Modifier.weight(1f))

                if (task.commentCount > 0) {
                    MetaCount(Icons.Rounded.ChatBubbleOutline, task.commentCount)
                    Spacer(Modifier.width(Space.xs))
                }
                if (task.attachmentCount > 0) {
                    MetaCount(Icons.Rounded.AttachFile, task.attachmentCount)
                    Spacer(Modifier.width(Space.xs))
                }
                Box(
                    Modifier
                        .clip(Radii.chip)
                        .background(t.hairline)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        "${task.storyPoints} pt",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = t.textSecondary,
                    )
                }
                if (expandable) {
                    Spacer(Modifier.width(Space.xxs))
                    Icon(
                        Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = t.textTertiary,
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(chevronRotation)
                            .clip(CircleShape)
                            .springPress(pressedScale = 0.85f) { expanded = !expanded }
                            .padding(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaCount(icon: ImageVector, count: Int) {
    val t = LocalTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = t.textTertiary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(2.dp))
        Text("$count", style = MaterialTheme.typography.labelSmall, color = t.textTertiary)
    }
}

/** Team member card: avatar, role, presence, load and current focus. */
@Composable
fun ProfileCard(
    user: User,
    modifier: Modifier = Modifier,
    currentTask: String? = null,
    openTasks: Int = 0,
    onClick: (() -> Unit)? = null,
) {
    val t = LocalTokens.current

    ForgeCard(modifier, contentPadding = Space.md, onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ForgeAvatar(user, size = 50.dp, showPresence = true)
                Spacer(Modifier.width(Space.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        user.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = t.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(presenceColor(user.presence)),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${user.presence.label} · ${user.timezone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = t.textTertiary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.sm))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MiniStat("Open", "$openTasks")
                MiniStat("Closed", "${user.tasksClosed}")
                MiniStat("Commits", "${user.commits}")
                MiniStat("Reviews", "${user.reviews}")
            }

            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Throughput",
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                )
                Spacer(Modifier.width(Space.xs))
                ForgeProgressBar(
                    progress = user.throughput,
                    modifier = Modifier.weight(1f),
                    height = 5.dp,
                )
                Spacer(Modifier.width(Space.xs))
                Text(
                    "${(user.throughput * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = t.textPrimary,
                )
            }

            if (currentTask != null) {
                Spacer(Modifier.height(Space.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(t.hairline)
                        .padding(Space.xs),
                ) {
                    Column {
                        Eyebrow("Working on")
                        Spacer(Modifier.height(2.dp))
                        Text(
                            currentTask,
                            style = MaterialTheme.typography.bodySmall,
                            color = t.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = t.textPrimary,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = t.textTertiary)
    }
}

/** Section header with an optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = t.textPrimary,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textTertiary,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = t.accent,
                modifier = Modifier
                    .clip(Radii.pill)
                    .springPress(pressedScale = 0.92f, onClick = onAction)
                    .padding(horizontal = Space.xs, vertical = 6.dp),
            )
        }
    }
}
