package com.appdev.softec.presentation.feature.Home

import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.domain.model.UserProfile

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentDate: String = "",
    val userEntry: UserProfile? = null,
    val todaysTasks: List<TaskData> = emptyList(),
    val tasksByCategory: Map<String, List<TaskData>> = emptyMap(),
    val completedTasksCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val hasTodaysTasks: Boolean = false,
    val currentMood: MoodEntry? = null,
    val showMoodSelector: Boolean = true,
    val error: String? = null
)