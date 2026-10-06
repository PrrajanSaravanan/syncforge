package com.syncforge.app.data.mock

import com.syncforge.app.data.model.Attachment
import com.syncforge.app.data.model.Channel
import com.syncforge.app.data.model.Message
import com.syncforge.app.data.model.MessageKind
import com.syncforge.app.data.model.Reaction
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

object MockChat {

    val channels: List<Channel> = listOf(
        Channel("c-01", "eng-platform", "Sync engine, edge cache and the op-log", false, 3, 24, "2m", muted = false),
        Channel("c-02", "design-system", "Forge tokens, motion specs and component reviews", false, 0, 12, "18m"),
        Channel("c-03", "mobile-android", "Compose rewrite, releases and crash triage", false, 7, 9, "just now"),
        Channel("c-04", "incidents", "Paging channel — keep it signal only", false, 1, 31, "1h"),
        Channel("c-05", "release-train", "Cut branches, RC builds and rollout gates", false, 0, 18, "3h"),
        Channel("c-06", "random", "Non-work things worth sharing", false, 0, 42, "5h"),
        Channel("c-07", "security-wg", "Zero-trust working group", true, 2, 7, "42m"),
        Channel("d-01", "Priya Raghunathan", "Engineering Manager", true, 2, 2, "8m", isDirect = true, peerId = "u-01"),
        Channel("d-02", "Yuki Tanaka", "Design Systems Lead", true, 0, 2, "1h", isDirect = true, peerId = "u-03"),
        Channel("d-03", "Marcus Bell", "Senior Backend Engineer", true, 0, 2, "yesterday", isDirect = true, peerId = "u-02"),
    )

    private fun r(vararg specs: Triple<String, Int, Boolean>) =
        specs.map { (emoji, count, mine) -> Reaction(emoji, count, mine) }.toPersistentList()

