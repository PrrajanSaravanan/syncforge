package com.syncforge.app.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Selectable accent identities exposed in Settings. */
enum class AccentPalette(val label: String, val base: Color, val soft: Color, val deep: Color) {
    Indigo("Indigo", Indigo500, Indigo400, Indigo600),
    Violet("Violet", Violet500, Violet400, Violet600),
    Cyan("Cyan", Cyan500, Cyan400, Cyan600),
    Emerald("Emerald", Emerald500, Emerald400, Emerald600),
    Amber("Amber", Amber500, Amber400, Amber600),
    Rose("Rose", Rose500, Rose400, Rose600);

    /** Secondary hue used for gradient sweeps — one step around the wheel. */
    val partner: Color
        get() = when (this) {
            Indigo -> Cyan400
            Violet -> Rose400
            Cyan -> Emerald400
            Emerald -> Cyan400
            Amber -> Rose400
            Rose -> Violet400
        }
}

private fun darkScheme(a: AccentPalette) = darkColorScheme(
    primary = a.base,
    onPrimary = Color.White,
    primaryContainer = a.deep,
    onPrimaryContainer = Color.White,
    inversePrimary = a.soft,
    secondary = a.partner,
    onSecondary = Ink900,
    secondaryContainer = Ink600,
    onSecondaryContainer = Ink050,
    tertiary = Violet400,
    onTertiary = Ink900,
    tertiaryContainer = Ink600,
    onTertiaryContainer = Ink050,
    background = Ink900,
    onBackground = Ink050,
    surface = Ink850,
    onSurface = Ink050,
    surfaceVariant = Ink700,
    onSurfaceVariant = Ink200,
    surfaceTint = a.base,
    surfaceContainerLowest = Ink900,
    surfaceContainerLow = Ink850,
    surfaceContainer = Ink800,
    surfaceContainerHigh = Ink700,
    surfaceContainerHighest = Ink600,
    inverseSurface = Ink050,
    inverseOnSurface = Ink900,
    error = SignalDanger,
    onError = Color.White,
    errorContainer = Color(0xFF4C1220),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Ink400,
    outlineVariant = Ink600,
    scrim = Color(0xCC03060D),
)

private fun lightScheme(a: AccentPalette) = lightColorScheme(
    primary = a.deep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E7FF),
    onPrimaryContainer = Color(0xFF1B1B4D),
    inversePrimary = a.soft,
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCF3F8),
    onSecondaryContainer = Color(0xFF06323C),
    tertiary = Violet600,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Color(0xFF0B1120),
    surface = Color.White,
    onSurface = Color(0xFF0B1120),
    surfaceVariant = Color(0xFFEEF1F8),
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = a.deep,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFF),
    surfaceContainer = Color(0xFFF1F4FB),
    surfaceContainerHigh = Color(0xFFE9EDF7),
    surfaceContainerHighest = Color(0xFFE1E7F3),
    inverseSurface = Ink800,
    inverseOnSurface = Ink050,
    error = Color(0xFFDC2626),
    onError = Color.White,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0x99101828),
)

private fun darkTokens(a: AccentPalette) = SyncForgeTokens(
    isDark = true,
    canvas = Ink900,
    surface1 = Ink850,
    surface2 = Ink800,
    surface3 = Ink700,
    hairline = Color(0x14FFFFFF),
    hairlineStrong = Color(0x24FFFFFF),
    glassFill = Color(0x0FFFFFFF),
    glassFillStrong = Color(0x1AFFFFFF),
    glassBorder = Color(0x1FFFFFFF),
    glassHighlight = Color(0x33FFFFFF),
    textPrimary = Ink050,
    textSecondary = Ink200,
    textTertiary = Ink300,
    accent = a.base,
    accentSoft = a.soft,
    accentMuted = a.base.copy(alpha = 0.16f),
    onAccent = Color.White,
    success = SignalSuccess,
    warning = SignalWarning,
    danger = SignalDanger,
    info = SignalInfo,
    brandGradient = Brush.linearGradient(listOf(Indigo500, Violet500, Cyan400)),
    accentGradient = Brush.linearGradient(listOf(a.soft, a.base, a.partner)),
    auroraGradient = Brush.radialGradient(
        listOf(a.base.copy(alpha = 0.40f), a.partner.copy(alpha = 0.14f), Color.Transparent),
    ),
    heroGradient = Brush.verticalGradient(
        listOf(a.base.copy(alpha = 0.22f), Ink850.copy(alpha = 0.0f)),
    ),
    shimmerGradient = Brush.linearGradient(
        listOf(Color(0x0AFFFFFF), Color(0x1FFFFFFF), Color(0x0AFFFFFF)),
    ),
    series = ChartSeries,
)

private fun lightTokens(a: AccentPalette) = SyncForgeTokens(
    isDark = false,
    canvas = Paper,
    surface1 = Color.White,
    surface2 = Color(0xFFF4F7FD),
    surface3 = Color(0xFFEAEFF8),
    hairline = Color(0x0F0B1120),
    hairlineStrong = Color(0x1A0B1120),
    glassFill = Color(0xB3FFFFFF),
    glassFillStrong = Color(0xE6FFFFFF),
    glassBorder = Color(0x1F0B1120),
    glassHighlight = Color(0x66FFFFFF),
    textPrimary = Color(0xFF0B1120),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF7A889C),
    accent = a.deep,
    accentSoft = a.base,
    accentMuted = a.base.copy(alpha = 0.12f),
    onAccent = Color.White,
    success = Color(0xFF15803D),
    warning = Color(0xFFB45309),
    danger = Color(0xFFDC2626),
    info = Color(0xFF1D4ED8),
    brandGradient = Brush.linearGradient(listOf(Indigo600, Violet600, Cyan600)),
    accentGradient = Brush.linearGradient(listOf(a.base, a.deep, a.partner)),
    auroraGradient = Brush.radialGradient(
        listOf(a.base.copy(alpha = 0.22f), a.partner.copy(alpha = 0.08f), Color.Transparent),
    ),
    heroGradient = Brush.verticalGradient(
        listOf(a.base.copy(alpha = 0.14f), Color.Transparent),
    ),
    shimmerGradient = Brush.linearGradient(
        listOf(Color(0x0D0B1120), Color(0x1A0B1120), Color(0x0D0B1120)),
    ),
    series = ChartSeries,
)

@Composable
fun SyncForgeTheme(
    darkTheme: Boolean = true,
    accent: AccentPalette = AccentPalette.Indigo,
    content: @Composable () -> Unit,
) {
    val target = if (darkTheme) darkScheme(accent) else lightScheme(accent)
    val tokens = if (darkTheme) darkTokens(accent) else lightTokens(accent)

    // Animate the big surfaces so theme + accent switches cross-fade instead of snapping.
    val spec = tween<Color>(durationMillis = 450)
    val bg by animateColorAsState(target.background, spec, label = "bg")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onSurface by animateColorAsState(target.onSurface, spec, label = "onSurface")

    val scheme = target.copy(
        background = bg,
        surface = surface,
        primary = primary,
        onSurface = onSurface,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Bar colours come from enableEdgeToEdge() in MainActivity; here we only
            // flip icon contrast so it tracks the live theme.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SyncForgeTypography,
            shapes = SyncForgeShapes,
            content = content,
        )
    }
}
