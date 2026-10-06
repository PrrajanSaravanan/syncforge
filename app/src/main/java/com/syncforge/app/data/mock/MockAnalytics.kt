package com.syncforge.app.data.mock

import com.syncforge.app.data.model.Achievement
import com.syncforge.app.data.model.AiInsight
import com.syncforge.app.data.model.Analytics
import com.syncforge.app.data.model.ContributionDay
import com.syncforge.app.data.model.LineSeries
import com.syncforge.app.data.model.NodeStatus
import com.syncforge.app.data.model.SeriesPoint
import com.syncforge.app.data.model.SprintReport
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

object MockAnalytics {

    val analytics = Analytics(
        weeklyProductivity = persistentListOf(
            SeriesPoint("Mon", 14f),
            SeriesPoint("Tue", 22f),
            SeriesPoint("Wed", 19f),
            SeriesPoint("Thu", 27f),
            SeriesPoint("Fri", 31f),
            SeriesPoint("Sat", 9f),
            SeriesPoint("Sun", 5f),
        ),
        sprintVelocity = persistentListOf(
            SeriesPoint("S18", 32f),
            SeriesPoint("S19", 38f),
            SeriesPoint("S20", 35f),
            SeriesPoint("S21", 44f),
            SeriesPoint("S22", 41f),
            SeriesPoint("S23", 49f),
            SeriesPoint("S24", 47f),
        ),
        taskDistribution = persistentListOf(
            SeriesPoint("Completed", 38f),
            SeriesPoint("In Progress", 27f),
            SeriesPoint("Review", 16f),
            SeriesPoint("Todo", 19f),
        ),
        burndown = persistentListOf(
            LineSeries(
                name = "Ideal",
                points = persistentListOf(
                    SeriesPoint("D1", 47f), SeriesPoint("D3", 40f), SeriesPoint("D5", 33f),
                    SeriesPoint("D7", 26f), SeriesPoint("D9", 19f), SeriesPoint("D11", 12f),
                    SeriesPoint("D13", 5f), SeriesPoint("D14", 0f),
                ),
                colorIndex = 6,
            ),
            LineSeries(
                name = "Actual",
                points = persistentListOf(
                    SeriesPoint("D1", 47f), SeriesPoint("D3", 44f), SeriesPoint("D5", 38f),
                    SeriesPoint("D7", 30f), SeriesPoint("D9", 24f), SeriesPoint("D11", 13f),
                    SeriesPoint("D13", 7f), SeriesPoint("D14", 3f),
                ),
                colorIndex = 0,
            ),
        ),
        deployFrequency = persistentListOf(
            SeriesPoint("W1", 12f), SeriesPoint("W2", 17f), SeriesPoint("W3", 15f),
            SeriesPoint("W4", 23f), SeriesPoint("W5", 26f), SeriesPoint("W6", 21f),
        ),
        cycleTimeDays = 2.4f,
        reviewTimeHours = 5.8f,
        mergeRate = 0.93f,
        sprintReports = persistentListOf(
            SprintReport("Sprint 24", committed = 52, completed = 47, carriedOver = 5, dateRange = "Jul 15 – Jul 28"),
            SprintReport("Sprint 23", committed = 51, completed = 49, carriedOver = 2, dateRange = "Jul 01 – Jul 14"),
            SprintReport("Sprint 22", committed = 46, completed = 41, carriedOver = 5, dateRange = "Jun 17 – Jun 30"),
            SprintReport("Sprint 21", committed = 48, completed = 44, carriedOver = 4, dateRange = "Jun 03 – Jun 16"),
            SprintReport("Sprint 20", committed = 40, completed = 35, carriedOver = 5, dateRange = "May 20 – Jun 02"),
        ),
        languageMix = persistentListOf(
            SeriesPoint("Kotlin", 34f),
            SeriesPoint("Rust", 24f),
            SeriesPoint("TypeScript", 21f),
            SeriesPoint("Go", 12f),
            SeriesPoint("Python", 9f),
        ),
    )

