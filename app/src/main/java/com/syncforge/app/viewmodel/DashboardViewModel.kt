package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.AiInsight
import com.syncforge.app.data.model.NodeStatus
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val projectCount: Int = 0,
    val activeProjects: Int = 0,
    val taskTotal: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0,
    val sprintProgress: Float = 0f,
    val sprintCommitted: Int = 0,
    val sprintCompleted: Int = 0,
    val activeDevelopers: Int = 0,
    val totalDevelopers: Int = 0,
    val workspaceHealth: Int = 0,
    val myTasks: List<Task> = emptyList(),
    val focusProjects: List<Project> = emptyList(),
    val recentActivity: List<ActivityEvent> = emptyList(),
    val insights: List<AiInsight> = emptyList(),
    val nodes: List<NodeStatus> = emptyList(),
    val unreadNotifications: Int = 0,
)

class DashboardViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.simulateLoad()
            _state.value = compose(loading = false)
        }
    }

    fun refresh() {
        if (_state.value.refreshing) return
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            repo.simulateRefresh()
            _state.value = compose(loading = false, refreshing = false)
        }
    }

    private fun compose(loading: Boolean, refreshing: Boolean = false): DashboardState {
        val projects = repo.projects.value
        val tasks = repo.tasks.value
        val sprint = repo.analytics.sprintReports.first()
        val completed = tasks.count { it.status == TaskStatus.Completed }

        return DashboardState(
            loading = loading,
            refreshing = refreshing,
            projectCount = projects.size,
            activeProjects = projects.count { it.progress in 0.01f..0.99f },
            taskTotal = tasks.size,
            completed = completed,
            pending = tasks.size - completed,
            overdue = tasks.count { it.overdue },
            sprintProgress = sprint.completed.toFloat() / sprint.committed,
            sprintCommitted = sprint.committed,
            sprintCompleted = sprint.completed,
            activeDevelopers = repo.users.count {
                it.presence.name == "Online" || it.presence.name == "Busy"
            },
            totalDevelopers = repo.users.size,
            workspaceHealth = projects.map { it.healthScore }.average().toInt(),
            myTasks = tasks.filter {
                repo.currentUser.id in it.assigneeIds && it.status != TaskStatus.Completed
            }.sortedBy { it.priority.ordinal }.take(5),
            focusProjects = projects.filter { it.starred }.take(4),
            recentActivity = repo.activity.value.take(6),
            insights = repo.insights,
            nodes = repo.nodes,
            unreadNotifications = repo.notifications.value.count { !it.read },
        )
    }
}
