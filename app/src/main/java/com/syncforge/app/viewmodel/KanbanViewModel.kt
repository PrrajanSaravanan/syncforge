package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.Priority
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanbanFilters(
    val projectId: String? = null,
    val priority: Priority? = null,
    val assigneeId: String? = null,
    val query: String = "",
    val mineOnly: Boolean = false,
)

class KanbanViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _filters = MutableStateFlow(KanbanFilters())
    val filters: StateFlow<KanbanFilters> = _filters.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    val projects = repo.projects

    /** Tasks bucketed by column, recomputed whenever tasks or filters change. */
    val columns: StateFlow<Map<TaskStatus, List<Task>>> =
        combine(repo.tasks, _filters) { tasks, f ->
            val q = f.query.trim().lowercase()
            val filtered = tasks.filter { t ->
                (f.projectId == null || t.projectId == f.projectId) &&
                    (f.priority == null || t.priority == f.priority) &&
                    (f.assigneeId == null || f.assigneeId in t.assigneeIds) &&
                    (!f.mineOnly || repo.currentUser.id in t.assigneeIds) &&
                    (q.isEmpty() || t.title.lowercase().contains(q) || t.ref.lowercase().contains(q))
            }
            TaskStatus.entries.associateWith { status ->
                filtered.filter { it.status == status }.sortedBy { it.priority.ordinal }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TaskStatus.entries.associateWith { emptyList() },
        )

    init {
        viewModelScope.launch {
            repo.simulateLoad(650)
            _loading.value = false
        }
    }

    /** Re-reads from the task list so checklist toggles inside the sheet recompose. */
    val selectedTask: StateFlow<Task?> =
        combine(repo.tasks, _selectedTaskId) { tasks, id ->
            id?.let { sel -> tasks.firstOrNull { it.id == sel } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun openTask(id: String) { _selectedTaskId.value = id }
    fun closeTask() { _selectedTaskId.value = null }

    fun moveTask(taskId: String, to: TaskStatus) = repo.moveTask(taskId, to)

    fun toggleChecklistItem(taskId: String, itemId: String) =
        repo.toggleChecklistItem(taskId, itemId)

    fun setProject(id: String?) = _filters.update { it.copy(projectId = id) }
    fun setPriority(p: Priority?) = _filters.update { it.copy(priority = p) }
    fun setQuery(q: String) = _filters.update { it.copy(query = q) }
    fun toggleMineOnly() = _filters.update { it.copy(mineOnly = !it.mineOnly) }
    fun clearFilters() {
        _filters.value = KanbanFilters()
    }

    fun user(id: String) = repo.user(id)
    fun project(id: String) = repo.project(id)
}
