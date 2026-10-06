package com.syncforge.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

/**
 * Frosted panel. Real blur is API 31+ and expensive on lists, so the glass look
 * is built from a translucent fill, a top-lit hairline border and a diagonal
 * highlight sweep — cheap enough for a scrolling feed and identical on API 24.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.card,
    contentPadding: Dp = Space.md,
    strong: Boolean = false,
    glow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = LocalTokens.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .scale(if (pressed) 0.985f else 1f)
            .then(
                if (glow != null) {
                    Modifier.dropGlow(color = glow, shape = shape)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(if (strong) t.glassFillStrong else t.glassFill)
            .drawWithCache {
                // Diagonal sheen from the top-left, the way frosted panels catch light.
                val sheen = Brush.linearGradient(
                    colors = listOf(t.glassHighlight.copy(alpha = 0.16f), Color.Transparent),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.85f, size.height * 0.9f),
                )
                onDrawBehind { drawRect(sheen) }
            }
            .border(1.dp, t.glassBorder, shape)
            .then(if (onClick != null) Modifier.pressable(interaction, onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Opaque elevated card — the default container for dense content. */
@Composable
fun ForgeCard(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.card,
    contentPadding: Dp = Space.md,
    tone: Int = 2,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = LocalTokens.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val fill = when (tone) {
        1 -> t.surface1
        3 -> t.surface3
        else -> t.surface2
    }

    Box(
        modifier = modifier
            .scale(if (pressed) 0.985f else 1f)
            .clip(shape)
            .background(fill)
            .border(1.dp, borderColor ?: t.hairline, shape)
            .then(if (onClick != null) Modifier.pressable(interaction, onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Gradient hero card used for the single most important stat on a screen. */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    brush: Brush? = null,
    shape: Shape = Radii.cardLarge,
    contentPadding: Dp = Space.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = LocalTokens.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .scale(if (pressed) 0.985f else 1f)
            .clip(shape)
            .background(brush ?: t.accentGradient)
            .drawWithCache {
                // Soft radial bloom in the top-right corner keeps flat gradients from looking printed.
                val bloom = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.1f),
                    radius = size.maxDimension * 0.7f,
                )
                onDrawBehind { drawRect(bloom) }
            }
            .then(if (onClick != null) Modifier.pressable(interaction, onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Ambient background: two slowly drifting colored blobs over the canvas.
 * This is the app's signature backdrop — used on splash, auth and page headers.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val t = LocalTokens.current
    val scheme = MaterialTheme.colorScheme

    val drift = if (animated) {
        val transition = rememberInfiniteTransition(label = "aurora")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(14_000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "drift",
        ).value
    } else {
        0.5f
    }

    Box(
        modifier = modifier
            .background(t.canvas)
            .drawWithCache {
                val w = size.width
                val h = size.height

                val primaryCenter = Offset(w * (0.18f + drift * 0.22f), h * (0.12f + drift * 0.10f))
                val secondaryCenter = Offset(w * (0.88f - drift * 0.20f), h * (0.42f + drift * 0.16f))

                val blobA = Brush.radialGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.34f * intensity),
                        scheme.primary.copy(alpha = 0.08f * intensity),
                        Color.Transparent,
                    ),
                    center = primaryCenter,
                    radius = w * 0.95f,
                )
                val blobB = Brush.radialGradient(
                    colors = listOf(
                        scheme.secondary.copy(alpha = 0.24f * intensity),
                        Color.Transparent,
                    ),
                    center = secondaryCenter,
                    radius = w * 0.8f,
                )
                // Vignette pulls focus to the middle and hides blob edges.
                val vignette = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, t.canvas.copy(alpha = 0.86f)),
                    startY = h * 0.45f,
                    endY = h,
                )

                onDrawBehind {
                    drawRect(blobA)
                    drawRect(blobB)
                    drawRect(vignette)
                }
            },
        content = content,
    )
}

/** Thin gradient rule used to separate major sections. */
@Composable
fun GradientDivider(modifier: Modifier = Modifier, alpha: Float = 0.5f) {
    val t = LocalTokens.current
    Box(
        modifier
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        t.hairlineStrong.copy(alpha = alpha),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

/** Soft colored glow behind a shape — our stand-in for real shadow tinting. */
private fun Modifier.dropGlow(color: Color, shape: Shape, spread: Dp = 18.dp): Modifier =
    this.drawWithCache {
        val radius = when (shape) {
            is RoundedCornerShape -> spread.toPx()
            else -> spread.toPx()
        }
        val glow = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.30f), Color.Transparent),
            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.62f),
            radius = size.maxDimension * 0.75f,
        )
        onDrawBehind {
            drawRect(
                brush = glow,
                topLeft = Offset(-radius, -radius / 2f),
                size = Size(size.width + radius * 2, size.height + radius * 1.5f),
            )
        }
    }

/** Shared shimmer overlay used by every skeleton placeholder. */
@Composable
fun Modifier.shimmer(shape: Shape = RoundedCornerShape(10.dp)): Modifier {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1_400), repeatMode = RepeatMode.Restart),
        label = "sweep",
    )

    return this
        .clip(shape)
        .background(if (t.isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
        .drawWithCache {
            val width = size.width
            // Sweep travels from off-screen left to off-screen right.
            val start = -width + progress * (width * 2.4f)
            val brush = Brush.linearGradient(
                colors = if (t.isDark) {
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.10f), Color.Transparent)
                } else {
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.75f), Color.Transparent)
                },
                start = Offset(start, 0f),
                end = Offset(start + width * 0.65f, size.height),
            )
            onDrawBehind { drawRect(brush) }
        }
}

/** Full-bleed skeleton block. */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
) {
    Box(modifier.shimmer(shape).fillMaxSize())
}
