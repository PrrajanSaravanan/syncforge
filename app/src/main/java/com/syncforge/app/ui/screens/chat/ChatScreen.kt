package com.syncforge.app.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.data.model.Channel
import com.syncforge.app.data.model.Message
import com.syncforge.app.data.model.MessageKind
import com.syncforge.app.data.model.User
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.CountBadge
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeAvatar
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgeSearchBar
import com.syncforge.app.ui.components.MockImagePlaceholder
import com.syncforge.app.ui.components.presenceColor
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.CodeStyle
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    onMessage: (String) -> Unit,
    viewModel: ChatViewModel = viewModel(),
) {
    val t = LocalTokens.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val typingUserId by viewModel.typingUserId.collectAsStateWithLifecycle()
    val pickerOpen by viewModel.showChannelPicker.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Keep the newest message in view as the conversation grows.
    LaunchedEffect(messages.size, activeChannel?.id) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        ChatHeader(
            channel = activeChannel,
            peer = activeChannel?.peerId?.let(viewModel::user),
            searching = searching,
            query = query,
            pickerOpen = pickerOpen,
            onQueryChange = viewModel::setQuery,
            onToggleSearch = viewModel::toggleSearch,
            onTogglePicker = viewModel::toggleChannelPicker,
        )

        // Channel switcher drops down over the conversation.
        AnimatedVisibility(
            visible = pickerOpen,
            enter = fadeIn(tween(200)) + expandVertically(tween(260)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(200)),
        ) {
            ChannelPicker(
                channels = channels,
                activeId = activeChannel?.id,
                onSelect = viewModel::selectChannel,
                resolveUser = viewModel::user,
            )
        }

        if (pinned.isNotEmpty() && !pickerOpen) {
            PinnedBanner(pinned.first(), viewModel.user(pinned.first().authorId))
        }

        Box(Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyState(
                    icon = if (query.isNotEmpty()) Icons.Rounded.SearchOff else Icons.Rounded.Tag,
                    title = if (query.isNotEmpty()) "No matches" else "No messages yet",
                    message = if (query.isNotEmpty()) {
                        "Nothing in this channel matches \"$query\"."
                    } else {
                        "Be the first to post in this channel."
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = Space.sm, end = Space.sm, top = Space.xs, bottom = Space.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    // Date separators + author grouping: consecutive messages from the
                    // same person within a day collapse into one avatar block.
                    messages.forEachIndexed { index, message ->
                        val previous = messages.getOrNull(index - 1)
                        if (previous == null || previous.dayLabel != message.dayLabel) {
                            item(key = "day-${message.id}") {
                                DateSeparator(message.dayLabel)
                            }
                        }
                        val grouped = previous != null &&
                            previous.authorId == message.authorId &&
                            previous.dayLabel == message.dayLabel &&
                            previous.kind != MessageKind.System

                        item(key = message.id) {
                            MessageRow(
                                message = message,
                                author = viewModel.user(message.authorId),
                                isMine = message.authorId == viewModel.currentUserId,
                                grouped = grouped,
                                onReact = { emoji -> viewModel.toggleReaction(message.id, emoji) },
                                onAttachment = { onMessage("${it} is a mock attachment") },
                            )
                        }
                    }

                    if (typingUserId != null) {
                        item(key = "typing") {
                            TypingIndicator(viewModel.user(typingUserId!!))
                        }
                    }
                }
            }
        }

        Composer(
            value = draft,
            onValueChange = viewModel::setDraft,
            onSend = viewModel::send,
            onAttach = { onMessage("Attachments are UI only in this prototype") },
            onVoice = { onMessage("Hold to record — disabled in this prototype") },
        )
    }
}

@Composable
private fun ChatHeader(
    channel: Channel?,
    peer: User?,
    searching: Boolean,
    query: String,
    pickerOpen: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onTogglePicker: () -> Unit,
) {
    val t = LocalTokens.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(t.accent.copy(alpha = 0.10f), Color.Transparent)),
            )
            .statusBarsPadding()
            .padding(horizontal = Space.gutter)
            .padding(top = Space.sm, bottom = Space.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .clip(Radii.pill)
                    .springPress(pressedScale = 0.98f, onClick = onTogglePicker)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (peer != null) {
                    ForgeAvatar(peer, size = 40.dp, showPresence = true)
                } else {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(t.accentMuted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Tag,
                            null,
                            tint = t.accent,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
                Spacer(Modifier.width(Space.xs))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            channel?.name ?: "Select a channel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = t.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = "Switch channel",
                            tint = t.textTertiary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        if (peer != null) {
                            "${peer.presence.label} · ${peer.timezone}"
                        } else {
                            "${channel?.memberCount ?: 0} members · ${channel?.topic.orEmpty()}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = t.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ForgeIconButton(
                icon = if (searching) Icons.Rounded.Close else Icons.Rounded.Search,
                contentDescription = if (searching) "Close search" else "Search messages",
                onClick = onToggleSearch,
            )
        }

        AnimatedVisibility(
            visible = searching,
            enter = fadeIn(tween(200)) + expandVertically(tween(240)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(180)),
        ) {
            Column {
                Spacer(Modifier.height(Space.xs))
                ForgeSearchBar(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = "Search this conversation",
                )
                Spacer(Modifier.height(Space.xxs))
            }
        }
    }
}

