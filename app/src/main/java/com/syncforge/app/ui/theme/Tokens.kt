package com.syncforge.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale — dashboard density (8→40). */
@Immutable
object Space {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
    /** Horizontal page gutter, shared by every screen for vertical rhythm. */
    val gutter: Dp = 18.dp
}

/**
 * Tokens Material 3's ColorScheme has no slot for: layered glass fills, ambient
 * gradients, priority/status hues and chart series. Provided through a
 * CompositionLocal so screens never hardcode hex.
 */
@Immutable
data class SyncForgeTokens(
    val isDark: Boolean,
    // Layered surfaces
    val canvas: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    // Glass
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // Accent
    val accent: Color,
    val accentSoft: Color,
    val accentMuted: Color,
    val onAccent: Color,
    // Signals
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    // Gradients
    val brandGradient: Brush,
    val accentGradient: Brush,
    val auroraGradient: Brush,
    val heroGradient: Brush,
    val shimmerGradient: Brush,
    // Charts
    val series: List<Color>,
) {
    fun priorityColor(name: String): Color = when (name.lowercase()) {
        "critical", "urgent", "blocker" -> PriorityCritical
        "high" -> PriorityHigh
        "medium" -> PriorityMedium
        else -> PriorityLow
    }

    fun statusColor(name: String): Color = when (name.lowercase()) {
        "completed", "done", "shipped", "healthy", "passing", "resolved" -> success
        "in progress", "active", "building", "in review", "review" -> info
        "at risk", "pending", "blocked", "degraded", "warning" -> warning
        "overdue", "failed", "critical", "down" -> danger
        else -> textTertiary
    }
}

val LocalTokens = staticCompositionLocalOf<SyncForgeTokens> {
    error("SyncForgeTokens not provided — wrap content in SyncForgeTheme")
}
