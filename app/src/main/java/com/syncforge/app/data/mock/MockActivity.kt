package com.syncforge.app.data.mock

import com.syncforge.app.data.model.ActivityEvent
import com.syncforge.app.data.model.ActivityKind

object MockActivity {

    /** 40 events, newest first, grouped by day for the timeline separators. */
    val all: List<ActivityEvent> = listOf(
        ActivityEvent("e-01", ActivityKind.Deployment, "u-04", "p-01", "Deployed sync-engine to production", "Rollout at 50% of ap-south-1 traffic", "11:09", "Today", meta = "v4.12.0"),
        ActivityEvent("e-02", ActivityKind.Commit, "u-00", "p-01", "Dedupe ops by vector clock before broadcast", "3 files changed · +84 −21", "11:02", "Today", meta = "a91f3c7"),
        ActivityEvent("e-03", ActivityKind.Review, "u-02", "p-01", "Approved pull request", "Looks good — flag it and let it bake", "10:58", "Today", meta = "#1284"),
        ActivityEvent("e-04", ActivityKind.PullRequest, "u-00", "p-01", "Opened pull request", "fix: collapse duplicate tombstones on rejoin", "10:41", "Today", meta = "#1284"),
        ActivityEvent("e-05", ActivityKind.TaskUpdate, "u-05", "p-03", "Moved CORE-1031 to Review", "Offline queue retry with exponential backoff", "10:33", "Today"),
        ActivityEvent("e-06", ActivityKind.Comment, "u-08", "p-03", "Commented on MOB-1014", "Can this land in the 3.0 beta?", "10:26", "Today"),
        ActivityEvent("e-07", ActivityKind.Commit, "u-05", "p-03", "Add baseline profiles for cold start", "6 files changed · +212 −8", "10:04", "Today", meta = "7d2be40"),
        ActivityEvent("e-08", ActivityKind.Release, "u-01", "p-03", "Cut release candidate", "RC 3.0.0-beta4 published to internal track", "08:50", "Today", meta = "3.0.0-beta4"),
        ActivityEvent("e-09", ActivityKind.Incident, "u-04", "p-11", "Mitigated INC-241", "Stale cache key after purge API deploy", "07:33", "Today", meta = "INC-241", succeeded = false),
        ActivityEvent("e-10", ActivityKind.Incident, "u-04", "p-11", "Opened INC-241", "Elevated 5xx on edge cache in eu-west-2", "07:02", "Today", meta = "INC-241", succeeded = false),

        ActivityEvent("e-11", ActivityKind.Commit, "u-03", "p-02", "Emit Compose tokens from Figma variables", "11 files changed · +403 −156", "18:22", "Yesterday", meta = "c40e19b"),
        ActivityEvent("e-12", ActivityKind.Deployment, "u-06", "p-06", "Deployed console to staging", "Bundle down to 174kb gzipped", "17:45", "Yesterday", meta = "v2.8.3"),
        ActivityEvent("e-13", ActivityKind.TaskUpdate, "u-06", "p-06", "Completed WEB-1022", "Virtualize the 10k-row issue table", "17:12", "Yesterday"),
        ActivityEvent("e-14", ActivityKind.PullRequest, "u-09", "p-04", "Merged pull request", "feat: rotate workload identities every 15m", "16:38", "Yesterday", meta = "#1279"),
        ActivityEvent("e-15", ActivityKind.Review, "u-00", "p-04", "Requested changes", "Bundle refresh needs a jitter window", "15:50", "Yesterday", meta = "#1279"),
        ActivityEvent("e-16", ActivityKind.Comment, "u-01", "p-01", "Commented on the sprint board", "Demo links only for review, please", "14:20", "Yesterday"),
        ActivityEvent("e-17", ActivityKind.Commit, "u-02", "p-11", "Guard against cache stampede on cold key", "2 files changed · +61 −14", "13:44", "Yesterday", meta = "5be8210"),
        ActivityEvent("e-18", ActivityKind.MemberJoin, "u-07", "p-09", "Joined AI Review Copilot", "Added by Elena Novak as Data Engineer", "11:30", "Yesterday"),
        ActivityEvent("e-19", ActivityKind.TaskUpdate, "u-09", "p-04", "Flagged SEC-1048 as blocked", "Waiting on SPIFFE bundle rollout", "10:15", "Yesterday"),
        ActivityEvent("e-20", ActivityKind.Deployment, "u-07", "p-05", "Ran warehouse backfill", "task_events reprocessed for Q2", "09:40", "Yesterday", meta = "dbt run"),

        ActivityEvent("e-21", ActivityKind.Release, "u-08", "p-10", "Shipped Onboarding Revamp", "Time-to-first-sync down from 22m to 4m", "16:00", "Jul 22", meta = "v1.0"),
        ActivityEvent("e-22", ActivityKind.Commit, "u-06", "p-06", "Command palette fuzzy ranking tweaks", "4 files changed · +97 −52", "15:22", "Jul 22", meta = "e17aa93"),
        ActivityEvent("e-23", ActivityKind.PullRequest, "u-05", "p-03", "Opened pull request", "perf: migrate task list to LazyColumn keys", "14:10", "Jul 22", meta = "#1271"),
        ActivityEvent("e-24", ActivityKind.Review, "u-03", "p-02", "Approved pull request", "Token naming matches the spec now", "12:55", "Jul 22", meta = "#1268"),
        ActivityEvent("e-25", ActivityKind.Comment, "u-04", "p-07", "Commented on OBS-1039", "Sampling noisy health checks saves ~38% ingest", "11:36", "Jul 22"),
        ActivityEvent("e-26", ActivityKind.TaskUpdate, "u-07", "p-05", "Completed DATA-1026", "Incremental model for sprint_velocity", "10:48", "Jul 22"),
        ActivityEvent("e-27", ActivityKind.Commit, "u-04", "p-07", "Correlate logs to trace ids", "8 files changed · +176 −40", "09:52", "Jul 22", meta = "b3f7c81"),
        ActivityEvent("e-28", ActivityKind.MemberJoin, "u-05", "p-14", "Joined Voice Huddles", "Assigned as project lead", "09:05", "Jul 22"),

        ActivityEvent("e-29", ActivityKind.Deployment, "u-02", "p-01", "Deployed sync-engine to staging", "Canary held for 45 minutes, no regressions", "17:30", "Jul 21", meta = "v4.11.2"),
        ActivityEvent("e-30", ActivityKind.Commit, "u-00", "p-01", "Backpressure op-log when replica lag exceeds 2s", "5 files changed · +143 −33", "16:47", "Jul 21", meta = "9c02de5"),
        ActivityEvent("e-31", ActivityKind.PullRequest, "u-06", "p-09", "Merged pull request", "feat: repo-aware context window assembly", "15:19", "Jul 21", meta = "#1262"),
        ActivityEvent("e-32", ActivityKind.Review, "u-09", "p-09", "Approved pull request", "Secret redaction path verified", "14:40", "Jul 21", meta = "#1262"),
        ActivityEvent("e-33", ActivityKind.Incident, "u-02", "p-01", "Resolved INC-238", "Op-log lag during nightly compaction", "12:11", "Jul 21", meta = "INC-238", succeeded = false),
        ActivityEvent("e-34", ActivityKind.TaskUpdate, "u-03", "p-02", "Moved DS-1009 to Completed", "Dark-mode contrast audit script", "11:02", "Jul 21"),
        ActivityEvent("e-35", ActivityKind.Comment, "u-08", "p-15", "Commented on DEV-1052", "Playground should default to the Kotlin SDK", "10:20", "Jul 21"),

        ActivityEvent("e-36", ActivityKind.Commit, "u-09", "p-04", "Enforce mTLS on internal admin mesh", "14 files changed · +389 −112", "18:05", "Jul 20", meta = "d81b6f4"),
        ActivityEvent("e-37", ActivityKind.Deployment, "u-06", "p-06", "Deployed console to production", "LCP improved 2.1s → 0.7s", "16:22", "Jul 20", meta = "v2.8.0"),
        ActivityEvent("e-38", ActivityKind.Release, "u-01", "p-06", "Published release notes", "Web Console 2.8 — command palette GA", "16:30", "Jul 20", meta = "v2.8.0"),
        ActivityEvent("e-39", ActivityKind.MemberJoin, "u-09", "p-12", "Joined Compliance SOC 2", "Assigned as project lead", "10:45", "Jul 20"),
        ActivityEvent("e-40", ActivityKind.TaskUpdate, "u-02", "p-08", "Created BILL-1055", "Idempotent replay for metering events", "09:30", "Jul 20"),
    )

    val dayGroups: List<String> = all.map { it.dayGroup }.distinct()

    fun forProject(projectId: String): List<ActivityEvent> = all.filter { it.projectId == projectId }

    fun forActor(userId: String): List<ActivityEvent> = all.filter { it.actorId == userId }
}
