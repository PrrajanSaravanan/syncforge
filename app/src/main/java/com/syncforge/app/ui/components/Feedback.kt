package com.syncforge.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.Toast

/**
 * Empty state: an animated "breathing" glyph, a headline, a sentence of guidance
 * and an optional CTA. Used anywhere a filtered list can come back with nothing.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "empty")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2_600), RepeatMode.Reverse),
        label = "pulse",
    )
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18_000), RepeatMode.Restart),
        label = "orbit",
    )

    Column(
        modifier
            .fillMaxWidth()
            .padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Slow-rotating dashed halo — the "Lottie-like" bit, drawn in Compose.
            Box(
                Modifier
                    .size(118.dp)
                    .rotate(orbit)
                    .scale(pulse)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                t.accent.copy(alpha = 0.02f),
                                t.accent.copy(alpha = 0.22f),
                                Color.Transparent,
                                t.accent.copy(alpha = 0.16f),
                                t.accent.copy(alpha = 0.02f),
                            ),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .size(78.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(t.surface2)
                    .border(1.dp, t.glassBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = t.accent, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(Space.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = t.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = t.textTertiary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Space.lg))
            ForgeSecondaryButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.width(210.dp),
            )
        }
    }
}

/** App-wide snackbar. Tone drives icon + accent stripe. */
@Composable
fun ForgeSnackbar(
    toast: Toast?,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        val tone = toast?.tone ?: Toast.Tone.Info
        val (icon, color) = when (tone) {
            Toast.Tone.Success -> Icons.Rounded.CheckCircle to t.success
            Toast.Tone.Error -> Icons.Rounded.ErrorOutline to t.danger
            Toast.Tone.Info -> Icons.Rounded.Info to t.info
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(Radii.card)
                .background(if (t.isDark) t.surface3 else t.surface1)
                .border(1.dp, color.copy(alpha = 0.35f), Radii.card)
                .padding(vertical = Space.sm, horizontal = Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(Space.sm))
            Text(
                toast?.text.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = t.textPrimary,
            )
        }
    }
}

/**
 * Pull-to-refresh indicator. Compose's official one is still experimental in this
 * BOM, so this is a small custom arc that scales in while a refresh is running.
 */
@Composable
fun RefreshIndicator(
    refreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val scale by animateFloatAsState(
        targetValue = if (refreshing) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "refreshScale",
    )
    val transition = rememberInfiniteTransition(label = "spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Restart),
        label = "angle",
    )

    if (scale > 0.01f) {
        Box(
            modifier
                .scale(scale)
                .size(38.dp)
                .clip(CircleShape)
                .background(t.surface3)
                .border(1.dp, t.glassBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(20.dp)
                    .rotate(angle)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.Transparent, t.accent, Color.Transparent),
                        ),
                        CircleShape,
                    ),
            )
            Box(Modifier.size(13.dp).clip(CircleShape).background(t.surface3))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeletons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: androidx.compose.ui.unit.Dp = 13.dp,
) {
    Box(
        modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmer(RoundedCornerShape(6.dp)),
    )
}

/** Card-shaped placeholder matching the metric cards on the dashboard. */
@Composable
fun SkeletonStatCard(modifier: Modifier = Modifier) {
    ForgeCard(modifier, contentPadding = Space.md) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Box(Modifier.size(34.dp).shimmer(RoundedCornerShape(11.dp)))
            Spacer(Modifier.height(Space.xs))
            SkeletonLine(widthFraction = 0.55f, height = 22.dp)
            SkeletonLine(widthFraction = 0.8f, height = 10.dp)
        }
    }
}

/** Placeholder matching a project/list card. */
@Composable
fun SkeletonListCard(modifier: Modifier = Modifier, lines: Int = 3) {
    ForgeCard(modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).shimmer(RoundedCornerShape(13.dp)))
                Spacer(Modifier.width(Space.sm))
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SkeletonLine(widthFraction = 0.6f, height = 14.dp)
                    SkeletonLine(widthFraction = 0.35f, height = 10.dp)
                }
            }
            repeat(lines - 1) {
                SkeletonLine(widthFraction = if (it % 2 == 0) 0.95f else 0.7f, height = 10.dp)
            }
            SkeletonLine(widthFraction = 1f, height = 8.dp)
        }
    }
}
