package com.appdev.softec.presentation.feature.Summarizer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    fun updateNoteText(text: String) {
        _uiState.update { it.copy(noteText = text) }
    }

    fun processVoiceInput(recognizedText: String) {
        _uiState.update { it.copy(noteText = recognizedText) }
    }

    fun generateText() {
        if (uiState.value.noteText.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val summarizedText = generateSummary(uiState.value.noteText)
                _uiState.update { it.copy(isLoading = false, summary = summarizedText) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
                Log.e("CGAZ", "Error generating summary: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun generateSummary(text: String): List<String> {
        val prompt = "Summarize the following text into bullet points. Return each bullet point as a separate line starting with a dash.\n\n$text"

        try {
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""

            // Parse the response as a list of bullet points
            return response.lines()
                .filter { it.trim().startsWith("-") }
                .map { it.trim().removePrefix("-").trim() }
                .filter { it.isNotEmpty() }
                .ifEmpty { listOf("No summary points generated") }
        } catch (e: Exception) {
            Log.e("CGAZ", "Error in generateSummary: ${e.localizedMessage}")
            return listOf("Error generating summary: ${e.localizedMessage}")
        }
    }
}
