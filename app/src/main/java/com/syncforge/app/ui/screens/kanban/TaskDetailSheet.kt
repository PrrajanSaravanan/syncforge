package com.syncforge.app.ui.screens.kanban

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.components.AvatarStack
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeProgressBar
import com.syncforge.app.ui.components.LabelChip
import com.syncforge.app.ui.components.MiniStat
import com.syncforge.app.ui.components.StatusTag
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.CodeStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

/**
 * Task detail sheet. Status is changed with the segmented control at the bottom —
 * the board is drag-free by design, so this is the move affordance.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    task: Task,
    assignees: List<User>,
    reporter: User,
    projectName: String,
    onDismiss: () -> Unit,
    onMove: (TaskStatus) -> Unit,
    onToggleChecklist: (String) -> Unit,
) {
    val t = LocalTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val priorityColor = t.priorityColor(task.priority.label)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = t.surface1,
        contentColor = t.textPrimary,
        shape = Radii.sheet,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = Space.sm, bottom = Space.xs)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(Radii.pill)
                    .background(t.hairlineStrong),
            )
        },
    ) {
        Column(
            Modifier
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.lg)
                .navigationBarsPadding(),
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .clip(Radii.pill)
                        .background(priorityColor),
                )
                Spacer(Modifier.width(Space.xs))
                Column(Modifier.weight(1f)) {
                    Text(task.ref, style = CodeStyle, color = t.textTertiary)
                    Text(
                        projectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                }
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = t.textSecondary,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(t.hairline)
                        .springPress(pressedScale = 0.88f, onClick = onDismiss)
                        .padding(8.dp),
                )
            }

            Spacer(Modifier.height(Space.sm))
            Text(
                task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = t.textPrimary,
            )

            Spacer(Modifier.height(Space.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusTag(task.priority.label, priorityColor)
                StatusTag(task.status.label, t.statusColor(task.status.label))
                if (task.overdue) StatusTag("Overdue", t.danger, filled = true)
            }

            Spacer(Modifier.height(Space.md))
            Text(
                task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = t.textSecondary,
            )

            if (task.branch.isNotEmpty()) {
                Spacer(Modifier.height(Space.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(t.hairline)
                        .padding(Space.sm),
                ) {
                    Column {
                        Eyebrow("Branch")
                        Spacer(Modifier.height(3.dp))
                        Text(task.branch, style = CodeStyle, color = t.accent)
                    }
                }
            }

            // ── Meta grid ────────────────────────────────────────────────────
            Spacer(Modifier.height(Space.md))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radii.card)
                    .background(t.surface2)
                    .padding(vertical = Space.sm),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MiniStat("Points", "${task.storyPoints}")
                MiniStat("Comments", "${task.commentCount}")
                MiniStat("Files", "${task.attachmentCount}")
                MiniStat("Due", task.dueLabel)
            }

            // ── People ───────────────────────────────────────────────────────
            Spacer(Modifier.height(Space.md))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("Assignees")
                    Spacer(Modifier.height(Space.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarStack(assignees, max = 3, size = 30.dp)
                        Spacer(Modifier.width(Space.xs))
                        Text(
                            assignees.joinToString { it.name.substringBefore(' ') },
                            style = MaterialTheme.typography.bodySmall,
                            color = t.textSecondary,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Eyebrow("Reporter")
                    Spacer(Modifier.height(Space.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            reporter.name.substringBefore(' '),
                            style = MaterialTheme.typography.bodySmall,
                            color = t.textSecondary,
                        )
                        Spacer(Modifier.width(6.dp))
                        ForgeAvatar(reporter, size = 26.dp)
                    }
                }
            }

            // ── Labels ───────────────────────────────────────────────────────
            if (task.labels.isNotEmpty()) {
                Spacer(Modifier.height(Space.md))
                Eyebrow("Labels")
                Spacer(Modifier.height(Space.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    task.labels.forEach { LabelChip(it) }
                }
            }

            // ── Checklist ────────────────────────────────────────────────────
            if (task.checklist.isNotEmpty()) {
                Spacer(Modifier.height(Space.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("Checklist")
                    Spacer(Modifier.width(Space.xs))
                    Text(
                        "${task.subtasksDone}/${task.subtasksTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = t.accent,
                    )
                }
                Spacer(Modifier.height(Space.xs))
                ForgeProgressBar(progress = task.subtaskProgress, height = 5.dp)
                Spacer(Modifier.height(Space.xs))
                task.checklist.forEach { item ->
                    ChecklistRow(
                        text = item.text,
                        done = item.done,
                        onToggle = { onToggleChecklist(item.id) },
                    )
                }
            }

            // ── Status control ───────────────────────────────────────────────
            Spacer(Modifier.height(Space.lg))
            Eyebrow("Move to")
            Spacer(Modifier.height(Space.xs))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radii.pill)
                    .background(t.surface2)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                TaskStatus.entries.forEach { status ->
                    val selected = status == task.status
                    val accent = columnAccentFor(status, t.series)
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(Radii.pill)
                            .background(if (selected) accent else androidx.compose.ui.graphics.Color.Transparent)
                            .springPress(pressedScale = 0.94f) {
                                if (!selected) onMove(status)
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            status.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) {
                                androidx.compose.ui.graphics.Color.White
                            } else {
                                t.textTertiary
                            },
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.md))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radii.card)
                    .background(t.hairline)
                    .padding(Space.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.ChatBubbleOutline,
                    null,
                    tint = t.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(Space.xs))
                Text(
                    "${task.commentCount} comments · created ${task.createdLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textTertiary,
                )
            }

            Spacer(Modifier.height(Space.xxl))
        }
    }
}

private fun columnAccentFor(
    status: TaskStatus,
    series: List<androidx.compose.ui.graphics.Color>,
): androidx.compose.ui.graphics.Color = when (status) {
    TaskStatus.Todo -> series[6]
    TaskStatus.InProgress -> series[0]
    TaskStatus.Review -> series[2]
    TaskStatus.Completed -> series[3]
}

@Composable
private fun ChecklistRow(text: String, done: Boolean, onToggle: () -> Unit) {
    val t = LocalTokens.current
    val tickScale by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .springPress(pressedScale = 0.98f, onClick = onToggle)
            .padding(vertical = 7.dp, horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (done) t.success else androidx.compose.ui.graphics.Color.Transparent)
                .border(
                    1.6.dp,
                    if (done) t.success else t.textTertiary,
                    RoundedCornerShape(7.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Check,
                null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(13.dp).scale(tickScale),
            )
        }
        Spacer(Modifier.width(Space.xs))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (done) t.textTertiary else t.textPrimary,
            textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
        )
    }
}
