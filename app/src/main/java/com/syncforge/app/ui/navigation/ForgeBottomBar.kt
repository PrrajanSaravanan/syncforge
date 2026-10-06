package com.syncforge.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.components.CountBadge
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

/**
 * Floating glass tab bar. The selected item grows a pill behind it and lifts a
 * gradient indicator above — the spring is the same one used for card presses.
 */
@Composable
fun ForgeBottomBar(
    current: BottomTab?,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    chatBadge: Int = 0,
) {
    val t = LocalTokens.current

    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Space.sm, vertical = Space.xs),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(Radii.pill)
                .background(if (t.isDark) t.surface2.copy(alpha = 0.96f) else t.surface1)
                .border(1.dp, t.glassBorder, Radii.pill)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    selected = tab == current,
                    badge = if (tab == BottomTab.Chat) chatBadge else 0,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: BottomTab,
    selected: Boolean,
    badge: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current

    val tint by animateColorAsState(
        if (selected) t.accent else t.textTertiary,
        tween(260),
        label = "tabTint",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "iconScale",
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 20.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "indicator",
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(260),
        label = "pill",
    )

    Box(
        modifier
            .height(58.dp)
            .clip(Radii.pill)
            .springPress(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Soft accent pill behind the active tab.
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp, vertical = 5.dp)
                .clip(Radii.pill)
                .background(t.accent.copy(alpha = 0.11f * pillAlpha)),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                    contentDescription = tab.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp).scale(iconScale),
                )
                if (badge > 0) {
                    CountBadge(
                        count = badge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 12.dp, bottom = 10.dp),
                    )
                }
            }
            Text(
                tab.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tint,
            )
            Box(
                Modifier
                    .padding(top = 3.dp)
                    .width(indicatorWidth)
                    .height(2.5.dp)
                    .clip(Radii.pill)
                    .background(
                        if (selected) t.accentGradient else Brush.linearGradient(
                            listOf(Color.Transparent, Color.Transparent),
                        ),
                    ),
            )
        }
    }
}
