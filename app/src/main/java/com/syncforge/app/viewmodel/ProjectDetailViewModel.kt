package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.Attachment
import com.syncforge.app.data.model.Project
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import com.syncforge.app.data.model.User
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectDetailState(
    val loading: Boolean = true,
    val project: Project? = null,
    val members: List<User> = emptyList(),
    val lead: User? = null,
    val tasks: List<Task> = emptyList(),
    val activity: List<ActivityEvent> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val statusCounts: Map<TaskStatus, Int> = emptyMap(),
)

class ProjectDetailViewModel(private val projectId: String) : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _state = MutableStateFlow(ProjectDetailState())
    val state: StateFlow<ProjectDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.simulateLoad(700)
            val project = repo.project(projectId)
            val tasks = repo.tasksFor(projectId)
            _state.value = ProjectDetailState(
                loading = false,
                project = project,
                members = project?.memberIds?.map { repo.user(it) }.orEmpty(),
                lead = project?.let { repo.user(it.leadId) },
                tasks = tasks,
                activity = repo.activity.value.filter { it.projectId == projectId },
                attachments = attachmentsFor(project),
                statusCounts = TaskStatus.entries.associateWith { s ->
                    tasks.count { it.status == s }
                },
            )
        }
    }

    /** Deterministic per-project attachment set — derived, not random. */
    private fun attachmentsFor(project: Project?): List<Attachment> {
        if (project == null) return emptyList()
        val slug = project.key.lowercase().substringAfter('-')
        return listOf(
            Attachment("at-$slug-1", "$slug-architecture.pdf", "1.8 MB", "pdf"),
            Attachment("at-$slug-2", "$slug-ui-spec.fig", "4.2 MB", "figma"),
            Attachment("at-$slug-3", "$slug-benchmarks.png", "760 KB", "image"),
            Attachment("at-$slug-4", "$slug-runbook.pdf", "412 KB", "pdf"),
            Attachment("at-$slug-5", "$slug-load-test.log", "94 KB", "log"),
        )
    }

    companion object {
        fun factory(projectId: String) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProjectDetailViewModel(projectId) as T
        }
    }
}
