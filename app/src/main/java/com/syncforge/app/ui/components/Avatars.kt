package com.syncforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncforge.app.data.model.PresenceStatus
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.SignalSuccess
import com.syncforge.app.ui.theme.SignalWarning

/**
 * Avatars are drawn, not fetched — the app has no network. Each user's seed maps
 * to a fixed two-stop gradient so the same person is always the same color.
 */
private val avatarGradients: List<List<Color>> = listOf(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
    listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFFA855F7), Color(0xFFEC4899)),
    listOf(Color(0xFF3B82F6), Color(0xFF6366F1)),
    listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
    listOf(Color(0xFF14B8A6), Color(0xFF22C55E)),
    listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
)

fun avatarBrush(seed: Int): Brush =
    Brush.linearGradient(avatarGradients[seed.mod(avatarGradients.size)])

fun presenceColor(status: PresenceStatus): Color = when (status) {
    PresenceStatus.Online -> SignalSuccess
    PresenceStatus.Busy -> SignalWarning
    PresenceStatus.Away -> Color(0xFF94A3B8)
    PresenceStatus.Offline -> Color(0xFF64748B)
}

@Composable
fun ForgeAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showPresence: Boolean = false,
    ringColor: Color? = null,
) {
    val t = LocalTokens.current
    Box(modifier.size(size)) {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(avatarBrush(user.avatarSeed))
                .then(
                    if (ringColor != null) {
                        Modifier.border(2.dp, ringColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                user.initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                // Scale the label with the circle so initials never crowd the edge.
                fontSize = (size.value * 0.36f).sp,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (showPresence) {
            val dot = (size.value * 0.28f).coerceIn(9f, 15f).dp
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(dot)
                    .clip(CircleShape)
                    .background(t.canvas)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(presenceColor(user.presence)),
            )
        }
    }
}

/** Overlapping avatar row with a "+N" overflow bubble. */
@Composable
fun AvatarStack(
    users: List<User>,
    modifier: Modifier = Modifier,
    max: Int = 4,
    size: Dp = 30.dp,
    overlap: Dp = 10.dp,
) {
    val t = LocalTokens.current
    val shown = users.take(max)
    val extra = users.size - shown.size

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        shown.forEachIndexed { index, user ->
            Box(
                Modifier
                    .offset(x = -(overlap * index))
                    .size(size)
                    .clip(CircleShape)
                    .background(t.canvas)
                    .padding(1.5.dp),
            ) {
                ForgeAvatar(user, size = size - 3.dp)
            }
        }
        if (extra > 0) {
            Box(
                Modifier
                    .offset(x = -(overlap * shown.size))
                    .size(size)
                    .clip(CircleShape)
                    .background(t.canvas)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(t.surface3),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "+$extra",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = t.textSecondary,
                )
            }
        }
    }
}

/**
 * Placeholder for "image" messages and attachment previews. Draws a deterministic
 * abstract composition from the name's hash rather than shipping bitmap assets.
 */
@Composable
fun MockImagePlaceholder(
    seedText: String,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val hash = seedText.hashCode()
    val colors = avatarGradients[hash.mod(avatarGradients.size)]
    val t = LocalTokens.current

    BoxWithConstraints(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.linearGradient(colors.map { it.copy(alpha = 0.85f) })),
        )
        // Two soft blooms make the fill read as a screenshot thumbnail, not a flat swatch.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                        radius = maxWidth.value * 1.6f,
                    ),
                ),
        )
        if (label != null) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color.Black.copy(alpha = 0.42f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Box(
            Modifier
                .matchParentSize()
                .border(1.dp, t.glassBorder, MaterialTheme.shapes.medium),
        )
    }
}
