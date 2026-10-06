package com.syncforge.app.data.mock

import com.syncforge.app.data.model.ChecklistItem
import com.syncforge.app.data.model.Priority
import com.syncforge.app.data.model.Task
import com.syncforge.app.data.model.TaskStatus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

/**
 * 100 tasks generated from a fixed seed so the board is identical on every launch
 * (a prototype that reshuffles on rotation reads as broken). Titles are drawn from
 * per-project engineering vocabularies rather than "Task 41".
 */
object MockTasks {

    private val titlesByProject: Map<String, List<String>> = mapOf(
        "p-01" to listOf(
            "Collapse duplicate CRDT tombstones on rejoin",
            "Cut convergence p99 below 80ms in ap-south-1",
            "Backpressure the op-log when replica lag exceeds 2s",
            "Add vector-clock compaction for long-lived docs",
            "Fix presence flicker on socket re-handshake",
            "Shard the op broadcast fan-out by document id",
            "Write soak test for 500-peer document",
            "Handle clock skew between edge and origin",
        ),
        "p-02" to listOf(
            "Emit Compose color tokens from Figma variables",
            "Deprecate legacy elevation scale",
            "Document motion durations in the token spec",
            "Ship dark-mode contrast audit script",
            "Add pill and chip radius primitives",
            "Generate typography ramp for tablet breakpoints",
            "Unify icon stroke weights at 1.75dp",
        ),
        "p-03" to listOf(
            "Offline queue: retry with exponential backoff",
            "Reduce cold start with baseline profiles",
            "Migrate task list to LazyColumn keys",
            "Fix shared-element transition on project open",
            "Add pull-to-refresh to activity feed",
            "Cache avatars with Coil disk policy",
            "Handle process death in kanban state",
            "Predictive back gesture on detail screens",
        ),
        "p-04" to listOf(
            "Rotate workload identities every 15 minutes",
            "Enforce mTLS on the internal admin mesh",
            "Migrate service-to-service auth off static tokens",
            "Add policy gate for cross-namespace calls",
            "Audit privileged service accounts",
            "Break glass procedure runbook",
            "Fix SPIFFE bundle refresh race",
        ),
        "p-05" to listOf(
            "Incremental model for sprint_velocity",
            "Backfill task_events for Q2",
            "Deduplicate commit facts by SHA",
            "Add freshness test on productivity mart",
            "Partition activity table by month",
            "Document warehouse lineage graph",
        ),
        "p-06" to listOf(
            "Virtualize the 10k-row issue table",
            "Command palette fuzzy ranking tweaks",
            "Trim vendor bundle below 180kb",
            "Fix focus trap in the filter drawer",
            "Skeleton states for dashboard widgets",
            "Server-render the project overview route",
            "Keyboard shortcuts help overlay",
        ),
        "p-07" to listOf(
            "Stitch traces across the billing boundary",
            "Sample noisy health-check spans out",
            "Alert on error-budget burn rate",
            "Ship ClickHouse retention policy",
            "Correlate logs to trace ids",
            "Dashboard for queue depth by region",
        ),
        "p-08" to listOf(
            "Idempotent replay for metering events",
            "Invoice preview endpoint contract",
            "Proration rules for mid-cycle upgrades",
            "Usage rollup job scheduling",
            "Tax metadata on line items",
        ),
        "p-09" to listOf(
            "Rank diff hunks by blast radius",
            "Repo-aware context window assembly",
            "Suppress suggestions on generated files",
            "Latency budget for inline review calls",
            "Feedback loop for rejected suggestions",
            "Redact secrets before context upload",
        ),
        "p-10" to listOf(
            "Guided setup step analytics",
            "Skip-to-workspace escape hatch",
            "Empty state illustrations",
            "Sample project seeding",
        ),
        "p-11" to listOf(
            "Stale-while-revalidate invalidation hooks",
            "Cache key normalization for query params",
            "Regional purge API",
            "Hit-rate dashboard per route",
            "Guard against cache stampede",
        ),
        "p-12" to listOf(
            "Map controls to evidence sources",
            "Automate access-review exports",
            "Quarterly vendor assessment tracker",
        ),
        "p-13" to listOf(
            "Hybrid scoring: BM25 plus cosine",
            "Index conversations with permission filters",
            "Typo tolerance for task refs",
            "Re-index worker idempotency",
            "Highlight matched spans in results",
        ),
        "p-14" to listOf(
            "Echo cancellation on Android capture",
            "Live transcription partial results",
            "Shared cursor presence protocol",
            "Reconnect huddle after network flap",
            "Bandwidth adaptation ladder",
            "Fix jitter buffer overrun",
        ),
        "p-15" to listOf(
            "OpenAPI spec linting in CI",
            "Interactive request playground",
            "SDK quickstart for Kotlin",
        ),
    )

    private val labelPool = listOf(
        "backend", "frontend", "android", "infra", "design", "perf",
        "security", "data", "api", "tech-debt", "bug", "feature",
        "a11y", "docs", "flaky-test", "observability",
    )

    private val dueLabels = listOf(
        "Today", "Tomorrow", "Jul 28", "Jul 30", "Aug 04",
        "Aug 11", "Aug 18", "Aug 25", "Sep 01", "Next sprint",
    )

