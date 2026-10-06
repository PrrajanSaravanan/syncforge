package com.syncforge.app.ui.screens.activity

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.automirrored.rounded.CallMerge
import androidx.compose.material.icons.rounded.Commit
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.ActivityKind
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeTopBar
import com.syncforge.app.ui.components.LabelChip
import com.syncforge.app.ui.components.PullToRefreshBox
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.CodeStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.ActivityViewModel

/** Icon vocabulary shared by the timeline, dashboard preview and notifications. */
fun activityIcon(kind: ActivityKind): ImageVector = when (kind) {
    ActivityKind.Commit -> Icons.Rounded.Commit
    ActivityKind.PullRequest -> Icons.AutoMirrored.Rounded.CallMerge
    ActivityKind.Deployment -> Icons.Rounded.RocketLaunch
    ActivityKind.TaskUpdate -> Icons.Rounded.TaskAlt
    ActivityKind.Comment -> Icons.Rounded.AddComment
    ActivityKind.MemberJoin -> Icons.Rounded.PersonAdd
    ActivityKind.Release -> Icons.Rounded.LocalOffer
    ActivityKind.Incident -> Icons.Rounded.BugReport
    ActivityKind.Review -> Icons.Rounded.RateReview
}

@Composable
fun ActivityScreen(
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
    viewModel: ActivityViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val events by viewModel.events.collectAsStateWithLifecycle()
    val kind by viewModel.kind.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ForgeTopBar(
            title = "Activity",
            eyebrow = "Workspace feed",
            subtitle = "${events.size} events across 15 projects",
            navIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavClick = onBack,
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.gutter),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            item {
                ForgeChip("All", selected = kind == null, onClick = { viewModel.setKind(null) })
            }
            items(ActivityKind.entries) { k ->
                ForgeChip(
                    label = k.label,
                    selected = kind == k,
                    icon = activityIcon(k),
                    onClick = { viewModel.setKind(if (kind == k) null else k) },
                )
            }
        }

        Spacer(Modifier.height(Space.sm))

        PullToRefreshBox(
            refreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            when {
                loading -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    items(6) { SkeletonListCard(lines = 2) }
                }

                events.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.HistoryToggleOff,
                    title = "No ${kind?.label?.lowercase() ?: ""} events",
                    message = "Nothing has happened in this category yet. Switch filters to see the rest of the feed.",
                    actionLabel = "Show everything",
                    onAction = { viewModel.setKind(null) },
                    modifier = Modifier.fillMaxSize(),
                )

                else -> {
                    // Group under day separators, preserving feed order.
                    val grouped = events.groupBy { it.dayGroup }
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
                        ),
                    ) {
                        grouped.forEach { (day, dayEvents) ->
                            item(key = "sep-$day") { DaySeparator(day, dayEvents.size) }
                            items(dayEvents, key = { it.id }) { event ->
                                ActivityRow(
                                    event = event,
                                    actor = viewModel.user(event.actorId),
                                    projectName = viewModel.project(event.projectId)?.name,
                                    showConnector = event != dayEvents.last(),
                                    onClick = event.projectId?.let { id -> { onOpenProject(id) } },
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
fun DaySeparator(label: String, count: Int, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = t.textPrimary,
        )
        Spacer(Modifier.width(Space.xs))
        LabelChip("$count")
        Spacer(Modifier.width(Space.xs))
        Box(Modifier.weight(1f).height(1.dp).background(t.hairline))
    }
}

/**
 * One event on the timeline: a rail with the kind icon, a connector down to the
 * next row, and the actor/meta line underneath.
 */
@Composable
fun ActivityRow(
    event: ActivityEvent,
    actor: User,
    projectName: String?,
    modifier: Modifier = Modifier,
    showConnector: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    val tone = when {
        !event.succeeded -> t.danger
        event.kind == ActivityKind.Deployment || event.kind == ActivityKind.Release -> t.success
        event.kind == ActivityKind.Review -> t.series[2]
        else -> t.info
    }

    Row(modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(34.dp),
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tone.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(activityIcon(event.kind), null, tint = tone, modifier = Modifier.size(15.dp))
            }
            if (showConnector) {
                Box(
                    Modifier
                        .width(1.5.dp)
                        .height(64.dp)
                        .background(t.hairlineStrong),
                )
            }
        }
        Spacer(Modifier.width(Space.xs))
        ForgeCard(
            Modifier
                .weight(1f)
                .padding(bottom = Space.xs),
            contentPadding = Space.sm,
            onClick = onClick,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ForgeAvatar(actor, size = 22.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        actor.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = t.textPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        event.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = t.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    event.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (event.meta.isNotEmpty() || projectName != null) {
                    Spacer(Modifier.height(Space.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        if (event.meta.isNotEmpty()) {
                            Box(
                                Modifier
                                    .clip(Radii.chip)
                                    .background(tone.copy(alpha = 0.13f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(event.meta, style = CodeStyle, color = tone)
                            }
                        }
                        if (projectName != null) {
                            LabelChip(projectName)
                        }
                    }
                }
            }
        }
    }
}
