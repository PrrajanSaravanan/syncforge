package com.syncforge.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SyncForge palette. Dark-first, tuned for OLED: the darkest surface is #070A14
 * rather than pure black so large fills don't smear on OLED panels.
 */

// ── Neutral ramp (dark) ──────────────────────────────────────────────────────
val Ink900 = Color(0xFF070A14)
val Ink850 = Color(0xFF0A0F1C)
val Ink800 = Color(0xFF0E1424)
val Ink700 = Color(0xFF141B2E)
val Ink600 = Color(0xFF1B2438)
val Ink500 = Color(0xFF243044)
val Ink400 = Color(0xFF334155)
val Ink300 = Color(0xFF64748B)
val Ink200 = Color(0xFF94A3B8)
val Ink100 = Color(0xFFCBD5E1)
val Ink050 = Color(0xFFF1F5F9)
val Paper = Color(0xFFFAFBFF)

// ── Accent families (each drives a selectable app accent) ────────────────────
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)

val Violet400 = Color(0xFFC084FC)
val Violet500 = Color(0xFFA855F7)
val Violet600 = Color(0xFF9333EA)

val Cyan400 = Color(0xFF22D3EE)
val Cyan500 = Color(0xFF06B6D4)
val Cyan600 = Color(0xFF0891B2)

val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)

val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)
val Amber600 = Color(0xFFD97706)

val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)

val Blue400 = Color(0xFF60A5FA)
val Blue500 = Color(0xFF3B82F6)

// ── Semantic signals ─────────────────────────────────────────────────────────
val SignalSuccess = Color(0xFF22C55E)
val SignalWarning = Color(0xFFF59E0B)
val SignalDanger = Color(0xFFEF4444)
val SignalInfo = Color(0xFF3B82F6)
val SignalNeutral = Color(0xFF94A3B8)

// ── Priority ─────────────────────────────────────────────────────────────────
val PriorityCritical = Color(0xFFFF4D6D)
val PriorityHigh = Color(0xFFFF8A4C)
val PriorityMedium = Color(0xFFFBBF24)
val PriorityLow = Color(0xFF38BDF8)

// ── Chart series (colorblind-tolerant, ordered by first-use) ─────────────────
val ChartSeries = listOf(
    Color(0xFF6366F1),
    Color(0xFF22D3EE),
    Color(0xFFA855F7),
    Color(0xFF34D399),
    Color(0xFFFBBF24),
    Color(0xFFFB7185),
    Color(0xFF60A5FA),
    Color(0xFFF472B6),
)
