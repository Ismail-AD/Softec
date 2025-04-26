package com.appdev.softec.presentation.feature.Mood

import androidx.compose.ui.graphics.Color
import com.appdev.softec.domain.model.MoodEntry

data class MoodJournalUiState(
    val currentMood: MoodType = MoodType.NEUTRAL,
    val moodNote: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String = "",
    val moodEntries: List<MoodEntry> = emptyList(),
    val isCalendarVisible: Boolean = false,
    val selectedPeriod: AnalysisPeriod = AnalysisPeriod.WEEK,
    val isAnalysisVisible: Boolean = false,
    val isLoading: Boolean = false ,
    val successMessage: String = "", // New field to capture success messages
)


enum class MoodType {
    VERY_HAPPY,
    HAPPY,
    NEUTRAL,
    SAD,
    VERY_SAD,
    ANGRY,
    STRESSED,
    EXCITED,
    CALM,
    TIRED;

    fun getEmoji(): String {
        return when (this) {
            VERY_HAPPY -> "😄"
            HAPPY -> "😊"
            NEUTRAL -> "😐"
            SAD -> "😔"
            VERY_SAD -> "😭"
            ANGRY -> "😡"
            STRESSED -> "😩"
            EXCITED -> "🥳"
            CALM -> "😌"
            TIRED -> "😴"
        }
    }

    fun getColor(): Color {
        return when (this) {
            VERY_HAPPY -> Color(0xFF4CAF50) // Green
            HAPPY -> Color(0xFF8BC34A)      // Light Green
            NEUTRAL -> Color(0xFFFFC107)    // Amber
            SAD -> Color(0xFFFF9800)        // Orange
            VERY_SAD -> Color(0xFFF44336)   // Red
            ANGRY -> Color(0xFFD32F2F)      // Dark Red
            STRESSED -> Color(0xFF7B1FA2)   // Purple
            EXCITED -> Color(0xFF00BCD4)    // Cyan
            CALM -> Color(0xFF03A9F4)        // Light Blue
            TIRED -> Color(0xFF9E9E9E)       // Grey
        }
    }
}

enum class AnalysisPeriod {
    WEEK, MONTH, YEAR
}
