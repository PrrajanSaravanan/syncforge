package com.syncforge.app.data.repository

import com.syncforge.app.data.mock.MockActivity
import com.syncforge.app.data.mock.MockAi
import com.syncforge.app.data.mock.MockAnalytics
import com.syncforge.app.data.mock.MockChat
import com.syncforge.app.data.mock.MockNotifications
import com.syncforge.app.data.mock.MockProjects
import com.syncforge.app.data.mock.MockTasks
import com.syncforge.app.data.mock.MockUsers
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.AppNotification
import com.syncforge.app.data.model.Channel
import com.syncforge.app.data.model.Message
import com.syncforge.app.data.model.MessageKind
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Reaction
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.collections.immutable.toPersistentList

/**
 * Single in-memory source of truth. Seeded from the Mock* objects and mutated
 * locally so interactions (moving a card, reacting, sending a message) persist
 * for the lifetime of the process. No network, no disk, no backend.
 */
class SyncForgeRepository private constructor() {

    private val _projects = MutableStateFlow(MockProjects.all)
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _tasks = MutableStateFlow(MockTasks.all)
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _messages = MutableStateFlow(MockChat.messages)
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _channels = MutableStateFlow(MockChat.channels)
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _notifications = MutableStateFlow(MockNotifications.all)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _activity = MutableStateFlow(MockActivity.all)
    val activity: StateFlow<List<ActivityEvent>> = _activity.asStateFlow()

    val users: List<User> = MockUsers.all
    val currentUser: User = MockUsers.currentUser
    val analytics = MockAnalytics.analytics
    val nodes = MockAnalytics.nodes
    val insights = MockAnalytics.insights
    val achievements = MockAnalytics.achievements
    val contributions = MockAnalytics.contributions

    // ── Lookups ──────────────────────────────────────────────────────────────

    fun user(id: String): User = MockUsers.user(id)
    fun project(id: String): Project? = _projects.value.firstOrNull { it.id == id }
    fun task(id: String): Task? = _tasks.value.firstOrNull { it.id == id }
    fun channel(id: String): Channel? = _channels.value.firstOrNull { it.id == id }

    fun tasksFor(projectId: String): List<Task> =
        _tasks.value.filter { it.projectId == projectId }

    fun messagesFor(channelId: String): List<Message> =
        _messages.value.filter { it.channelId == channelId }

    // ── Simulated latency, so shimmer states are actually exercised ──────────

    suspend fun simulateLoad(millis: Long = 900) = delay(millis)

    suspend fun simulateRefresh(millis: Long = 1_200) = delay(millis)

    // ── Mutations ────────────────────────────────────────────────────────────

    fun toggleStar(projectId: String) = _projects.update { list ->
        list.map { if (it.id == projectId) it.copy(starred = !it.starred) else it }
    }

    fun moveTask(taskId: String, status: TaskStatus) = _tasks.update { list ->
        list.map { if (it.id == taskId) it.copy(status = status) else it }
    }

    fun toggleChecklistItem(taskId: String, itemId: String) = _tasks.update { list ->
        list.map { task ->
            if (task.id != taskId) return@map task
            val updated = task.checklist.map {
                if (it.id == itemId) it.copy(done = !it.done) else it
            }.toPersistentList()
            task.copy(checklist = updated, subtasksDone = updated.count { it.done })
        }
    }

    fun sendMessage(channelId: String, body: String) {
        if (body.isBlank()) return
        val message = Message(
            id = "m-local-${System.currentTimeMillis()}",
            channelId = channelId,
            authorId = currentUser.id,
            body = body.trim(),
            timeLabel = "now",
            dayLabel = "Today",
            kind = MessageKind.Text,
        )
        _messages.update { it + message }
        _channels.update { list ->
            list.map { if (it.id == channelId) it.copy(lastActivity = "now") else it }
        }
    }

    fun toggleReaction(messageId: String, emoji: String) = _messages.update { list ->
        list.map { msg ->
            if (msg.id != messageId) return@map msg
            val existing = msg.reactions.firstOrNull { it.emoji == emoji }
            val next = when {
                existing == null -> msg.reactions + Reaction(emoji, 1, true)
                existing.mine && existing.count <= 1 -> msg.reactions - existing
                existing.mine -> msg.reactions.map {
                    if (it.emoji == emoji) it.copy(count = it.count - 1, mine = false) else it
                }
                else -> msg.reactions.map {
                    if (it.emoji == emoji) it.copy(count = it.count + 1, mine = true) else it
                }
            }
            msg.copy(reactions = next.toPersistentList())
        }
    }

    fun markChannelRead(channelId: String) = _channels.update { list ->
        list.map { if (it.id == channelId) it.copy(unread = 0) else it }
    }

    fun markNotificationRead(id: String) = _notifications.update { list ->
        list.map { if (it.id == id) it.copy(read = true) else it }
    }

    fun markAllNotificationsRead() = _notifications.update { list ->
        list.map { it.copy(read = true) }
    }

    fun dismissNotification(id: String) = _notifications.update { list ->
        list.filterNot { it.id == id }
    }

    // ── AI ───────────────────────────────────────────────────────────────────

    fun aiReply(prompt: String): String = MockAi.reply(prompt)

    val aiSuggestions = MockAi.suggestedPrompts
    val aiQuickActions = MockAi.quickActions
    val aiGreeting = MockAi.greeting

    companion object {
        @Volatile private var instance: SyncForgeRepository? = null

        fun get(): SyncForgeRepository =
            instance ?: synchronized(this) {
                instance ?: SyncForgeRepository().also { instance = it }
            }
    }
}