    /** 50 messages across the primary channels, ordered oldest → newest. */
    val messages: List<Message> = listOf(
        // ── #eng-platform, Yesterday ──────────────────────────────────────────
        Message("m-01", "c-01", "u-02", "Morning — op-log lag in ap-south-1 is sitting at 1.8s again after the 04:00 batch.", "09:02", "Yesterday", MessageKind.Text),
        Message("m-02", "c-01", "u-00", "That lines up with the compaction window. I think we're re-broadcasting tombstones we already acked.", "09:04", "Yesterday", MessageKind.Text),
        Message("m-03", "c-01", "u-04", "Confirmed from the traces — fan-out doubles for ~90 seconds.", "09:07", "Yesterday", MessageKind.Text,
            reactions = r(Triple("👀", 3, false))),
        Message("m-04", "c-01", "u-04", "trace-ap-south-lag.png", "09:08", "Yesterday", MessageKind.Image,
            attachment = Attachment("a-01", "trace-ap-south-lag.png", "1.4 MB", "image")),
        Message("m-05", "c-01", "u-00", "Nice capture. Patch below — dedupe by vector clock before the broadcast hop.", "09:15", "Yesterday", MessageKind.Text),
        Message("m-06", "c-01", "u-00", "if (clock.dominates(peer.lastAck)) {\n    return@forEach   // already delivered\n}\nbroadcast(op)", "09:15", "Yesterday", MessageKind.Code,
            codeLang = "kotlin", reactions = r(Triple("🔥", 5, true), Triple("✅", 2, false))),
        Message("m-07", "c-01", "u-02", "Ship it behind the flag and let it bake for a cycle.", "09:21", "Yesterday", MessageKind.Text, threadReplies = 4),
        Message("m-08", "c-01", "u-09", "One ask: make sure the dedupe path still logs the drop count, otherwise we lose the audit trail.", "09:26", "Yesterday", MessageKind.Text),
        Message("m-09", "c-01", "u-00", "Good call — counter added.", "09:31", "Yesterday", MessageKind.Text, edited = true),

        // ── #eng-platform, Today ──────────────────────────────────────────────
        Message("m-10", "c-01", "u-01", "Reminder: sprint 24 review is at 16:00 GMT+5:30. Bring your demo links, not slides.", "08:40", "Today", MessageKind.Text,
            pinned = true, reactions = r(Triple("👍", 8, true))),
        Message("m-11", "c-01", "u-04", "Flag rolled to 25% of ap-south traffic. p99 convergence down to 74ms.", "10:12", "Today", MessageKind.Text,
            reactions = r(Triple("🚀", 6, true), Triple("🎉", 3, false))),
        Message("m-12", "c-01", "u-02", "convergence-after-patch.png", "10:13", "Today", MessageKind.Image,
            attachment = Attachment("a-02", "convergence-after-patch.png", "892 KB", "image")),
        Message("m-13", "c-01", "u-00", "That's the target hit with headroom. I'll draft the rollout note.", "10:18", "Today", MessageKind.Text),
        Message("m-14", "c-01", "u-09", "", "10:25", "Today", MessageKind.Voice, voiceSeconds = 47,
            voiceWaveform = persistentListOf(0.3f, 0.6f, 0.9f, 0.5f, 0.7f, 1f, 0.4f, 0.8f, 0.6f, 0.35f, 0.75f, 0.5f, 0.9f, 0.45f, 0.65f, 0.3f, 0.55f, 0.85f, 0.4f, 0.7f, 0.5f, 0.95f, 0.6f, 0.3f)),
        Message("m-15", "c-01", "u-04", "rollout-checklist.pdf", "10:44", "Today", MessageKind.File,
            attachment = Attachment("a-03", "rollout-checklist.pdf", "204 KB", "pdf")),
        Message("m-16", "c-01", "u-02", "Approved from my side. Let's go to 50% after lunch.", "11:02", "Today", MessageKind.Text,
            reactions = r(Triple("✅", 4, true))),
        Message("m-17", "c-01", "u-00", "Done. Watching the burn-rate alert for the next hour.", "11:09", "Today", MessageKind.Text),

        // ── #mobile-android ───────────────────────────────────────────────────
        Message("m-18", "c-03", "u-05", "Cold start on the Pixel 6a is down to 640ms with baseline profiles in place.", "09:20", "Today", MessageKind.Text,
            reactions = r(Triple("🔥", 7, true))),
        Message("m-19", "c-03", "u-03", "Beautiful. Does that include the splash animation, or is that measured after first frame?", "09:22", "Today", MessageKind.Text),
        Message("m-20", "c-03", "u-05", "After first frame. Full time-to-interactive is 910ms.", "09:24", "Today", MessageKind.Text),
        Message("m-21", "c-03", "u-00", "Worth adding a macrobenchmark so we notice when it regresses.", "09:31", "Today", MessageKind.Text, threadReplies = 2),
        Message("m-22", "c-03", "u-05", "Agreed, filing it now.", "09:33", "Today", MessageKind.Text),
        Message("m-23", "c-03", "u-03", "kanban-motion-spec.fig", "10:02", "Today", MessageKind.File,
            attachment = Attachment("a-04", "kanban-motion-spec.fig", "3.8 MB", "figma"),
            reactions = r(Triple("😍", 5, true), Triple("👏", 2, false))),
        Message("m-24", "c-03", "u-05", "The card lift spring in there feels much better than what we shipped.", "10:11", "Today", MessageKind.Text),
        Message("m-25", "c-03", "u-08", "Can we get this into the 3.0 beta or is it post-launch?", "10:26", "Today", MessageKind.Text),
        Message("m-26", "c-03", "u-05", "It's a motion-spec change only, so beta is realistic.", "10:29", "Today", MessageKind.Text),
        Message("m-27", "c-03", "u-03", "", "10:35", "Today", MessageKind.Voice, voiceSeconds = 28,
            voiceWaveform = persistentListOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.8f, 0.55f, 0.7f, 0.45f, 0.85f, 0.5f, 0.65f, 0.9f, 0.35f, 0.6f, 0.75f, 0.4f)),
        Message("m-28", "c-03", "u-05", "Perfect, I'll pick it up after standup.", "10:41", "Today", MessageKind.Text),

