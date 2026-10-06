package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.ProjectStatus
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProjectSort(val label: String) {
    Recent("Recently active"),
    Progress("Progress"),
    Deadline("Deadline"),
    Priority("Priority"),
    Health("Health"),
    Alphabetical("A–Z"),
}

data class ProjectFilters(
    val query: String = "",
    val status: ProjectStatus? = null,
    val category: String = "All",
    val sort: ProjectSort = ProjectSort.Recent,
    val gridMode: Boolean = true,
    val starredOnly: Boolean = false,
)

class ProjectsViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _filters = MutableStateFlow(ProjectFilters())
    val filters: StateFlow<ProjectFilters> = _filters.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val categories = com.syncforge.app.data.mock.MockProjects.categories

    val projects: StateFlow<List<Project>> =
        combine(repo.projects, _filters) { list, f -> applyFilters(list, f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.simulateLoad()
            _loading.value = false
        }
    }

    private fun applyFilters(list: List<Project>, f: ProjectFilters): List<Project> {
        val q = f.query.trim().lowercase()
        val filtered = list.filter { p ->
            val matchesQuery = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.key.lowercase().contains(q) ||
                p.description.lowercase().contains(q) ||
                p.stack.any { it.lowercase().contains(q) }
            val matchesStatus = f.status == null || p.status == f.status
            val matchesCategory = f.category == "All" || p.category == f.category
            val matchesStar = !f.starredOnly || p.starred
            matchesQuery && matchesStatus && matchesCategory && matchesStar
        }
        return when (f.sort) {
            ProjectSort.Recent -> filtered.sortedByDescending { it.healthScore + it.velocity }
            ProjectSort.Progress -> filtered.sortedByDescending { it.progress }
            ProjectSort.Deadline -> filtered.sortedBy { it.daysRemaining }
            ProjectSort.Priority -> filtered.sortedBy { it.priority.ordinal }
            ProjectSort.Health -> filtered.sortedByDescending { it.healthScore }
            ProjectSort.Alphabetical -> filtered.sortedBy { it.name }
        }
    }

    fun setQuery(value: String) = _filters.update { it.copy(query = value) }
    fun setStatus(value: ProjectStatus?) = _filters.update { it.copy(status = value) }
    fun setCategory(value: String) = _filters.update { it.copy(category = value) }
    fun setSort(value: ProjectSort) = _filters.update { it.copy(sort = value) }
    fun toggleGrid() = _filters.update { it.copy(gridMode = !it.gridMode) }
    fun toggleStarredOnly() = _filters.update { it.copy(starredOnly = !it.starredOnly) }
    fun clearFilters() {
        // Preserve the grid/list choice — it's a view preference, not a filter.
        _filters.value = ProjectFilters(gridMode = _filters.value.gridMode)
    }

    fun toggleStar(id: String) = repo.toggleStar(id)

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            repo.simulateRefresh()
            _refreshing.value = false
        }
    }
}
