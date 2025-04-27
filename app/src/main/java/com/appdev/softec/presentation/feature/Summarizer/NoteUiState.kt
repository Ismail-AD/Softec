package com.appdev.softec.presentation.feature.Summarizer

data class NoteUiState(
    val noteText: String = "",
    val summary: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)