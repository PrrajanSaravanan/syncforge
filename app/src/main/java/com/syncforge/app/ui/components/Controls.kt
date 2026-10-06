package com.syncforge.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.theme.EyebrowStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

// ─────────────────────────────────────────────────────────────────────────────
// Buttons
// ─────────────────────────────────────────────────────────────────────────────

/** Primary CTA: gradient fill, spring press, optional inline loading spinner. */
@Composable
fun ForgePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    shape: Shape = Radii.pill,
) {
    val t = LocalTokens.current
    val active = enabled && !loading

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .background(if (active) t.accentGradient else SolidColor(t.hairlineStrong))
            .springPress(enabled = active, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = t.onAccent,
                strokeWidth = 2.2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (active) t.onAccent else t.textTertiary,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (active) t.onAccent else t.textTertiary,
                )
            }
        }
    }
}

/** Outlined secondary action. */
@Composable
fun ForgeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    shape: Shape = Radii.pill,
) {
    val t = LocalTokens.current
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .background(t.glassFill)
            .border(1.dp, t.glassBorder, shape)
            .springPress(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            if (icon != null) {
                Icon(icon, null, tint = t.textPrimary, modifier = Modifier.size(19.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = t.textPrimary,
            )
        }
    }
}

/** Compact icon button on a glass pill — used in top bars and card corners. */
@Composable
fun ForgeIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null,
    badge: Int = 0,
) {
    val t = LocalTokens.current
    Box(modifier) {
        Box(
            modifier = Modifier
                .size(44.dp)                       // meets the 44dp touch minimum
                .clip(CircleShape)
                .background(t.glassFill)
                .border(1.dp, t.glassBorder, CircleShape)
                .springPress(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tint ?: t.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        if (badge > 0) {
            CountBadge(
                count = badge,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 2.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chips
// ─────────────────────────────────────────────────────────────────────────────

/** Filter chip with animated selection fill and optional leading dot/icon. */
@Composable
fun ForgeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    count: Int? = null,
) {
    val t = LocalTokens.current
    val bg by animateColorAsState(
        if (selected) t.accent else t.glassFill,
        tween(220),
        label = "chipBg",
    )
    val fg by animateColorAsState(
        if (selected) t.onAccent else t.textSecondary,
        tween(220),
        label = "chipFg",
    )
    val border by animateColorAsState(
        if (selected) Color.Transparent else t.glassBorder,
        tween(220),
        label = "chipBorder",
    )

    Row(
        modifier = modifier
            .height(38.dp)
            .clip(Radii.pill)
            .background(bg)
            .border(1.dp, border, Radii.pill)
            .springPress(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        }
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(15.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            maxLines = 1,
        )
        if (count != null) {
            Box(
                Modifier
                    .clip(Radii.pill)
                    .background(if (selected) Color.White.copy(alpha = 0.24f) else t.hairlineStrong)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = fg,
                )
            }
        }
    }
}

/** Non-interactive status/priority tag. */
@Composable
fun StatusTag(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(Radii.chip)
            .background(if (filled) color else color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val fg = if (filled) Color.White else color
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            maxLines = 1,
        )
    }
}

/** Small label chip for tags like "backend", "perf". */
@Composable
fun LabelChip(label: String, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Box(
        modifier
            .clip(Radii.chip)
            .background(t.hairline)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = t.textSecondary,
            maxLines = 1,
        )
    }
}

/** Uppercase section eyebrow. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    val t = LocalTokens.current
    Text(text.uppercase(), style = EyebrowStyle, color = color ?: t.textTertiary, modifier = modifier)
}

/** Numeric badge used on nav items, icon buttons and channel rows. */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    if (count <= 0) return
    val t = LocalTokens.current
    val label = if (count > 99) "99+" else "$count"
    Box(
        modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(Radii.pill)
            .background(color ?: t.danger)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search
// ─────────────────────────────────────────────────────────────────────────────

/** Glass search field with an animated clear affordance. */
@Composable
fun ForgeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSubmit: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    val height by animateDpAsState(52.dp, label = "searchHeight")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(Radii.pill)
            .background(t.glassFill)
            .border(1.dp, t.glassBorder, Radii.pill)
            .padding(horizontal = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            tint = t.textTertiary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Space.xs))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = t.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(
                    MaterialTheme.typography.bodyLarge.copy(color = t.textPrimary),
                ),
                cursorBrush = SolidColor(t.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit?.invoke() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(
            visible = value.isNotEmpty(),
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(t.hairlineStrong)
                    .springPress(pressedScale = 0.9f) { onValueChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Clear search",
                    tint = t.textSecondary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Space.xs))
            trailing()
        }
    }
}

/** Horizontal gradient meter used for progress everywhere in the app. */
@Composable
fun ForgeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color? = null,
    brush: Brush? = null,
    height: androidx.compose.ui.unit.Dp = 8.dp,
) {
    val t = LocalTokens.current
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .height(height)
            .clip(Radii.pill)
            .background(trackColor ?: t.hairlineStrong),
    ) {
        Box(
            Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .clip(Radii.pill)
                .background(brush ?: t.accentGradient),
        )
    }
}

/** Dimmed helper text row. */
@Composable
fun MutedText(
    text: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val t = LocalTokens.current
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = t.textTertiary,
        modifier = modifier.alpha(alpha),
    )
}