@Composable
private fun ChannelPicker(
    channels: List<Channel>,
    activeId: String?,
    onSelect: (String) -> Unit,
    resolveUser: (String) -> User,
) {
    val t = LocalTokens.current
    val grouped = channels.groupBy { it.isDirect }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter)
            .clip(Radii.card)
            .background(t.surface2)
            .border(1.dp, t.hairline, Radii.card)
            .padding(Space.xs),
    ) {
        grouped[false]?.let { list ->
            Eyebrow("Channels", modifier = Modifier.padding(Space.xs))
            list.forEach { channel ->
                ChannelRow(channel, channel.id == activeId, null, onSelect)
            }
        }
        grouped[true]?.let { list ->
            Spacer(Modifier.height(Space.xs))
            Eyebrow("Direct messages", modifier = Modifier.padding(Space.xs))
            list.forEach { channel ->
                ChannelRow(
                    channel,
                    channel.id == activeId,
                    channel.peerId?.let(resolveUser),
                    onSelect,
                )
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    selected: Boolean,
    peer: User?,
    onSelect: (String) -> Unit,
) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) t.accentMuted else Color.Transparent)
            .springPress(pressedScale = 0.99f) { onSelect(channel.id) }
            .padding(horizontal = Space.xs, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (peer != null) {
            ForgeAvatar(peer, size = 26.dp, showPresence = true)
        } else {
            Icon(
                Icons.Rounded.Tag,
                null,
                tint = if (selected) t.accent else t.textTertiary,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(Space.xs))
        Text(
            channel.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected || channel.unread > 0) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) t.accent else t.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            channel.lastActivity,
            style = MaterialTheme.typography.labelSmall,
            color = t.textTertiary,
        )
        if (channel.unread > 0) {
            Spacer(Modifier.width(6.dp))
            CountBadge(channel.unread, color = t.accent)
        }
    }
}

@Composable
private fun PinnedBanner(message: Message, author: User) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter, vertical = Space.xxs)
            .clip(RoundedCornerShape(12.dp))
            .background(t.warning.copy(alpha = 0.12f))
            .padding(horizontal = Space.sm, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.PushPin,
            null,
            tint = t.warning,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "${author.name.substringBefore(' ')}: ${message.body}",
            style = MaterialTheme.typography.labelMedium,
            color = t.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DateSeparator(label: String) {
    val t = LocalTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(t.hairline))
        Box(
            Modifier
                .padding(horizontal = Space.xs)
                .clip(Radii.pill)
                .background(t.surface3)
                .padding(horizontal = 11.dp, vertical = 4.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = t.textTertiary,
            )
        }
        Box(Modifier.weight(1f).height(1.dp).background(t.hairline))
    }
}

@Composable
private fun TypingIndicator(user: User) {
    val t = LocalTokens.current
    val transition = rememberInfiniteTransition(label = "typing")

    Row(
        Modifier.padding(start = 42.dp, top = Space.xxs, bottom = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .clip(Radii.bubbleTheirs)
                .background(t.surface3)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Three dots on staggered phases — the classic typing pulse.
            repeat(3) { index ->
                val alpha by transition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 180),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "dot$index",
                )
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(t.textSecondary.copy(alpha = alpha)),
                )
            }
        }
        Spacer(Modifier.width(Space.xs))
        Text(
            "${user.name.substringBefore(' ')} is typing",
            style = MaterialTheme.typography.labelSmall,
            color = t.textTertiary,
        )
    }
}

@Composable
private fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onVoice: () -> Unit,
) {
    val t = LocalTokens.current
    val canSend = value.isNotBlank()

    Row(
        Modifier
            .fillMaxWidth()
            .background(t.canvas)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = Space.sm)
            .padding(bottom = BottomBarInset - 24.dp, top = Space.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        Icon(
            Icons.Rounded.AddCircleOutline,
            contentDescription = "Add attachment",
            tint = t.textTertiary,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .springPress(pressedScale = 0.86f, onClick = onAttach)
                .padding(11.dp),
        )

        Row(
            Modifier
                .weight(1f)
                .clip(Radii.card)
                .background(t.surface2)
                .border(1.dp, t.hairline, Radii.card)
                .padding(horizontal = Space.sm, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "Message the channel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = t.textTertiary,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = t.textPrimary),
                    cursorBrush = SolidColor(t.accent),
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.width(Space.xs))

        // Mic swaps to Send as soon as there's a draft.
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (canSend) t.accentGradient else SolidColor(t.surface3))
                .springPress(pressedScale = 0.88f) { if (canSend) onSend() else onVoice() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (canSend) Icons.AutoMirrored.Rounded.Send else Icons.Rounded.Mic,
                contentDescription = if (canSend) "Send message" else "Record voice message",
                tint = if (canSend) t.onAccent else t.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
