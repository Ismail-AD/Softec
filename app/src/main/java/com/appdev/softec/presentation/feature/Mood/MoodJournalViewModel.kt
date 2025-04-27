package com.appdev.softec.presentation.feature.Mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.domain.repository.MoodRepository
import com.appdev.softec.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MoodJournalViewModel @Inject constructor(
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodJournalUiState())
    val uiState: StateFlow<MoodJournalUiState> = _uiState.asStateFlow()

    init {
        loadMoodEntries()
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = uiState.value.selectedDate

        // Move the date to the previous month
        calendar.add(Calendar.MONTH, -1)

        // Update the selected date
        _uiState.update { it.copy(selectedDate = calendar.timeInMillis) }
    }

    // Function to navigate to the next month
    fun nextMonth() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = uiState.value.selectedDate

        // Move the date to the next month
        calendar.add(Calendar.MONTH, 1)

        // Update the selected date
        _uiState.update { it.copy(selectedDate = calendar.timeInMillis) }
    }

    fun selectMood(mood: MoodType) {
        _uiState.update { it.copy(currentMood = mood) }
    }

    fun updateMoodNote(note: String) {
        _uiState.update { it.copy(moodNote = note) }
    }

    fun updateSelectedDate(timestamp: Long) {
        _uiState.update { it.copy(selectedDate = timestamp) }
    }

    fun toggleCalendarVisibility() {
        _uiState.update { it.copy(isCalendarVisible = !it.isCalendarVisible) }
    }

    fun toggleAnalysisVisibility() {
        _uiState.update { it.copy(isAnalysisVisible = !it.isAnalysisVisible) }
    }

    fun updateSelectedPeriod(period: AnalysisPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun saveMoodEntry() {
        if (uiState.value.moodNote.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add a note about how you're feeling") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val moodEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            timestamp = uiState.value.selectedDate,
            mood = uiState.value.currentMood,
            note = uiState.value.moodNote
        )

        viewModelScope.launch {
            try {
                moodRepository.saveMoodEntry(moodEntry).collect { result ->
                    when (result) {
                        is ResultState.Success -> {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    savedSuccessfully = true,
                                    moodNote = "",
                                    errorMessage = "",
                                    successMessage = result.data // Capture the success message
                                )
                            }
                            loadMoodEntries() // Optional, if you want to reload the entries
                        }
                        is ResultState.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = result.message?.localizedMessage ?: "Failed to save mood entry"
                                )
                            }
                        }
                        is ResultState.Loading -> {
                            // Handle loading state if needed
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Failed to save mood entry"
                    )
                }
            }
        }
    }


    private fun loadMoodEntries() {
        viewModelScope.launch {
            moodRepository.getMoodEntries().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is ResultState.Success -> {
                        _uiState.update { it.copy(moodEntries = result.data, isLoading = false) }
                    }

                    is ResultState.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message.localizedMessage
                                    ?: "Failed to load mood entries"
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}
