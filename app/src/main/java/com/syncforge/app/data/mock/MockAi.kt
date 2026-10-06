package com.syncforge.app.data.mock

import kotlinx.collections.immutable.persistentListOf

object MockAi {

    val suggestedPrompts = listOf(
        "Summarize what shipped this week",
        "Which projects are at risk?",
        "Who is overloaded right now?",
        "Draft the Sprint 24 review notes",
        "Explain INC-241 in plain language",
        "What should I pick up next?",
    )

    val quickActions = listOf(
        "Standup summary" to "Generate my standup update from yesterday's activity",
        "Sprint risks" to "List the top three risks to Sprint 24 with evidence",
        "Review queue" to "Triage my open review requests by blast radius",
        "Release notes" to "Draft release notes for 3.0.0-beta4",
    )

    /**
     * Keyword-routed canned replies. Every response is local — no model call is made.
     * Matched on the first keyword found, falling back to a generic workspace summary.
     */
    private val routes: List<Pair<List<String>, String>> = listOf(
        listOf("ship", "week", "summar") to
            "**This week across the workspace**\n\n" +
            "• sync-engine v4.12.0 reached production at 50% of ap-south-1 traffic, with p99 convergence " +
            "down from 118ms to 74ms after the vector-clock dedupe patch.\n" +
            "• Web Console 2.8 shipped the command palette to GA and cut LCP from 2.1s to 0.7s.\n" +
            "• Forge Design System now emits Compose and CSS tokens from a single Figma publish.\n" +
            "• INC-241 (edge cache, eu-west-2) was mitigated in 31 minutes; postmortem is Thursday.\n\n" +
            "47 of 52 committed story points closed. Five points carry into Sprint 25.",

        listOf("risk", "at risk", "slip", "miss") to
            "**Three projects need attention**\n\n" +
            "**1. Zero-Trust Rollout — health 58**\n" +
            "Velocity fell to 28 points with 23 open issues and 81% of budget consumed. At the current " +
            "burn rate Aug 28 slips by roughly 9 days. Moving the two SPIFFE bundle tasks to Sprint 25 " +
            "recovers the date.\n\n" +
            "**2. Voice Huddles — health 61**\n" +
            "34% complete against an Oct 21 target with 19 open issues. The jitter-buffer and echo-" +
            "cancellation work has no second owner.\n\n" +
            "**3. Edge Cache Layer — capacity**\n" +
            "eu-west-2 is at 88% load and 187ms p99 following INC-241 — the thinnest headroom of any " +
            "region going into the 3.0 rollout.",

        listOf("overload", "capacity", "who is", "workload", "busy") to
            "**Load distribution looks uneven**\n\n" +
            "• **Daniel Okafor** — 11 open tasks across Mobile 3.0 and Voice Huddles, both of which have " +
            "beta dates in the next six weeks. This is the clearest overload signal.\n" +
            "• **Marcus Bell** — 9 open tasks spanning four projects; context-switching cost is high.\n" +
            "• **Kwame Asante** — 2 open tasks and 71% relative throughput, so there is headroom here.\n\n" +
            "Reassigning one Voice Huddles task from Daniel to Kwame would even out the next sprint " +
            "without touching either deadline.",

        listOf("standup", "update", "yesterday") to
            "**Your standup update**\n\n" +
            "*Yesterday:* Landed the vector-clock dedupe patch on sync-engine (#1284, approved by Marcus), " +
            "added the drop-count counter Rafael asked for, and drove the 25% → 50% rollout in ap-south-1.\n\n" +
            "*Today:* Draft the rollout note, watch the burn-rate alert through the traffic cycle, and " +
            "prepare the six-minute sprint review demo.\n\n" +
            "*Blockers:* None. Clock-skew task (CORE-1044) is queued behind the rollout.",

        listOf("next", "pick up", "should i") to
            "**Suggested next task: CORE-1044**\n" +
            "*Handle clock skew between edge and origin* — Critical, 8 points, assigned to you by Priya.\n\n" +
            "It is the last correctness gap before the sync-engine rollout goes to 100%, and it shares " +
            "context with the dedupe work you just finished, so the ramp-up cost is near zero.\n\n" +
            "After that, MOB-1067 (predictive back gesture) is the highest-value item in your queue that " +
            "is not blocked.",

        listOf("inc-241", "incident", "outage", "5xx") to
            "**INC-241 in plain language**\n\n" +
            "At 07:02 the edge cache in London started returning server errors for about 4% of requests. " +
            "Sofia drained traffic away from that one location, which brought errors back under 0.1% " +
            "within 17 minutes. Full mitigation was called at 07:33.\n\n" +
            "The cause was a stale cache key left behind by the purge-API deploy: the cache kept serving " +
            "a key the new code no longer knew how to refresh.\n\n" +
            "*Impact:* one region, 31 minutes, no data loss. Rafael owns the postmortem on Thursday.",

        listOf("release", "notes", "beta") to
            "**Release notes — 3.0.0-beta4**\n\n" +
            "**Performance**\n" +
            "• Cold start reduced to 640ms on mid-tier hardware via baseline profiles\n" +
            "• Task list migrated to keyed lazy layout, removing scroll jank on long boards\n\n" +
            "**Features**\n" +
            "• Offline queue retries with exponential backoff\n" +
            "• Pull-to-refresh on the activity feed\n\n" +
            "**Fixes**\n" +
            "• Shared-element transition no longer flickers when opening a project\n" +
            "• Kanban state survives process death\n\n" +
            "Smoke tests: 42 scenarios green on Android, console and warehouse unaffected.",

        listOf("review", "queue", "triage", "pull request") to
            "**Your review queue, ordered by blast radius**\n\n" +
            "**1. #1279 — rotate workload identities every 15m** (Zero-Trust)\n" +
            "Touches auth for every internal service. You already requested a jitter window; the update " +
            "is waiting on you.\n\n" +
            "**2. #1271 — migrate task list to LazyColumn keys** (Mobile 3.0)\n" +
            "Ships in beta4, so review latency here directly delays the release train.\n\n" +
            "**3. #1268 — token naming** (Design System)\n" +
            "Low risk, already approved by Yuki. Safe to rubber-stamp.",
    )

    fun reply(prompt: String): String {
        val q = prompt.lowercase()
        routes.firstOrNull { (keys, _) -> keys.any { it in q } }?.let { return it.second }
        return "**Workspace snapshot**\n\n" +
            "15 projects are in flight with 100 tracked tasks — 38 completed, 27 in progress, 16 in " +
            "review and 19 still in the backlog. Sprint 24 closed 47 of 52 committed points.\n\n" +
            "Two projects are flagged at risk (Zero-Trust Rollout, Voice Huddles) and one region " +
            "(eu-west-2) is running hot after INC-241.\n\n" +
            "Ask me about risks, workload, the review queue, or what to pick up next."
    }

    val followUpChips = persistentListOf(
        "Show the evidence",
        "Draft a message to the team",
        "What changed since last week?",
    )

    val greeting = "I'm Forge AI. I can see your 15 projects, 100 tasks, sprint history and activity " +
        "feed. Ask me anything about the workspace — or start with one of these."
}
