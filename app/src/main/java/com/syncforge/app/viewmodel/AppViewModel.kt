package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.repository.SyncForgeRepository
import com.syncforge.app.ui.theme.AccentPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val darkMode: Boolean = true,
    val accent: AccentPalette = AccentPalette.Indigo,
    val pushNotifications: Boolean = true,
    val mentionsOnly: Boolean = false,
    val soundEffects: Boolean = true,
    val compactDensity: Boolean = false,
    val reduceMotion: Boolean = false,
    val language: String = "English (US)",
    val workspace: String = "Northwind Labs",
)

/** Transient one-shot message rendered by the app-level snackbar host. */
data class Toast(
    val id: Long = System.nanoTime(),
    val text: String,
    val tone: Tone = Tone.Info,
) {
    enum class Tone { Success, Error, Info }
}

/**
 * App-shell state: theme settings, session flag and the global snackbar queue.
 * Scoped to the activity so every screen shares one instance.
 */
class AppViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _signedIn = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    private val _toast = MutableStateFlow<Toast?>(null)
    val toast: StateFlow<Toast?> = _toast.asStateFlow()

    val currentUser = repo.currentUser

    // ── Settings ─────────────────────────────────────────────────────────────

    fun setDarkMode(on: Boolean) = _settings.update { it.copy(darkMode = on) }
    fun setAccent(a: AccentPalette) = _settings.update { it.copy(accent = a) }
    fun setPush(on: Boolean) = _settings.update { it.copy(pushNotifications = on) }
    fun setMentionsOnly(on: Boolean) = _settings.update { it.copy(mentionsOnly = on) }
    fun setSound(on: Boolean) = _settings.update { it.copy(soundEffects = on) }
    fun setCompact(on: Boolean) = _settings.update { it.copy(compactDensity = on) }
    fun setReduceMotion(on: Boolean) = _settings.update { it.copy(reduceMotion = on) }
    fun setLanguage(value: String) = _settings.update { it.copy(language = value) }

    // ── Session (local only — no auth service) ───────────────────────────────

    fun signIn() {
        _signedIn.value = true
        showToast("Welcome back, ${currentUser.name.substringBefore(' ')}", Toast.Tone.Success)
    }

    fun continueAsGuest() {
        _signedIn.value = true
        showToast("Exploring as guest — changes stay on this device", Toast.Tone.Info)
    }

    fun signOut() {
        _signedIn.value = false
        showToast("Signed out", Toast.Tone.Info)
    }

    // ── Snackbar ─────────────────────────────────────────────────────────────

    fun showToast(text: String, tone: Toast.Tone = Toast.Tone.Info) {
        _toast.value = Toast(text = text, tone = tone)
    }

    fun consumeToast() {
        _toast.value = null
    }

    /** Used by Settings > Export Data — writes nothing, just simulates the job. */
    fun exportData() {
        viewModelScope.launch {
            showToast("Preparing workspace export…", Toast.Tone.Info)
            repo.simulateLoad(1_400)
            showToast("Export ready — 15 projects, 100 tasks", Toast.Tone.Success)
        }
    }
}
