package com.syncforge.app.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.AiMessage
import com.syncforge.app.ui.components.AuroraBackground
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.GlassCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.AiViewModel

@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    viewModel: AiViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val thinking by viewModel.thinking.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Follow the stream as it lands.
    LaunchedEffect(messages.size, messages.lastOrNull()?.body?.length) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    AuroraBackground(Modifier.fillMaxSize(), intensity = 0.75f) {
        Column(Modifier.fillMaxSize()) {
            AiHeader(onBack = onBack, onReset = viewModel::reset)

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = Space.gutter, vertical = Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.fromUser) {
                        UserBubble(message)
                    } else {
                        AssistantBubble(message, onChip = viewModel::send)
                    }
                }

                if (thinking) {
                    item(key = "thinking") { ThinkingBubble() }
                }

                // Prompt suggestions only while the conversation is still fresh.
                if (messages.size <= 1) {
                    item(key = "quick") {
                        QuickActionGrid(viewModel.quickActions) { viewModel.send(it) }
                    }
                }
            }

            if (messages.size <= 3) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    modifier = Modifier.padding(bottom = Space.xs),
                ) {
                    items(viewModel.suggestions) { prompt ->
                        SuggestionChip(prompt) { viewModel.send(prompt) }
                    }
                }
            }

            AiComposer(
                value = draft,
                enabled = !thinking,
                onValueChange = viewModel::setDraft,
                onSend = { viewModel.send() },
            )
        }
    }
}

@Composable
private fun AiHeader(onBack: () -> Unit, onReset: () -> Unit) {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "aiOrb")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9_000)),
        label = "spin",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2_200), RepeatMode.Reverse),
        label = "pulse",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForgeIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, contentDescription = "Back")
        Spacer(Modifier.width(Space.sm))

        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(44.dp)
                    .scale(pulse)
                    .rotate(spin)
                    .background(
                        Brush.sweepGradient(
                            listOf(t.series[0], t.series[2], t.series[1], t.series[0]),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(t.canvas),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    null,
                    tint = t.series[2],
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.width(Space.sm))
        Column(Modifier.weight(1f)) {
            Text(
                "Forge AI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = t.textPrimary,
            )
            Text(
                "On-device · workspace aware",
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary,
            )
        }
        ForgeIconButton(
            Icons.Rounded.Refresh,
            onReset,
            contentDescription = "Reset conversation",
        )
    }
}

@Composable
private fun UserBubble(message: AiMessage) {
    val t = LocalTokens.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .widthIn(max = 290.dp)
                .clip(Radii.bubbleMine)
                .background(t.accentGradient)
                .padding(horizontal = 15.dp, vertical = 11.dp),
        ) {
            Text(
                message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = t.onAccent,
            )
        }
    }
}

@Composable
private fun AssistantBubble(message: AiMessage, onChip: (String) -> Unit) {
    val t = LocalTokens.current
    Column {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = Radii.bubbleTheirs,
            strong = true,
            contentPadding = Space.md,
        ) {
            Column {
                Text(
                    renderMarkdown(message.body, t.textPrimary, t.accent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = t.textPrimary,
                )
                // Caret blinks while the response streams in.
                if (message.streaming) {
                    Spacer(Modifier.height(6.dp))
                    StreamingCaret()
                }
            }
        }

        if (message.chips.isNotEmpty()) {
            Spacer(Modifier.height(Space.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                items(message.chips) { chip ->
                    SuggestionChip(chip) { onChip(chip) }
                }
            }
        }
    }
}

/**
 * Minimal inline markdown: **bold** and *italic* only. The mock replies are the
 * only source, so a full parser would be dead weight.
 */
@Composable
private fun renderMarkdown(text: String, base: Color, accent: Color) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end == -1) {
                    append(text.substring(i)); i = text.length
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accent)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                }
            }
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end == -1) {
                    append(text.substring(i)); i = text.length
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = base.copy(alpha = 0.85f))) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                }
            }
            else -> {
                append(text[i]); i++
            }
        }
    }
}

@Composable
private fun StreamingCaret() {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "blink",
    )
    Box(
        Modifier
            .width(9.dp)
            .height(15.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(t.accent.copy(alpha = alpha)),
    )
}

@Composable
private fun ThinkingBubble() {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "thinking")

    GlassCard(
        modifier = Modifier.widthIn(max = 200.dp),
        shape = Radii.bubbleTheirs,
        contentPadding = Space.sm,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                val scale by transition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(560, delayMillis = index * 160),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "dot$index",
                )
                Box(
                    Modifier
                        .padding(horizontal = 2.5.dp)
                        .size(7.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(t.series[2]),
                )
            }
            Spacer(Modifier.width(Space.xs))
            Text(
                "Reading workspace",
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary,
            )
        }
    }
}

@Composable
private fun QuickActionGrid(
    actions: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    val t = LocalTokens.current
    Column {
        Spacer(Modifier.height(Space.xs))
        Eyebrow("Quick actions", color = t.series[2])
        Spacer(Modifier.height(Space.xs))
        actions.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                row.forEach { (label, prompt) ->
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(Radii.card)
                            .background(t.surface2)
                            .border(1.dp, t.hairline, Radii.card)
                            .springPress(pressedScale = 0.97f) { onSelect(prompt) }
                            .padding(Space.sm),
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    val t = LocalTokens.current
    Box(
        Modifier
            .clip(Radii.pill)
            .background(t.glassFill)
            .border(1.dp, t.glassBorder, Radii.pill)
            .springPress(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = Space.sm, vertical = 9.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = t.textSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun AiComposer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val t = LocalTokens.current
    val canSend = enabled && value.isNotBlank()

    Row(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(bottom = Space.md, top = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .clip(Radii.pill)
                .background(t.glassFillStrong)
                .border(1.dp, t.glassBorder, Radii.pill)
                .padding(horizontal = Space.md, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "Ask about your workspace",
                        style = MaterialTheme.typography.bodyMedium,
                        color = t.textTertiary,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = t.textPrimary),
                    cursorBrush = SolidColor(t.accent),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.width(Space.xs))
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (canSend) t.accentGradient else SolidColor(t.surface3))
                .springPress(enabled = canSend, pressedScale = 0.88f, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send",
                tint = if (canSend) t.onAccent else t.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
