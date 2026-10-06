package com.syncforge.app.data.mock

import com.syncforge.app.data.model.AppNotification
import com.syncforge.app.data.model.NotificationKind

object MockNotifications {

    /** 25 notifications, newest first. */
    val all: List<AppNotification> = listOf(
        AppNotification("n-01", NotificationKind.Mention, "Priya mentioned you in #eng-platform", "Can you take the sprint review demo for the sync engine work?", "2m", false, "u-01", "p-01", "Reply"),
        AppNotification("n-02", NotificationKind.Review, "Marcus approved your pull request", "#1284 · fix: collapse duplicate tombstones on rejoin", "18m", false, "u-02", "p-01", "View PR"),
        AppNotification("n-03", NotificationKind.Deployment, "sync-engine v4.12.0 reached production", "Rollout at 50% of ap-south-1 traffic · p99 74ms", "34m", false, "u-04", "p-01", "Open run"),
        AppNotification("n-04", NotificationKind.Incident, "INC-241 was mitigated", "Elevated 5xx on edge cache in eu-west-2 · 31 min to mitigate", "1h", false, "u-04", "p-11", "Postmortem"),
        AppNotification("n-05", NotificationKind.Assignment, "Daniel assigned you MOB-1067", "Predictive back gesture on detail screens", "1h", false, "u-05", "p-03", "Open task"),
        AppNotification("n-06", NotificationKind.Review, "Review requested on #1279", "feat: rotate workload identities every 15m", "2h", true, "u-09", "p-04", "Review"),
        AppNotification("n-07", NotificationKind.Mention, "Yuki mentioned you in #design-system", "Motion tokens are ready — card spring is damping 20 / stiffness 90", "3h", true, "u-03", "p-02", "Reply"),
        AppNotification("n-08", NotificationKind.System, "Sprint 24 review starts in 4 hours", "16:00 GMT+5:30 · Bring demo links, not slides", "3h", true, null, null, "Add to calendar"),
        AppNotification("n-09", NotificationKind.Deployment, "Console v2.8.3 deployed to staging", "Bundle down to 174kb gzipped", "5h", true, "u-06", "p-06", "Open run"),
        AppNotification("n-10", NotificationKind.Assignment, "You were added to Voice Huddles", "Daniel Okafor added you as a reviewer", "6h", true, "u-05", "p-14", "Open project"),
        AppNotification("n-11", NotificationKind.Mention, "Sofia mentioned you in #incidents", "Traces show fan-out doubling for ~90 seconds", "8h", true, "u-04", "p-01", "Reply"),
        AppNotification("n-12", NotificationKind.Review, "Your review was requested on #1271", "perf: migrate task list to LazyColumn keys", "yesterday", true, "u-05", "p-03", "Review"),
        AppNotification("n-13", NotificationKind.System, "Zero-Trust Rollout health dropped to 58", "23 open issues · budget at 81%", "yesterday", false, null, "p-04", "Open project"),
        AppNotification("n-14", NotificationKind.Assignment, "Priya assigned you CORE-1044", "Handle clock skew between edge and origin", "yesterday", true, "u-01", "p-01", "Open task"),
        AppNotification("n-15", NotificationKind.Deployment, "Warehouse backfill completed", "task_events reprocessed for Q2 · 41 min runtime", "yesterday", true, "u-07", "p-05", "Open run"),
        AppNotification("n-16", NotificationKind.Mention, "Hannah mentioned you in #mobile-android", "Can this land in the 3.0 beta?", "yesterday", true, "u-08", "p-03", "Reply"),
        AppNotification("n-17", NotificationKind.Review, "Elena requested changes on #1276", "Context assembly needs a token budget guard", "2d", true, "u-06", "p-09", "View PR"),
        AppNotification("n-18", NotificationKind.System, "Weekly digest is ready", "You closed 11 tasks and reviewed 14 pull requests", "2d", true, null, null, "Read digest"),
        AppNotification("n-19", NotificationKind.Incident, "INC-238 was resolved", "Op-log lag during nightly compaction", "3d", true, "u-02", "p-01", "Postmortem"),
        AppNotification("n-20", NotificationKind.Deployment, "Console v2.8.0 reached production", "LCP improved 2.1s → 0.7s", "4d", true, "u-06", "p-06", "Open run"),
        AppNotification("n-21", NotificationKind.Assignment, "You are on-call from Monday", "Primary rotation · platform pod", "4d", true, "u-01", null, "View rotation"),
        AppNotification("n-22", NotificationKind.Mention, "Rafael mentioned you in #security-wg", "Make sure the dedupe path still logs the drop count", "5d", true, "u-09", "p-04", "Reply"),
        AppNotification("n-23", NotificationKind.System, "Onboarding Revamp shipped", "Time-to-first-sync down from 22m to 4m", "5d", true, "u-08", "p-10", "Open project"),
        AppNotification("n-24", NotificationKind.Review, "Kwame approved your pull request", "#1258 · feat: hybrid scoring for search", "6d", true, "u-07", "p-13", "View PR"),
        AppNotification("n-25", NotificationKind.System, "Achievement unlocked: Ship It", "50 deployments to production without a rollback", "1w", true, null, null, "View profile"),
    )

    val unreadCount: Int = all.count { !it.read }
}
