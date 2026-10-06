package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.Channel
import com.syncforge.app.data.model.Message
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _activeChannelId = MutableStateFlow("c-01")
    val activeChannelId: StateFlow<String> = _activeChannelId.asStateFlow()

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** Peer typing indicator — driven by a local ticker, not a socket. */
    private val _typingUserId = MutableStateFlow<String?>(null)
    val typingUserId: StateFlow<String?> = _typingUserId.asStateFlow()

    private val _showChannelPicker = MutableStateFlow(false)
    val showChannelPicker: StateFlow<Boolean> = _showChannelPicker.asStateFlow()

    val channels: StateFlow<List<Channel>> = repo.channels

    val activeChannel: StateFlow<Channel?> =
        combine(repo.channels, _activeChannelId) { list, id ->
            list.firstOrNull { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<Message>> =
        combine(repo.messages, _activeChannelId, _query) { all, id, q ->
            val forChannel = all.filter { it.channelId == id }
            if (q.isBlank()) forChannel
            else forChannel.filter { it.body.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pinned: StateFlow<List<Message>> =
        combine(repo.messages, _activeChannelId) { all, id ->
            all.filter { it.channelId == id && it.pinned }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.simulateLoad(600)
            _loading.value = false
            repo.markChannelRead(_activeChannelId.value)
            simulateTyping()
        }
    }

    /** Cycles a teammate "typing…" bubble so the screen feels live. */
    private suspend fun simulateTyping() {
        val cast = listOf("u-02", "u-04", "u-05", "u-03")
        var i = 0
        while (true) {
            delay(7_000)
            _typingUserId.value = cast[i % cast.size]
            i++
            delay(3_500)
            _typingUserId.value = null
        }
    }

    fun selectChannel(id: String) {
        _activeChannelId.value = id
        _query.value = ""
        _searching.value = false
        _showChannelPicker.value = false
        repo.markChannelRead(id)
    }

    fun setDraft(value: String) { _draft.value = value }

    fun send() {
        val body = _draft.value
        if (body.isBlank()) return
        repo.sendMessage(_activeChannelId.value, body)
        _draft.value = ""
    }

    fun toggleReaction(messageId: String, emoji: String) = repo.toggleReaction(messageId, emoji)

    fun setQuery(value: String) { _query.value = value }
    fun toggleSearch() {
        _searching.value = !_searching.value
        if (!_searching.value) _query.value = ""
    }

    fun toggleChannelPicker() { _showChannelPicker.value = !_showChannelPicker.value }

    fun user(id: String) = repo.user(id)
    val currentUserId = repo.currentUser.id
}
