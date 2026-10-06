package com.syncforge.app.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddReaction
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncforge.app.data.model.Message
import com.syncforge.app.data.model.MessageKind
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.MockImagePlaceholder
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.CodeStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space

private val quickReactions = listOf("👍", "🔥", "✅", "🎉", "👀")

/**
 * One message. Mine render right-aligned on an accent gradient, others left on a
 * neutral surface. Tapping a bubble reveals the quick-reaction row.
 */
@Composable
fun MessageRow(
    message: Message,
    author: User,
    isMine: Boolean,
    grouped: Boolean,
    onReact: (String) -> Unit,
    onAttachment: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current

    if (message.kind == MessageKind.System) {
        SystemMessage(message.body)
        return
    }

    var showReactions by remember { mutableStateOf(false) }

    Row(
        modifier
            .fillMaxWidth()
            .padding(top = if (grouped) 1.dp else Space.xs),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isMine) {
            // Gutter keeps grouped messages aligned with the first bubble's text.
            Box(Modifier.width(38.dp), contentAlignment = Alignment.TopCenter) {
                if (!grouped) ForgeAvatar(author, size = 32.dp, showPresence = true)
            }
            Spacer(Modifier.width(Space.xs))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            if (!grouped) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isMine) {
                        Text(
                            author.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = t.textPrimary,
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        message.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                    )
                    if (message.pinned) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = t.warning,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
            }

            Box(
                Modifier
                    .clip(if (isMine) Radii.bubbleMine else Radii.bubbleTheirs)
                    .background(
                        if (isMine) {
                            t.accentGradient
                        } else {
                            Brush.linearGradient(listOf(t.surface3, t.surface3))
                        },
                    )
                    .springPress(pressedScale = 0.98f) { showReactions = !showReactions },
            ) {
                when (message.kind) {
                    MessageKind.Image -> ImageBubble(message, isMine, onAttachment)
                    MessageKind.File -> FileBubble(message, isMine, onAttachment)
                    MessageKind.Voice -> VoiceBubble(message, isMine)
                    MessageKind.Code -> CodeBubble(message, isMine)
                    else -> TextBubble(message, isMine)
                }
            }

            if (message.reactions.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    message.reactions.forEach { reaction ->
                        ReactionPill(
                            emoji = reaction.emoji,
                            count = reaction.count,
                            mine = reaction.mine,
                            onClick = { onReact(reaction.emoji) },
                        )
                    }
                }
            }

            if (message.threadReplies > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${message.threadReplies} replies",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = t.accent,
                )
            }

            AnimatedVisibility(
                visible = showReactions,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
            ) {
                Row(
                    Modifier
                        .padding(top = 5.dp)
                        .clip(Radii.pill)
                        .background(t.surface2)
                        .border(1.dp, t.hairline, Radii.pill)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    quickReactions.forEach { emoji ->
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .springPress(pressedScale = 0.82f) {
                                    onReact(emoji)
                                    showReactions = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emoji, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (isMine) Spacer(Modifier.width(Space.xs))
    }
}

@Composable
private fun TextBubble(message: Message, isMine: Boolean) {
    val t = LocalTokens.current
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            message.body,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMine) t.onAccent else t.textPrimary,
        )
        if (message.edited) {
            Spacer(Modifier.height(2.dp))
            Text(
                "edited",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) t.onAccent.copy(alpha = 0.7f) else t.textTertiary,
            )
        }
    }
}

@Composable
private fun ImageBubble(message: Message, isMine: Boolean, onAttachment: (String) -> Unit) {
    val t = LocalTokens.current
    val name = message.attachment?.name ?: message.body
    Column(Modifier.padding(5.dp)) {
        MockImagePlaceholder(
            seedText = name,
            label = message.attachment?.sizeLabel,
            modifier = Modifier
                .width(250.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(15.dp))
                .springPress(pressedScale = 0.98f) { onAttachment(name) },
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Image,
                null,
                tint = if (isMine) t.onAccent else t.textTertiary,
                modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) t.onAccent else t.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FileBubble(message: Message, isMine: Boolean, onAttachment: (String) -> Unit) {
    val t = LocalTokens.current
    val file = message.attachment ?: return
    val (icon, tone) = when (file.kind) {
        "figma" -> Icons.Rounded.Image to t.series[4]
        "log" -> Icons.Rounded.Terminal to t.series[3]
        "zip" -> Icons.Rounded.Description to t.series[2]
        else -> Icons.Rounded.Description to t.series[0]
    }

    Row(
        Modifier
            .widthIn(min = 210.dp)
            .padding(10.dp)
            .springPress(pressedScale = 0.98f) { onAttachment(file.name) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (isMine) Color.White.copy(alpha = 0.22f) else tone.copy(alpha = 0.16f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = if (isMine) t.onAccent else tone,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(Space.xs))
        Column(Modifier.weight(1f)) {
            Text(
                file.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isMine) t.onAccent else t.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${file.kind.uppercase()} · ${file.sizeLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) t.onAccent.copy(alpha = 0.78f) else t.textTertiary,
            )
        }
    }
}

@Composable
private fun VoiceBubble(message: Message, isMine: Boolean) {
    val t = LocalTokens.current
    var playing by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "voiceProgress",
    )
    val fg = if (isMine) t.onAccent else t.textPrimary

    Row(
        Modifier
            .widthIn(min = 210.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMine) Color.White.copy(alpha = 0.22f) else t.accentMuted)
                .springPress(pressedScale = 0.85f) { playing = !playing },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play voice message",
                tint = if (isMine) t.onAccent else t.accent,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(Space.xs))

        // Waveform: bars past the playhead dim out.
        Row(
            Modifier.weight(1f).height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            message.voiceWaveform.forEachIndexed { index, amplitude ->
                val played = index.toFloat() / message.voiceWaveform.size <= progress
                Box(
                    Modifier
                        .weight(1f)
                        .height((amplitude * 26f).dp.coerceAtLeast(3.dp))
                        .clip(Radii.pill)
                        .background(
                            if (played) fg else fg.copy(alpha = 0.32f),
                        ),
                )
            }
        }
        Spacer(Modifier.width(Space.xs))
        Text(
            "0:${message.voiceSeconds.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun CodeBubble(message: Message, isMine: Boolean) {
    val t = LocalTokens.current
    Column(Modifier.padding(8.dp)) {
        Row(
            Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Terminal,
                null,
                tint = if (isMine) t.onAccent else t.textTertiary,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                message.codeLang.ifEmpty { "code" },
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) t.onAccent.copy(alpha = 0.8f) else t.textTertiary,
            )
        }
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = if (isMine) 0.28f else 0.38f))
                .padding(11.dp),
        ) {
            Text(
                message.body,
                style = CodeStyle,
                color = if (isMine) t.onAccent else t.series[3],
            )
        }
    }
}

@Composable
private fun ReactionPill(emoji: String, count: Int, mine: Boolean, onClick: () -> Unit) {
    val t = LocalTokens.current
    Row(
        Modifier
            .clip(Radii.pill)
            .background(if (mine) t.accentMuted else t.surface3)
            .border(
                1.dp,
                if (mine) t.accent.copy(alpha = 0.5f) else Color.Transparent,
                Radii.pill,
            )
            .springPress(pressedScale = 0.88f, onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(3.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (mine) t.accent else t.textSecondary,
        )
    }
}

@Composable
private fun SystemMessage(body: String) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .clip(Radii.pill)
                .background(t.danger.copy(alpha = 0.12f))
                .padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Info, null, tint = t.danger, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                body,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = t.danger,
            )
        }
    }
}
