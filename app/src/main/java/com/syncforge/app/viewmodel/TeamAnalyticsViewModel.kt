package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.Achievement
import com.syncforge.app.data.model.Analytics
import com.syncforge.app.data.model.ContributionDay
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.model.User
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TeamMemberCard(
    val user: User,
    val currentTask: Task?,
    val projectNames: List<String>,
    val openTasks: Int,
)

class TeamViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _roleFilter = MutableStateFlow<String?>(null)
    val roleFilter: StateFlow<String?> = _roleFilter.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** Broad role buckets so the filter row stays short. */
    val roleGroups = listOf("Engineering", "Design", "Product", "Security", "Data")

    val members: StateFlow<List<TeamMemberCard>> =
        combine(repo.tasks, repo.projects, _query, _roleFilter) { tasks, projects, q, role ->
            repo.users
                .filter { u ->
                    val matchesQuery = q.isBlank() ||
                        u.name.contains(q, true) ||
                        u.role.contains(q, true) ||
                        u.handle.contains(q, true) ||
                        u.skills.any { it.contains(q, true) }
                    matchesQuery && (role == null || roleGroup(u.role) == role)
                }
                .map { u -> card(u, tasks, projects) }
                .sortedBy { it.user.presence.ordinal }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun card(u: User, tasks: List<Task>, projects: List<Project>): TeamMemberCard {
        val mine = tasks.filter { u.id in it.assigneeIds }
        val open = mine.filter { it.status != TaskStatus.Completed }
        return TeamMemberCard(
            user = u,
            currentTask = open.firstOrNull { it.status == TaskStatus.InProgress } ?: open.firstOrNull(),
            projectNames = projects.filter { u.id in it.memberIds }.map { it.name },
            openTasks = open.size,
        )
    }

    private fun roleGroup(role: String): String = when {
        role.contains("Design", true) -> "Design"
        role.contains("Product", true) -> "Product"
        role.contains("Security", true) -> "Security"
        role.contains("Data", true) -> "Data"
        else -> "Engineering"
    }

    init {
        viewModelScope.launch {
            repo.simulateLoad(600)
            _loading.value = false
        }
    }

    fun setQuery(value: String) { _query.value = value }
    fun setRole(value: String?) { _roleFilter.value = value }
}

data class AnalyticsState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val range: String = "This sprint",
    val data: Analytics? = null,
    val throughputLeaders: List<User> = emptyList(),
    val projectProgress: List<Pair<String, Float>> = emptyList(),
)

class AnalyticsViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    val ranges = listOf("This sprint", "Last 30 days", "This quarter", "All time")

    init { load() }

    private fun load() {
        viewModelScope.launch {
            repo.simulateLoad(800)
            _state.value = AnalyticsState(
                loading = false,
                data = repo.analytics,
                throughputLeaders = repo.users.sortedByDescending { it.throughput }.take(5),
                projectProgress = repo.projects.value
                    .sortedByDescending { it.progress }
                    .take(6)
                    .map { it.key to it.progress },
            )
        }
    }

    fun setRange(value: String) {
        _state.value = _state.value.copy(range = value)
    }

    fun refresh() {
        val current = _state.value
        if (current.refreshing) return
        viewModelScope.launch {
            _state.value = current.copy(refreshing = true)
            repo.simulateRefresh()
            _state.value = _state.value.copy(refreshing = false)
        }
    }
}

data class ProfileState(
    val loading: Boolean = true,
    val user: User? = null,
    val achievements: List<Achievement> = emptyList(),
    val contributions: List<ContributionDay> = emptyList(),
    val contributionTotal: Int = 0,
    val projects: List<Project> = emptyList(),
    val openTasks: Int = 0,
    val closedTasks: Int = 0,
)

class ProfileViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.simulateLoad(600)
            val me = repo.currentUser
            val mine = repo.tasks.value.filter { me.id in it.assigneeIds }
            _state.value = ProfileState(
                loading = false,
                user = me,
                achievements = repo.achievements,
                contributions = repo.contributions,
                contributionTotal = repo.contributions.sumOf { it.level * 2 },
                projects = repo.projects.value.filter { me.id in it.memberIds },
                openTasks = mine.count { it.status != TaskStatus.Completed },
                closedTasks = me.tasksClosed,
            )
        }
    }
}