    private val checklistPool = listOf(
        "Reproduce on staging",
        "Add regression test",
        "Update runbook",
        "Benchmark before/after",
        "Peer review with owner",
        "Feature flag rollout",
        "Update changelog",
        "Verify on low-end device",
    )

    /** Follow-up variants let an 88-title vocabulary cover 100 tasks without repeats. */
    private val followUps = listOf(
        "follow-up", "phase 2", "hardening pass", "rollout", "cleanup",
    )

    // Declared after every list it reads: object initializers run top to bottom,
    // so building the tasks first would see the pools as null.
    val all: List<Task> = buildTasks()

    private fun buildTasks(): List<Task> {
        val rng = Random(20240724)
        val out = mutableListOf<Task>()
        var ref = 1_001

        // Round-robin across projects, wrapping each project's vocabulary once it
        // runs out so the loop always terminates at exactly 100 tasks.
        val projectIds = MockProjects.all.map { it.id }.filter {
            !titlesByProject[it].isNullOrEmpty()
        }
        val cursor = projectIds.associateWith { 0 }.toMutableMap()

        var i = 0
        while (out.size < 100) {
            val projectId = projectIds[i % projectIds.size]
            i++
            val pool = titlesByProject.getValue(projectId)
            val idx = cursor.getValue(projectId)
            cursor[projectId] = idx + 1

            val project = MockProjects.project(projectId) ?: continue
            val lap = idx / pool.size
            val baseTitle = pool[idx % pool.size]
            val title = if (lap == 0) baseTitle
            else "$baseTitle (${followUps[(lap - 1) % followUps.size]})"

            // Status distribution skews toward completed for shipped projects.
            val status = when {
                project.progress >= 0.99f -> TaskStatus.Completed
                else -> when (rng.nextInt(100)) {
                    in 0..29 -> TaskStatus.Todo
                    in 30..57 -> TaskStatus.InProgress
                    in 58..74 -> TaskStatus.Review
                    else -> TaskStatus.Completed
                }
            }

            val priority = when (rng.nextInt(100)) {
                in 0..11 -> Priority.Critical
                in 12..42 -> Priority.High
                in 43..79 -> Priority.Medium
                else -> Priority.Low
            }

            val members = project.memberIds
            val assigneeCount = if (rng.nextInt(10) < 3) 2 else 1
            val assignees = members.shuffled(rng).take(assigneeCount).toPersistentList()

            val subtasksTotal = rng.nextInt(0, 6)
            val subtasksDone = if (status == TaskStatus.Completed) subtasksTotal
            else rng.nextInt(0, subtasksTotal + 1)

            val checklist = checklistPool.shuffled(rng).take(subtasksTotal)
                .mapIndexed { c, text ->
                    ChecklistItem("c-$ref-$c", text, c < subtasksDone)
                }.toPersistentList()

            val due = dueLabels[rng.nextInt(dueLabels.size)]
            val overdue = status != TaskStatus.Completed && rng.nextInt(100) < 14

            out += Task(
                id = "t-$ref",
                ref = "${project.key.substringAfter('-')}-$ref",
                title = title,
                description = buildDescription(title, project.name),
                projectId = projectId,
                status = status,
                priority = priority,
                assigneeIds = assignees,
                reporterId = project.leadId,
                labels = labelPool.shuffled(rng).take(rng.nextInt(1, 4)).toPersistentList(),
                dueLabel = due,
                overdue = overdue,
                storyPoints = listOf(1, 2, 3, 5, 8, 13)[rng.nextInt(6)],
                commentCount = rng.nextInt(0, 24),
                attachmentCount = rng.nextInt(0, 5),
                subtasksDone = subtasksDone,
                subtasksTotal = subtasksTotal,
                checklist = checklist,
                createdLabel = listOf("2d ago", "5d ago", "1w ago", "2w ago", "3w ago")[rng.nextInt(5)],
                branch = branchName(project.key, ref, title),
            )
            ref++
        }
        return out
    }

    private fun buildDescription(title: String, projectName: String): String =
        "Tracked under $projectName. ${title.trimEnd('.')} — scoped during sprint planning with " +
            "acceptance criteria agreed by the owning pod. Rollout is gated behind a flag and " +
            "monitored for one full traffic cycle before the flag is removed."

    private fun branchName(key: String, ref: Int, title: String): String {
        val slug = title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .split(' ')
            .filter { it.isNotBlank() }
            .take(4)
            .joinToString("-")
        return "${key.lowercase()}/$ref-$slug"
    }

    private val byId = all.associateBy { it.id }

    fun task(id: String): Task? = byId[id]

    fun forProject(projectId: String): List<Task> = all.filter { it.projectId == projectId }

    fun byStatus(status: TaskStatus): List<Task> = all.filter { it.status == status }

    fun assignedTo(userId: String): List<Task> = all.filter { userId in it.assigneeIds }

    val completedCount = all.count { it.status == TaskStatus.Completed }
    val pendingCount = all.size - completedCount
    val overdueCount = all.count { it.overdue }
}
