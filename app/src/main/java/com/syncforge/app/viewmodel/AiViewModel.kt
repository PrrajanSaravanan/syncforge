package com.syncforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncforge.app.data.mock.MockAi
import com.syncforge.app.data.model.AiMessage
import com.syncforge.app.data.repository.SyncForgeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Local assistant. Replies come from MockAi's keyword router and are revealed
 * word-by-word to imitate streaming — nothing leaves the device.
 */
class AiViewModel : ViewModel() {

    private val repo = SyncForgeRepository.get()

    private val _messages = MutableStateFlow(
        listOf(
            AiMessage(
                id = "ai-greeting",
                body = repo.aiGreeting,
                fromUser = false,
                timeLabel = "now",
            ),
        ),
    )
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _thinking = MutableStateFlow(false)
    val thinking: StateFlow<Boolean> = _thinking.asStateFlow()

    val suggestions = repo.aiSuggestions
    val quickActions = repo.aiQuickActions

    private var streamJob: Job? = null

    fun setDraft(value: String) { _draft.value = value }

    fun send(prompt: String = _draft.value) {
        val text = prompt.trim()
        if (text.isEmpty() || _thinking.value) return

        _draft.value = ""
        _messages.update {
            it + AiMessage("u-${System.nanoTime()}", text, fromUser = true, timeLabel = "now")
        }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _thinking.value = true
            delay(700)                        // "reading the workspace"
            _thinking.value = false

            val full = repo.aiReply(text)
            val id = "ai-${System.nanoTime()}"
            _messages.update {
                it + AiMessage(id, "", fromUser = false, timeLabel = "now", streaming = true)
            }

            // Reveal in word chunks so long answers land in ~1.5s.
            val words = full.split(' ')
            val builder = StringBuilder()
            words.forEachIndexed { index, word ->
                builder.append(if (index == 0) word else " $word")
                _messages.update { list ->
                    list.map { if (it.id == id) it.copy(body = builder.toString()) else it }
                }
                delay(if (word.contains('\n')) 40 else 18)
            }

            _messages.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(streaming = false, chips = MockAi.followUpChips)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun reset() {
        streamJob?.cancel()
        _thinking.value = false
        _messages.value = listOf(
            AiMessage("ai-greeting", repo.aiGreeting, fromUser = false, timeLabel = "now"),
        )
    }
}
