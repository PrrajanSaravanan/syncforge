package com.syncforge.app.ui.screens.notifications

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.AppNotification
import com.syncforge.app.data.model.NotificationKind
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeTopBar
import com.syncforge.app.ui.components.PullToRefreshBox
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.NotificationsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

private fun kindIcon(kind: NotificationKind): ImageVector = when (kind) {
    NotificationKind.Mention -> Icons.Rounded.AlternateEmail
    NotificationKind.Assignment -> Icons.Rounded.AssignmentInd
    NotificationKind.Deployment -> Icons.Rounded.RocketLaunch
    NotificationKind.Review -> Icons.Rounded.RateReview
    NotificationKind.Incident -> Icons.Rounded.BugReport
    NotificationKind.System -> Icons.Rounded.Settings
}

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
    onMessage: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val items by viewModel.notifications.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val unreadOnly by viewModel.unreadOnly.collectAsStateWithLifecycle()
    val unread by viewModel.unreadCount.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ForgeTopBar(
            title = "Notifications",
            eyebrow = "Inbox",
            subtitle = if (unread > 0) "$unread unread" else "You're all caught up",
            navIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavClick = onBack,
        ) {
            ForgeIconButton(
                icon = Icons.Rounded.DoneAll,
                contentDescription = "Mark all as read",
                tint = if (unread > 0) t.accent else null,
                onClick = {
                    viewModel.markAllRead()
                    onMessage("All notifications marked as read")
                },
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.gutter),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            item {
                ForgeChip(
                    label = "Unread",
                    selected = unreadOnly,
                    onClick = viewModel::toggleUnreadOnly,
                    count = unread,
                )
            }
            item {
                ForgeChip("All", selected = filter == null, onClick = { viewModel.setFilter(null) })
            }
            items(NotificationKind.entries) { kind ->
                ForgeChip(
                    label = kind.label,
                    selected = filter == kind,
                    icon = kindIcon(kind),
                    onClick = { viewModel.setFilter(if (filter == kind) null else kind) },
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

                items.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.NotificationsNone,
                    title = if (unreadOnly) "Inbox zero" else "Nothing here",
                    message = if (unreadOnly) {
                        "You've read every notification. Nice work — go ship something."
                    } else {
                        "No notifications match this filter yet."
                    },
                    actionLabel = "Show all",
                    onAction = {
                        viewModel.setFilter(null)
                        if (unreadOnly) viewModel.toggleUnreadOnly()
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Space.xs),
                ) {
                    items(items, key = { it.id }) { item ->
                        SwipeableNotification(
                            notification = item,
                            onDismiss = {
                                viewModel.dismiss(item.id)
                                onMessage("Notification dismissed")
                            },
                            onClick = {
                                viewModel.markRead(item.id)
                                item.projectId?.let(onOpenProject)
                                    ?: onMessage(item.actionLabel ?: "Opened")
                            },
                            avatar = item.actorId?.let { viewModel.user(it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Swipe left to dismiss. Past a third of the width the row commits; anything
 * shorter springs back, so an accidental brush never deletes a notification.
 */
@Composable
private fun SwipeableNotification(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    avatar: com.syncforge.app.data.model.User?,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val offset = remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = offset.value,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "swipe",
    )

    Box(modifier) {
        // Delete affordance revealed behind the card as it slides away.
        Box(
            Modifier
                .matchParentSize()
                .clip(Radii.card)
                .background(t.danger.copy(alpha = 0.18f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = null,
                tint = t.danger,
                modifier = Modifier
                    .padding(end = Space.lg)
                    .size(22.dp),
            )
        }

        Box(
            Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(notification.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(offset.value) > size.width / 3f) {
                                offset.value = -size.width.toFloat()
                                onDismiss()
                            } else {
                                offset.value = 0f
                            }
                        },
                        onDragCancel = { offset.value = 0f },
                    ) { _, dragAmount ->
                        // Left-only: positive drags are clamped so the row can't fly right.
                        offset.value = (offset.value + dragAmount).coerceAtMost(0f)
                    }
                },
        ) {
            NotificationCard(notification, avatar, onClick)
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    avatar: com.syncforge.app.data.model.User?,
    onClick: () -> Unit,
) {
    val t = LocalTokens.current
    val tone = when (notification.kind) {
        NotificationKind.Incident -> t.danger
        NotificationKind.Deployment -> t.success
        NotificationKind.Mention -> t.series[2]
        NotificationKind.Review -> t.series[1]
        NotificationKind.Assignment -> t.series[0]
        NotificationKind.System -> t.textTertiary
    }

    ForgeCard(
        Modifier.fillMaxWidth(),
        contentPadding = Space.sm,
        borderColor = if (!notification.read) tone.copy(alpha = 0.32f) else null,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box {
                if (avatar != null) {
                    ForgeAvatar(avatar, size = 38.dp)
                } else {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(tone.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            kindIcon(notification.kind),
                            null,
                            tint = tone,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                // Kind badge overlaps the avatar so the source is legible at a glance.
                if (avatar != null) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(t.surface2)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(tone),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            kindIcon(notification.kind),
                            null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(Space.sm))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                        color = t.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (!notification.read) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(tone))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = t.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Space.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                    if (notification.actionLabel != null) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            notification.actionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = tone,
                            modifier = Modifier
                                .clip(Radii.pill)
                                .background(tone.copy(alpha = 0.12f))
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
