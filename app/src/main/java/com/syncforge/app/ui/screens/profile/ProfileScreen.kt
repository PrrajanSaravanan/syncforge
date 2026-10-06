package com.syncforge.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.Achievement
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.ContributionGrid
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.LabelChip
import com.syncforge.app.ui.components.MiniStat
import com.syncforge.app.ui.components.ProgressRing
import com.syncforge.app.ui.components.SectionHeader
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.components.StatusTag
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignOut: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user = state.user

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = BottomBarInset),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        item {
            if (user == null) {
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(Space.gutter),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    repeat(3) { SkeletonListCard(lines = 4) }
                }
            } else {
                ProfileHero(
                    user = user,
                    onOpenSettings = onOpenSettings,
                    onOpenNotifications = onOpenNotifications,
                )
            }
        }

        if (user != null) {
            item {
                Row(
                    Modifier.padding(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    ForgeCard(Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ProgressRing(
                                progress = user.throughput,
                                size = 82.dp,
                                label = "throughput",
                            )
                        }
                    }
                    ForgeCard(Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                            Eyebrow("Focus area")
                            Text(
                                user.focusArea,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = t.textPrimary,
                            )
                            Spacer(Modifier.height(Space.xxs))
                            Eyebrow("Member since")
                            Text(
                                user.joinedOn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = t.accent,
                            )
                        }
                    }
                }
            }

            // ── Contribution graph ───────────────────────────────────────────
            item {
                ForgeCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.gutter),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Contributions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = t.textPrimary,
                                )
                                Text(
                                    "${state.contributionTotal} contributions in the last year",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = t.textTertiary,
                                )
                            }
                            StatusTag("90 day streak", t.success)
                        }
                        Spacer(Modifier.height(Space.md))
                        ContributionGrid(
                            days = state.contributions,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(Space.xs))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Less",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                            Spacer(Modifier.width(5.dp))
                            listOf(0.08f, 0.28f, 0.52f, 0.76f, 1f).forEach { alpha ->
                                Box(
                                    Modifier
                                        .padding(horizontal = 1.5.dp)
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(t.accent.copy(alpha = alpha)),
                                )
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "More",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                        }
                    }
                }
            }

            // ── Achievements ─────────────────────────────────────────────────
            item {
                SectionHeader(
                    "Achievements",
                    subtitle = "${state.achievements.count { it.unlocked }} of ${state.achievements.size} unlocked",
                    modifier = Modifier.padding(horizontal = Space.gutter),
                )
            }
            item {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    state.achievements.forEach { achievement ->
                        AchievementCard(achievement) {
                            onMessage(
                                if (achievement.unlocked) {
                                    "${achievement.title} — ${achievement.description}"
                                } else {
                                    "Locked: ${achievement.description}"
                                },
                            )
                        }
                    }
                }
            }

            // ── Skills ───────────────────────────────────────────────────────
            item {
                ForgeCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.gutter),
                ) {
                    Column {
                        Eyebrow("Skills")
                        Spacer(Modifier.height(Space.xs))
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            user.skills.forEach { LabelChip(it) }
                        }
                        Spacer(Modifier.height(Space.md))
                        Eyebrow("Projects")
                        Spacer(Modifier.height(Space.xs))
                        state.projects.forEach { project ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(t.series[project.accentIndex % t.series.size]),
                                )
                                Spacer(Modifier.width(Space.xs))
                                Text(
                                    project.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = t.textSecondary,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    "${(project.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = t.textTertiary,
                                )
                            }
                        }
                    }
                }
            }

            // ── Shortcuts ────────────────────────────────────────────────────
            item {
                Column(
                    Modifier.padding(horizontal = Space.gutter),
                    verticalArrangement = Arrangement.spacedBy(Space.xs),
                ) {
                    ShortcutRow(Icons.Rounded.Settings, "Settings", "Theme, accent, notifications", onOpenSettings)
                    ShortcutRow(Icons.Rounded.HistoryToggleOff, "My activity", "Commits, reviews and deploys", onOpenActivity)
                    ShortcutRow(Icons.Rounded.Groups, "Team directory", "10 people across 5 disciplines", onOpenTeam)
                    ShortcutRow(Icons.Rounded.NotificationsNone, "Notifications", "Inbox and filters", onOpenNotifications)
                    ShortcutRow(
                        Icons.AutoMirrored.Rounded.Logout,
                        "Sign out",
                        "Return to the login screen",
                        onSignOut,
                        destructive = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHero(
    user: com.syncforge.app.data.model.User,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val t = LocalTokens.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        t.accent.copy(alpha = 0.30f),
                        t.accent.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            ForgeIconButton(
                Icons.Rounded.NotificationsNone,
                onOpenNotifications,
                contentDescription = "Notifications",
            )
            Spacer(Modifier.width(Space.xs))
            ForgeIconButton(Icons.Rounded.Settings, onOpenSettings, contentDescription = "Settings")
        }

        Spacer(Modifier.height(Space.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ForgeAvatar(user, size = 82.dp, showPresence = true, ringColor = t.accent)
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.textPrimary,
                )
                Text(
                    user.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = t.accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    user.handle,
                    style = MaterialTheme.typography.labelMedium,
                    color = t.textTertiary,
                )
            }
        }

        Spacer(Modifier.height(Space.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            MetaLine(Icons.Rounded.Place, user.location)
            MetaLine(Icons.Rounded.Schedule, user.timezone)
        }

        Spacer(Modifier.height(Space.md))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(Radii.card)
                .background(t.surface2)
                .padding(vertical = Space.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MiniStat("Commits", "${user.commits}")
            MiniStat("Reviews", "${user.reviews}")
            MiniStat("Closed", "${user.tasksClosed}")
            MiniStat("Open", "${user.tasksOpen}")
        }
    }
}

@Composable
private fun MetaLine(icon: ImageVector, text: String) {
    val t = LocalTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = t.textTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = t.textTertiary)
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, onClick: () -> Unit) {
    val t = LocalTokens.current
    val tierColor = when (achievement.tier) {
        "Platinum" -> t.series[1]
        "Gold" -> t.warning
        "Silver" -> t.textSecondary
        else -> t.series[4]
    }

    Column(
        Modifier
            .width(122.dp)
            .clip(Radii.card)
            .background(t.surface2)
            .springPress(pressedScale = 0.96f, onClick = onClick)
            .padding(Space.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (achievement.unlocked) {
                        Brush.linearGradient(listOf(tierColor, tierColor.copy(alpha = 0.55f)))
                    } else {
                        Brush.linearGradient(listOf(t.hairline, t.hairline))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.EmojiEvents,
                null,
                tint = if (achievement.unlocked) Color.White else t.textTertiary,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.height(Space.xs))
        Text(
            achievement.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (achievement.unlocked) t.textPrimary else t.textTertiary,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            achievement.tier,
            style = MaterialTheme.typography.labelSmall,
            color = if (achievement.unlocked) tierColor else t.textTertiary,
        )
    }
}

@Composable
private fun ShortcutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val t = LocalTokens.current
    val tone = if (destructive) t.danger else t.accent

    ForgeCard(Modifier.fillMaxWidth(), contentPadding = Space.sm, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(tone.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tone, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(Space.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (destructive) t.danger else t.textPrimary,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                )
            }
        }
    }
}
