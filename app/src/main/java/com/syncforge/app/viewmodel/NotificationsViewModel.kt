package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.ActivityKind
import com.syncforge.app.data.model.AppNotification
import com.syncforge.app.data.model.NotificationKind
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _filter = MutableStateFlow<NotificationKind?>(null)
    val filter: StateFlow<NotificationKind?> = _filter.asStateFlow()

    private val _unreadOnly = MutableStateFlow(false)
    val unreadOnly: StateFlow<Boolean> = _unreadOnly.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val notifications: StateFlow<List<AppNotification>> =
        combine(repo.notifications, _filter, _unreadOnly) { list, kind, unread ->
            list.filter { (kind == null || it.kind == kind) && (!unread || !it.read) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> =
        repo.notifications
            .map { list -> list.count { !it.read } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            repo.simulateLoad(550)
            _loading.value = false
        }
    }

    fun setFilter(kind: NotificationKind?) { _filter.value = kind }
    fun toggleUnreadOnly() { _unreadOnly.value = !_unreadOnly.value }
    fun markRead(id: String) = repo.markNotificationRead(id)
    fun markAllRead() = repo.markAllNotificationsRead()
    fun dismiss(id: String) = repo.dismissNotification(id)
    fun user(id: String) = repo.user(id)
    fun project(id: String) = repo.project(id)

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            repo.simulateRefresh()
            _refreshing.value = false
        }
    }
}

class ActivityViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _kind = MutableStateFlow<ActivityKind?>(null)
    val kind: StateFlow<ActivityKind?> = _kind.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val events: StateFlow<List<ActivityEvent>> =
        combine(repo.activity, _kind) { list, k ->
            list.filter { k == null || it.kind == k }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.simulateLoad(550)
            _loading.value = false
        }
    }

    fun setKind(value: ActivityKind?) { _kind.value = value }
    fun user(id: String) = repo.user(id)
    fun project(id: String?) = id?.let { repo.project(it) }

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            repo.simulateRefresh()
            _refreshing.value = false
        }
    }
}
