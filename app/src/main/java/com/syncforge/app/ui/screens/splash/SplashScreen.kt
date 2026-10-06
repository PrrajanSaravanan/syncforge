package com.syncforge.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.components.AuroraBackground
import com.syncforge.app.ui.theme.EyebrowStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import kotlinx.coroutines.delay

/**
 * Splash: the forge mark assembles from two chevrons, a sweep ring orbits it and
 * a determinate bar tracks the (simulated) workspace warm-up before handing off.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val t = LocalTokens.current

    var started by remember { mutableStateOf(false) }
    var stage by remember { mutableStateOf(0) }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "logoScale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha",
    )
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1_900, easing = LinearEasing),
        label = "progress",
    )

    val infinite = rememberInfiniteTransition(label = "splash")
    val ringAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2_600, easing = LinearEasing)),
        label = "ring",
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1_800), RepeatMode.Reverse),
        label = "glow",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(500); stage = 1        // wordmark
        delay(450); stage = 2        // tagline + progress
        delay(1_700)
        onFinished()
    }

    AuroraBackground(Modifier.fillMaxSize(), intensity = 1.2f) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(Space.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                // Outer glow bloom.
                Box(
                    Modifier
                        .size(210.dp)
                        .scale(glowPulse)
                        .alpha(logoAlpha * 0.55f)
                        .background(t.auroraGradient, CircleShape),
                )
                // Orbiting sweep ring.
                Box(
                    Modifier
                        .size(132.dp)
                        .alpha(logoAlpha)
                        .rotate(ringAngle),
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    t.accent.copy(alpha = 0.15f),
                                    t.accent,
                                    Color.Transparent,
                                ),
                            ),
                            startAngle = 0f,
                            sweepAngle = 300f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                // The mark itself.
                Box(
                    Modifier
                        .size(104.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .clip(Radii.cardLarge)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    t.accent,
                                    t.accent.copy(alpha = 0.72f),
                                    MaterialTheme.colorScheme.secondary,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ForgeMark(Modifier.size(52.dp))
                }
            }

            Spacer(Modifier.height(Space.xxl))

            AnimatedVisibility(
                visible = stage >= 1,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SyncForge",
                        style = MaterialTheme.typography.displayMedium,
                        color = t.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(Space.xs))
                    Box(
                        Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .clip(Radii.pill)
                            .background(t.accentGradient),
                    )
                }
            }

            Spacer(Modifier.height(Space.md))

            AnimatedVisibility(visible = stage >= 2, enter = fadeIn(tween(600))) {
                Text(
                    "Real-Time Engineering\nCollaboration Platform",
                    style = MaterialTheme.typography.bodyLarge,
                    color = t.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.5,
                )
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(visible = stage >= 2, enter = fadeIn(tween(400))) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.55f)
                            .height(3.dp)
                            .clip(Radii.pill)
                            .background(t.hairlineStrong),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .clip(Radii.pill)
                                .background(t.accentGradient),
                        )
                    }
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        when {
                            progress < 0.4f -> "Connecting to workspace"
                            progress < 0.75f -> "Syncing 15 projects"
                            else -> "Almost ready"
                        },
                        style = EyebrowStyle,
                        color = t.textTertiary,
                    )
                }
            }

            Spacer(Modifier.height(Space.xxl))
            Text(
                "Northwind Labs",
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary.copy(alpha = 0.7f),
            )
        }
    }
}

/** The SyncForge glyph: two interlocking chevrons around a shared core. */
@Composable
fun ForgeMark(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.13f

        // Left chevron
        val left = Path().apply {
            moveTo(w * 0.42f, h * 0.06f)
            lineTo(w * 0.08f, h * 0.5f)
            lineTo(w * 0.42f, h * 0.94f)
        }
        // Right chevron
        val right = Path().apply {
            moveTo(w * 0.58f, h * 0.06f)
            lineTo(w * 0.92f, h * 0.5f)
            lineTo(w * 0.58f, h * 0.94f)
        }

        drawPath(left, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawPath(right, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

        // Core diamond ties the two halves together.
        val core = Path().apply {
            moveTo(w * 0.5f, h * 0.3f)
            lineTo(w * 0.66f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.7f)
            lineTo(w * 0.34f, h * 0.5f)
            close()
        }
        drawPath(core, color.copy(alpha = 0.95f))
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(w * 0.5f, h * 0.02f),
            end = Offset(w * 0.5f, h * 0.28f),
            strokeWidth = stroke * 0.5f,
            cap = StrokeCap.Round,
        )
    }
}
