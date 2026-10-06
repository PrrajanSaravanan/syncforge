package com.syncforge.app.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

/**
 * Auth text field. Label stays visible above the input (never placeholder-only)
 * and the border animates to the accent on focus.
 */
@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    helper: String? = null,
    error: String? = null,
) {
    val t = LocalTokens.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    var revealed by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        when {
            error != null -> t.danger
            focused -> t.accent
            else -> t.glassBorder
        },
        tween(220),
        label = "fieldBorder",
    )

    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (focused) t.accent else t.textSecondary,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(t.glassFill)
                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon, null,
                    tint = if (focused) t.accent else t.textTertiary,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(Space.sm))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = t.textTertiary.copy(alpha = 0.75f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    interactionSource = interaction,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = t.textPrimary),
                    cursorBrush = SolidColor(t.accent),
                    visualTransformation = if (isPassword && !revealed) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isPassword) {
                Icon(
                    if (revealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (revealed) "Hide password" else "Show password",
                    tint = t.textTertiary,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .springPress(pressedScale = 0.86f) { revealed = !revealed }
                        .padding(7.dp),
                )
            }
        }
        // Error takes precedence and sits directly under the field it belongs to.
        if (error != null) {
            Spacer(Modifier.height(5.dp))
            Text(error, style = MaterialTheme.typography.labelSmall, color = t.danger)
        } else if (helper != null) {
            Spacer(Modifier.height(5.dp))
            Text(helper, style = MaterialTheme.typography.labelSmall, color = t.textTertiary)
        }
    }
}

/** Animated checkbox for "Remember me" and settings-style toggles. */
@Composable
fun ForgeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val boxColor by animateColorAsState(
        if (checked) t.accent else Color.Transparent,
        tween(200),
        label = "checkboxBg",
    )
    val borderColor by animateColorAsState(
        if (checked) t.accent else t.textTertiary,
        tween(200),
        label = "checkboxBorder",
    )
    val tickScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tick",
    )

    Row(
        modifier
            .clip(Radii.pill)
            .springPress(pressedScale = 0.96f) { onCheckedChange(!checked) }
            .padding(vertical = 5.dp, horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(21.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(boxColor)
                .border(1.8.dp, borderColor, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Check, null,
                tint = t.onAccent,
                modifier = Modifier.size(14.dp).scale(tickScale),
            )
        }
        Spacer(Modifier.width(Space.xs))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = t.textSecondary)
    }
}

/**
 * Social sign-in row. UI only — each button raises a snackbar explaining that
 * this build has no auth backend.
 */
@Composable
fun SocialRow(
    onProvider: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val providers = listOf(
        Triple("Google", "G", Color(0xFFEA4335)),
        Triple("GitHub", "GH", Color(0xFF8B949E)),
        Triple("Okta", "O", Color(0xFF007DC1)),
    )
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        providers.forEach { (name, glyph, color) ->
            SocialButton(
                name = name,
                glyph = glyph,
                color = color,
                onClick = { onProvider(name) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SocialButton(
    name: String,
    glyph: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    Row(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(t.glassFill)
            .border(1.dp, t.glassBorder, RoundedCornerShape(15.dp))
            .springPress(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                glyph,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color,
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = t.textSecondary,
        )
    }
}

/** "or continue with" divider. */
@Composable
fun OrDivider(text: String = "or continue with", modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(t.hairlineStrong))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = t.textTertiary,
            modifier = Modifier.padding(horizontal = Space.sm),
        )
        Box(Modifier.weight(1f).height(1.dp).background(t.hairlineStrong))
    }
}