    val nodes: List<NodeStatus> = listOf(
        NodeStatus("edge-ap-south-1", "Mumbai", "Healthy", 41, 0.62f),
        NodeStatus("edge-us-east-1", "Virginia", "Healthy", 28, 0.55f),
        NodeStatus("edge-eu-west-2", "London", "Degraded", 187, 0.88f),
        NodeStatus("edge-ap-northeast-1", "Tokyo", "Healthy", 52, 0.47f),
        NodeStatus("edge-sa-east-1", "São Paulo", "Healthy", 76, 0.39f),
        NodeStatus("relay-global", "Anycast", "Healthy", 19, 0.71f),
    )

    val insights: List<AiInsight> = listOf(
        AiInsight(
            "ai-01",
            "Zero-Trust Rollout will miss Aug 28",
            "Velocity dropped to 28 points while 23 issues remain open. At the current burn rate the " +
                "sprint lands 9 days late — moving two SPIFFE tasks to Sprint 25 recovers the date.",
            confidence = 87, tag = "Risk",
        ),
        AiInsight(
            "ai-02",
            "Review latency is your bottleneck",
            "Median review wait is 5.8h against a 2.1h cycle time. Two reviewers hold 61% of open " +
                "requests — spreading review load would cut lead time by roughly a third.",
            confidence = 79, tag = "Throughput",
        ),
        AiInsight(
            "ai-03",
            "Edge cache needs attention before the next release",
            "eu-west-2 is at 88% load with 187ms p99 after INC-241. Capacity headroom is the lowest " +
                "of any region heading into the 3.0 rollout.",
            confidence = 92, tag = "Reliability",
        ),
    )

    val achievements: List<Achievement> = listOf(
        Achievement("ach-01", "Ship It", "50 production deploys without a rollback", true, "Gold"),
        Achievement("ach-02", "Reviewer of the Quarter", "300+ pull request reviews", true, "Platinum"),
        Achievement("ach-03", "Streak Keeper", "90 consecutive days with a contribution", true, "Gold"),
        Achievement("ach-04", "Incident Commander", "Led 10 incident mitigations", true, "Silver"),
        Achievement("ach-05", "Test Guardian", "Raised coverage above 85% on a core service", true, "Silver"),
        Achievement("ach-06", "Mentor", "Onboarded 5 engineers to the platform pod", true, "Bronze"),
        Achievement("ach-07", "Perf Hunter", "Cut a p99 latency in half", false, "Gold"),
        Achievement("ach-08", "Zero Bug Sprint", "Close a sprint with no open defects", false, "Platinum"),
    )

    /** 53 weeks × 7 days GitHub-style contribution grid, fixed seed. */
    val contributions: List<ContributionDay> = run {
        val rng = Random(4711)
        buildList {
            for (week in 0 until 53) {
                for (day in 0 until 7) {
                    // Weekends and older weeks are quieter; recent weekdays are dense.
                    val weekend = day == 0 || day == 6
                    val recency = week / 52f
                    val roll = rng.nextFloat() + recency * 0.35f - if (weekend) 0.45f else 0f
                    val level = when {
                        roll < 0.30f -> 0
                        roll < 0.55f -> 1
                        roll < 0.78f -> 2
                        roll < 0.95f -> 3
                        else -> 4
                    }
                    add(ContributionDay(week, day, level))
                }
            }
        }
    }

    val contributionTotal: Int = contributions.sumOf { it.level * 2 }

    /** Per-project progress rings shown on the dashboard workspace-health card. */
    val workspaceHealth: List<SeriesPoint> = MockProjects.all
        .sortedByDescending { it.healthScore }
        .take(5)
        .map { SeriesPoint(it.key, it.healthScore.toFloat()) }
        .toPersistentList()
}
