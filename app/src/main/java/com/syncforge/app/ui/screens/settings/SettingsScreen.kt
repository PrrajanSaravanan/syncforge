package com.syncforge.app.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.MotionPhotosOff
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Workspaces
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
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeCard
import com.syncforge.app.ui.components.ForgeTopBar
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.AccentPalette
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.AppViewModel

private val languages = listOf(
    "English (US)", "English (UK)", "Deutsch", "Français", "日本語", "हिन्दी",
)

@Composable
fun SettingsScreen(
    app: AppViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val t = LocalTokens.current
    val settings by app.settings.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = BottomBarInset),
        verticalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        item {
            ForgeTopBar(
                title = "Settings",
                eyebrow = "Preferences",
                subtitle = "Applied instantly · stored in memory only",
                navIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavClick = onBack,
            )
        }

        // ── Appearance ───────────────────────────────────────────────────────
        item {
            SettingsGroup("Appearance") {
                ToggleRow(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark mode",
                    subtitle = "Dark is the default for this workspace",
                    checked = settings.darkMode,
                    onCheckedChange = app::setDarkMode,
                )
                RowDivider()
                Column(Modifier.padding(Space.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingIcon(Icons.Rounded.Palette, t.accent)
                        Spacer(Modifier.width(Space.sm))
                        Column {
                            Text(
                                "Accent colour",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = t.textPrimary,
                            )
                            Text(
                                settings.accent.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = t.textTertiary,
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.sm))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    ) {
                        AccentPalette.entries.forEach { palette ->
                            AccentSwatch(
                                palette = palette,
                                selected = settings.accent == palette,
                                onClick = { app.setAccent(palette) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                RowDivider()
                ToggleRow(
                    icon = Icons.Rounded.Tune,
                    title = "Compact density",
                    subtitle = "Tighter spacing in lists and boards",
                    checked = settings.compactDensity,
                    onCheckedChange = app::setCompact,
                )
                RowDivider()
                ToggleRow(
                    icon = Icons.Rounded.MotionPhotosOff,
                    title = "Reduce motion",
                    subtitle = "Minimise animation and parallax",
                    checked = settings.reduceMotion,
                    onCheckedChange = app::setReduceMotion,
                )
            }
        }

        // ── Notifications ────────────────────────────────────────────────────
        item {
            SettingsGroup("Notifications") {
                ToggleRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Push notifications",
                    subtitle = "Deploys, mentions and incidents",
                    checked = settings.pushNotifications,
                    onCheckedChange = app::setPush,
                )
                RowDivider()
                ToggleRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Mentions only",
                    subtitle = "Silence everything except direct mentions",
                    checked = settings.mentionsOnly,
                    onCheckedChange = app::setMentionsOnly,
                    enabled = settings.pushNotifications,
                )
                RowDivider()
                ToggleRow(
                    icon = Icons.Rounded.RecordVoiceOver,
                    title = "Sound effects",
                    subtitle = "Message and huddle tones",
                    checked = settings.soundEffects,
                    onCheckedChange = app::setSound,
                )
            }
        }

        // ── Workspace ────────────────────────────────────────────────────────
        item {
            SettingsGroup("Workspace") {
                ValueRow(
                    icon = Icons.Rounded.Workspaces,
                    title = "Workspace",
                    value = settings.workspace,
                    onClick = { app.showToast("Workspace switching is disabled in this prototype") },
                )
                RowDivider()
                Column(Modifier.padding(Space.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingIcon(Icons.Rounded.Language, t.series[1])
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            "Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary,
                        )
                    }
                    Spacer(Modifier.height(Space.xs))
                    Column {
                        languages.chunked(2).forEach { pair ->
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = Space.xs),
                                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            ) {
                                pair.forEach { language ->
                                    LanguageChip(
                                        label = language,
                                        selected = settings.language == language,
                                        onClick = { app.setLanguage(language) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // ── Data & about ─────────────────────────────────────────────────────
        item {
            SettingsGroup("Data") {
                ValueRow(
                    icon = Icons.Rounded.Download,
                    title = "Export data",
                    value = "JSON",
                    onClick = app::exportData,
                )
                RowDivider()
                ValueRow(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    title = "Help & feedback",
                    value = "",
                    onClick = { app.showToast("Support is not available in this prototype") },
                )
                RowDivider()
                ValueRow(
                    icon = Icons.Rounded.Info,
                    title = "About SyncForge",
                    value = "v1.0.0",
                    onClick = {
                        app.showToast("SyncForge 1.0.0 · frontend prototype, no backend")
                    },
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = Space.gutter)) {
                ForgeCard(
                    Modifier.fillMaxWidth(),
                    contentPadding = Space.sm,
                    borderColor = t.danger.copy(alpha = 0.3f),
                    onClick = onSignOut,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingIcon(Icons.AutoMirrored.Rounded.Logout, t.danger)
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            "Sign out",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = t.danger,
                        )
                    }
                }
                Spacer(Modifier.height(Space.md))
                Text(
                    "SyncForge · Real-Time Engineering Collaboration Platform\n" +
                        "Frontend prototype — all data is generated locally and never leaves this device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(Modifier.padding(horizontal = Space.gutter)) {
        Eyebrow(title)
        Spacer(Modifier.height(Space.xs))
        ForgeCard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            Column { content() }
        }
    }
}

@Composable
private fun RowDivider() {
    val t = LocalTokens.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 62.dp)
            .height(1.dp)
            .background(t.hairline),
    )
}

@Composable
private fun SettingIcon(icon: ImageVector, tone: Color) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tone.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tone, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .springPress(enabled = enabled) { onCheckedChange(!checked) }
            .padding(Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIcon(icon, if (enabled) t.accent else t.textTertiary)
        Spacer(Modifier.width(Space.sm))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) t.textPrimary else t.textTertiary,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary,
            )
        }
        ForgeSwitch(checked = checked && enabled, enabled = enabled)
    }
}

/** Custom switch — the thumb slides on a spring and the track cross-fades. */
@Composable
private fun ForgeSwitch(checked: Boolean, enabled: Boolean = true) {
    val t = LocalTokens.current
    val trackColor by animateColorAsState(
        if (checked) t.accent else t.hairlineStrong,
        tween(240),
        label = "track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "thumb",
    )

    Box(
        Modifier
            .width(46.dp)
            .height(27.dp)
            .clip(Radii.pill)
            .background(trackColor)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(start = thumbOffset)
                .size(21.dp)
                .clip(CircleShape)
                .background(if (enabled) Color.White else t.textTertiary),
        )
    }
}

@Composable
private fun ValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .springPress(onClick = onClick)
            .padding(Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIcon(icon, t.series[2])
        Spacer(Modifier.width(Space.sm))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = t.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotEmpty()) {
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                color = t.textTertiary,
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    palette: AccentPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val ring by animateDpAsState(
        targetValue = if (selected) 2.5.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "swatchRing",
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(ring, t.textPrimary, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(palette.base, palette.partner)),
                )
                .springPress(pressedScale = 0.88f, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            palette.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) t.textPrimary else t.textTertiary,
            maxLines = 1,
        )
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val bg by animateColorAsState(
        if (selected) t.accentMuted else Color.Transparent,
        tween(200),
        label = "langBg",
    )
    Row(
        modifier
            .clip(Radii.pill)
            .background(bg)
            .border(
                1.dp,
                if (selected) t.accent.copy(alpha = 0.5f) else t.hairlineStrong,
                Radii.pill,
            )
            .springPress(pressedScale = 0.95f, onClick = onClick)
            .padding(horizontal = Space.sm, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) t.accent else t.textSecondary,
            maxLines = 1,
        )
    }
}