        // ── #design-system ────────────────────────────────────────────────────
        Message("m-29", "c-02", "u-03", "Token pipeline now emits Compose and CSS from one Figma publish. No more manual sync.", "08:15", "Today", MessageKind.Text,
            pinned = true, reactions = r(Triple("🎉", 9, true), Triple("🙌", 4, false))),
        Message("m-30", "c-02", "u-06", "Does it handle the semantic layer, or just primitives?", "08:19", "Today", MessageKind.Text),
        Message("m-31", "c-02", "u-03", "Both — primitives resolve into semantic aliases at build time.", "08:22", "Today", MessageKind.Text),
        Message("m-32", "c-02", "u-06", "forge-tokens-v4.zip", "08:40", "Today", MessageKind.File,
            attachment = Attachment("a-05", "forge-tokens-v4.zip", "1.1 MB", "zip")),
        Message("m-33", "c-02", "u-03", "Contrast audit passes AA everywhere except the amber-on-surface pairing. Fixing the ramp.", "09:05", "Today", MessageKind.Text),
        Message("m-34", "c-02", "u-05", "That one bit us on the priority chips too.", "09:12", "Today", MessageKind.Text),
        Message("m-35", "c-02", "u-03", "Bumped amber to 500 on dark. Chips are readable now.", "09:48", "Today", MessageKind.Text,
            reactions = r(Triple("✅", 3, true))),

        // ── #incidents ────────────────────────────────────────────────────────
        Message("m-36", "c-04", "u-04", "INC-241 opened: elevated 5xx on the edge cache in eu-west-2.", "07:02", "Today", MessageKind.System),
        Message("m-37", "c-04", "u-04", "Error rate 4.2%, isolated to one PoP. Draining traffic now.", "07:04", "Today", MessageKind.Text),
        Message("m-38", "c-04", "u-02", "Drain confirmed. Rate back under 0.1%.", "07:19", "Today", MessageKind.Text,
            reactions = r(Triple("🙏", 5, false))),
        Message("m-39", "c-04", "u-04", "INC-241 mitigated. Root cause looks like a stale cache key after the purge API deploy.", "07:33", "Today", MessageKind.Text),
        Message("m-40", "c-04", "u-09", "Postmortem scheduled for Thursday. I'll own the write-up.", "08:01", "Today", MessageKind.Text),
        Message("m-41", "c-04", "u-04", "inc-241-timeline.log", "08:14", "Today", MessageKind.File,
            attachment = Attachment("a-06", "inc-241-timeline.log", "58 KB", "log")),

        // ── #release-train ────────────────────────────────────────────────────
        Message("m-42", "c-05", "u-01", "RC-3.0.0-beta4 is cut. Please smoke-test your surface before 14:00.", "08:50", "Today", MessageKind.Text, pinned = true),
        Message("m-43", "c-05", "u-05", "Android smoke test green — 42 scenarios, no regressions.", "11:20", "Today", MessageKind.Text,
            reactions = r(Triple("✅", 6, true))),
        Message("m-44", "c-05", "u-06", "Console green as well.", "11:26", "Today", MessageKind.Text),
        Message("m-45", "c-05", "u-07", "Warehouse jobs unaffected by the release.", "11:40", "Today", MessageKind.Text),

        // ── DMs ───────────────────────────────────────────────────────────────
        Message("m-46", "d-01", "u-01", "Can you take the sprint review demo for the sync engine work?", "08:44", "Today", MessageKind.Text),
        Message("m-47", "d-01", "u-00", "Yes — I'll show the convergence numbers before and after the dedupe patch.", "08:46", "Today", MessageKind.Text),
        Message("m-48", "d-01", "u-01", "Perfect. Keep it to six minutes, we have four demos.", "08:47", "Today", MessageKind.Text,
            reactions = r(Triple("👍", 1, true))),
        Message("m-49", "d-02", "u-03", "Sent you the updated motion tokens — the card spring is damping 20 / stiffness 90 now.", "10:04", "Today", MessageKind.Text),
        Message("m-50", "d-02", "u-00", "Looks right. I'll wire it into the kanban lift.", "10:09", "Today", MessageKind.Text),
    )

    fun forChannel(channelId: String): List<Message> = messages.filter { it.channelId == channelId }

    fun pinned(channelId: String): List<Message> = forChannel(channelId).filter { it.pinned }

    fun channel(id: String): Channel? = channels.firstOrNull { it.id == id }

    val totalUnread: Int = channels.sumOf { it.unread }
}
