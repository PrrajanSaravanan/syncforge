package com.syncforge.app.data.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// ─────────────────────────────────────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────────────────────────────────────

enum class Priority(val label: String) {
    Critical("Critical"), High("High"), Medium("Medium"), Low("Low")
}

enum class TaskStatus(val label: String) {
    Todo("Todo"), InProgress("In Progress"), Review("Review"), Completed("Completed")
}

enum class ProjectStatus(val label: String) {
    Active("Active"), AtRisk("At Risk"), Planning("Planning"),
    OnHold("On Hold"), Shipped("Shipped")
}

enum class PresenceStatus(val label: String) {
    Online("Online"), Busy("In a meeting"), Away("Away"), Offline("Offline")
}

enum class ActivityKind(val label: String) {
    Commit("Commit"), PullRequest("Pull Request"), Deployment("Deployment"),
    TaskUpdate("Task"), Comment("Comment"), MemberJoin("Member"),
    Release("Release"), Incident("Incident"), Review("Review")
}

enum class NotificationKind(val label: String) {
    Mention("Mentions"), Assignment("Assigned"), Deployment("Deploys"),
    Review("Reviews"), System("System"), Incident("Incidents")
}

enum class MessageKind { Text, Image, File, Voice, Code, System }

// ─────────────────────────────────────────────────────────────────────────────
// Core entities
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class User(
    val id: String,
    val name: String,
    val handle: String,
    val role: String,
    val email: String,
    val organization: String,
    val initials: String,
    val avatarSeed: Int,
    val presence: PresenceStatus,
    val timezone: String,
    val location: String,
    val tasksOpen: Int,
    val tasksClosed: Int,
    val commits: Int,
    val reviews: Int,
    val throughput: Float,           // 0f..1f — relative velocity vs. team median
    val focusArea: String,
    val skills: ImmutableList<String> = persistentListOf(),
    val joinedOn: String = "",
)

@Immutable
data class Project(
    val id: String,
    val name: String,
    val key: String,                 // e.g. "SF-CORE"
    val description: String,
    val category: String,
    val status: ProjectStatus,
    val priority: Priority,
    val progress: Float,             // 0f..1f
    val dueLabel: String,
    val daysRemaining: Int,
    val taskTotal: Int,
    val taskDone: Int,
    val openIssues: Int,
    val memberIds: ImmutableList<String>,
    val leadId: String,
    val healthScore: Int,            // 0..100
    val velocity: Int,               // story points / sprint
    val budgetUsed: Float,           // 0f..1f
    val repo: String,
    val stack: ImmutableList<String>,
    val accentIndex: Int,            // picks a gradient from the chart series
    val starred: Boolean = false,
    val sprint: String = "",
    val coverage: Int = 0,           // test coverage %
) {
    val remainingTasks: Int get() = (taskTotal - taskDone).coerceAtLeast(0)
}

@Immutable
data class Task(
    val id: String,
    val ref: String,                 // e.g. "SF-1042"
    val title: String,
    val description: String,
    val projectId: String,
    val status: TaskStatus,
    val priority: Priority,
    val assigneeIds: ImmutableList<String>,
    val reporterId: String,
    val labels: ImmutableList<String>,
    val dueLabel: String,
    val overdue: Boolean,
    val storyPoints: Int,
    val commentCount: Int,
    val attachmentCount: Int,
    val subtasksDone: Int,
    val subtasksTotal: Int,
    val checklist: ImmutableList<ChecklistItem> = persistentListOf(),
    val createdLabel: String = "",
    val branch: String = "",
) {
    val subtaskProgress: Float
        get() = if (subtasksTotal == 0) 0f else subtasksDone.toFloat() / subtasksTotal
}

@Immutable
data class ChecklistItem(val id: String, val text: String, val done: Boolean)

@Immutable
data class Channel(
    val id: String,
    val name: String,
    val topic: String,
    val isPrivate: Boolean,
    val unread: Int,
    val memberCount: Int,
    val lastActivity: String,
    val isDirect: Boolean = false,
    val peerId: String? = null,
    val muted: Boolean = false,
)

@Immutable
data class Reaction(val emoji: String, val count: Int, val mine: Boolean)

@Immutable
data class Attachment(
    val id: String,
    val name: String,
    val sizeLabel: String,
    val kind: String,                // "figma" | "pdf" | "zip" | "image" | "log"
)

@Immutable
data class Message(
    val id: String,
    val channelId: String,
    val authorId: String,
    val body: String,
    val timeLabel: String,
    val dayLabel: String,            // groups messages under date separators
    val kind: MessageKind,
    val reactions: ImmutableList<Reaction> = persistentListOf(),
    val attachment: Attachment? = null,
    val pinned: Boolean = false,
    val edited: Boolean = false,
    val voiceSeconds: Int = 0,
    val voiceWaveform: ImmutableList<Float> = persistentListOf(),
    val replyToId: String? = null,
    val threadReplies: Int = 0,
    val codeLang: String = "",
)

@Immutable
data class ActivityEvent(
    val id: String,
    val kind: ActivityKind,
    val actorId: String,
    val projectId: String?,
    val title: String,
    val detail: String,
    val timeLabel: String,
    val dayGroup: String,
    val meta: String = "",           // SHA, PR number, env name…
    val succeeded: Boolean = true,
)

@Immutable
data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val timeLabel: String,
    val read: Boolean,
    val actorId: String?,
    val projectId: String?,
    val actionLabel: String? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// Analytics
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class SeriesPoint(val label: String, val value: Float)

@Immutable
data class LineSeries(
    val name: String,
    val points: ImmutableList<SeriesPoint>,
    val colorIndex: Int,
)

@Immutable
data class SprintReport(
    val name: String,
    val committed: Int,
    val completed: Int,
    val carriedOver: Int,
    val dateRange: String,
)

@Immutable
data class ContributionDay(val week: Int, val weekday: Int, val level: Int) // level 0..4

@Immutable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val tier: String,                // "Bronze" | "Silver" | "Gold" | "Platinum"
)

@Immutable
data class NodeStatus(
    val name: String,
    val region: String,
    val state: String,               // "Healthy" | "Degraded" | "Down"
    val latencyMs: Int,
    val load: Float,                 // 0f..1f
)

@Immutable
data class AiInsight(
    val id: String,
    val headline: String,
    val body: String,
    val confidence: Int,             // %
    val tag: String,
)

@Immutable
data class AiMessage(
    val id: String,
    val body: String,
    val fromUser: Boolean,
    val timeLabel: String,
    val streaming: Boolean = false,
    val chips: ImmutableList<String> = persistentListOf(),
)

@Immutable
data class Analytics(
    val weeklyProductivity: ImmutableList<SeriesPoint>,
    val sprintVelocity: ImmutableList<SeriesPoint>,
    val taskDistribution: ImmutableList<SeriesPoint>,
    val burndown: ImmutableList<LineSeries>,
    val deployFrequency: ImmutableList<SeriesPoint>,
    val cycleTimeDays: Float,
    val reviewTimeHours: Float,
    val mergeRate: Float,
    val sprintReports: ImmutableList<SprintReport>,
    val languageMix: ImmutableList<SeriesPoint>,
)
