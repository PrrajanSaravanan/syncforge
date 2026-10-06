package com.syncforge.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Space

/**
 * Space a scrolling screen must reserve so content clears the floating tab bar.
 * The bar is 66dp tall inside 8dp vertical padding and sits above the system
 * navigation inset, so the reserve has to track that inset rather than guess it.
 */
val BottomBarInset: Dp
    @Composable get() = 90.dp + WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

/**
 * Screen header. Title block on the left, actions on the right, with an aurora
 * wash bleeding down from the status bar so every screen shares one silhouette.
 */
@Composable
fun ForgeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    eyebrow: String? = null,
    navIcon: ImageVector? = null,
    onNavClick: (() -> Unit)? = null,
    large: Boolean = false,
    actions: RowScopeActions = {},
) {
    val t = LocalTokens.current
    Column(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(t.accent.copy(alpha = 0.10f), Color.Transparent),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navIcon != null && onNavClick != null) {
                ForgeIconButton(icon = navIcon, contentDescription = "Back", onClick = onNavClick)
                Spacer(Modifier.width(Space.sm))
            }
            Column(Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Eyebrow(eyebrow, color = t.accent)
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    title,
                    style = if (large) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = t.textPrimary,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = t.textTertiary,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) { actions() }
        }
    }
}

typealias RowScopeActions = @Composable () -> Unit

/**
 * Pull-to-refresh wrapper. Compose's material3 pull-refresh is still
 * experimental in this BOM, so this reads the nested-scroll overscroll itself:
 * drag past the top accumulates pull, releasing past the threshold fires once.
 */
@Composable
fun PullToRefreshBox(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val threshold = 120f
    val pull = remember { mutableFloatStateOf(0f) }

    val connection = remember(refreshing) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // Only accumulate when the list itself could not consume a downward drag.
                if (!refreshing && available.y > 0f) {
                    pull.value = (pull.value + available.y * 0.5f).coerceAtMost(threshold * 1.6f)
                }
                return Offset.Zero
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Absorb upward drags into the pull distance before the list scrolls back.
                if (!refreshing && pull.value > 0f && available.y < 0f) {
                    val consumed = minOf(pull.value, -available.y)
                    pull.value -= consumed
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!refreshing && pull.value >= threshold) onRefresh()
                pull.value = 0f
                return Velocity.Zero
            }
        }
    }

    val indicatorOffset by animateFloatAsState(
        targetValue = if (refreshing) threshold * 0.55f else pull.value,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pullOffset",
    )

    Box(modifier.nestedScroll(connection)) {
        content()
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .offsetY(indicatorOffset)
                .alpha((indicatorOffset / threshold).coerceIn(0f, 1f)),
        ) {
            RefreshIndicator(refreshing = refreshing || pull.value > 4f)
        }
    }
}

private fun Modifier.offsetY(y: Float): Modifier =
    this.offset { IntOffset(0, y.toInt()) }

/** Absorbs taps so a scrim/overlay below stays non-interactive. */
fun Modifier.consumeTaps(): Modifier = this.pointerInput(Unit) {
    awaitEachGesture { awaitFirstDown(requireUnconsumed = false) }
}

/** Standard vertical rhythm for stacked page sections. */
@Composable
fun SectionSpacer(height: Dp = Space.xl) {
    Spacer(Modifier.height(height))
}

/** Full-screen skeleton used while a screen's first load resolves. */
@Composable
fun ScreenSkeleton(modifier: Modifier = Modifier, cards: Int = 4) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = Space.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        SkeletonLine(widthFraction = 0.5f, height = 26.dp)
        SkeletonLine(widthFraction = 0.72f, height = 12.dp)
        Spacer(Modifier.height(Space.sm))
        repeat(cards) { SkeletonListCard() }
    }
}
