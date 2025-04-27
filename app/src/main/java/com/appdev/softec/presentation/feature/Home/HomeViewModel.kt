package com.appdev.softec.presentation.feature.Home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.repository.MoodRepository
import com.appdev.softec.domain.repository.TaskRepository
import com.appdev.softec.domain.repository.UserRepository
import com.appdev.softec.presentation.feature.Mood.MoodType
import com.appdev.softec.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val moodRepository: MoodRepository,
    val firebaseAuth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Track loading states for each data type
    private var isTasksLoading = true
    private var isMoodLoading = true
    private var isProfileLoading = true

    init {
        _uiState.update { it.copy(
            currentDate = dateFormat.format(Date()),
            isLoading = true
        ) }
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // Get today's range
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1

            // Load today's tasks
            loadTodaysTasks(todayStart, todayEnd)

            // Load today's latest mood
            loadTodaysMood(todayStart, todayEnd)

            // Load user profile
            loadUserProfile()
        }
    }

    private fun loadTodaysTasks(todayStart: Long, todayEnd: Long) {
        viewModelScope.launch {
            isTasksLoading = true
            updateLoadingState()

            taskRepository.getTasksByDateRange(todayStart, todayEnd).collect { result ->
                isTasksLoading = false

                when (result) {
                    is ResultState.Success -> {
                        val todaysTasks = result.data
                        val tasksByCategory = todaysTasks.groupBy { it.category ?: "Uncategorized" }
                        val completedTasksCount = todaysTasks.count { it.isCompleted }
                        val pendingTasksCount = todaysTasks.count { !it.isCompleted }

                        _uiState.update { state ->
                            state.copy(
                                todaysTasks = todaysTasks,
                                tasksByCategory = tasksByCategory,
                                completedTasksCount = completedTasksCount,
                                pendingTasksCount = pendingTasksCount,
                                hasTodaysTasks = todaysTasks.isNotEmpty()
                            )
                        }
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            error = result.message.localizedMessage ?: "Error loading tasks"
                        ) }
                    }
                    is ResultState.Loading -> {
                        // Loading state already set
                    }
                }

                updateLoadingState()
            }
        }
    }

    private fun loadTodaysMood(todayStart: Long, todayEnd: Long) {
        viewModelScope.launch {
            isMoodLoading = true
            updateLoadingState()

            moodRepository.getMoodEntriesByDateRange(todayStart, todayEnd).collect { result ->
                isMoodLoading = false

                when (result) {
                    is ResultState.Success -> {
                        // Find the latest mood entry for today by sorting by timestamp in descending order
                        val latestMood = result.data.maxByOrNull { it.timestamp }

                        // Debug logging
                        Log.d("HomeViewModel", "Mood entries found: ${result.data.size}")
                        if (latestMood != null) {
                            Log.d("HomeViewModel", "Latest mood: ${latestMood.mood}, time: ${Date(latestMood.timestamp)}")
                        } else {
                            Log.d("HomeViewModel", "No mood entries found for today")
                        }

                        _uiState.update { it.copy(
                            currentMood = latestMood,
                            showMoodSelector = latestMood == null
                        ) }
                    }
                    is ResultState.Failure -> {
                        Log.e("HomeViewModel", "Error loading mood: ${result.message.localizedMessage}")
                        _uiState.update { it.copy(
                            showMoodSelector = true,
                            error = result.message.localizedMessage ?: "Error loading mood data"
                        ) }
                    }
                    is ResultState.Loading -> {
                        // Already handling loading state
                    }
                }

                updateLoadingState()
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            isProfileLoading = true
            updateLoadingState()

            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                userRepository.getUserProfile(userId).collect { result ->
                    isProfileLoading = false

                    when (result) {
                        is ResultState.Success -> {
                            _uiState.update { it.copy(
                                userEntry = result.data
                            ) }
                        }
                        is ResultState.Failure -> {
                            _uiState.update { it.copy(
                                error = result.message.localizedMessage ?: "Error loading user profile"
                            ) }
                        }
                        is ResultState.Loading -> {
                            // Loading state already being handled
                        }
                    }

                    updateLoadingState()
                }
            } else {
                isProfileLoading = false
                _uiState.update { it.copy(
                    error = "User not logged in"
                ) }
                updateLoadingState()
            }
        }
    }

    // Helper method to update the loading state based on all data requests
    private fun updateLoadingState() {
        val isStillLoading = isTasksLoading || isMoodLoading || isProfileLoading
        _uiState.update { it.copy(isLoading = isStillLoading) }
    }

    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, isCompleted).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        // Instead of reloading all data, update the specific task in the state
                        _uiState.update { state ->
                            val updatedTasks = state.todaysTasks.map { task ->
                                if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                            }

                            val updatedTasksByCategory = updatedTasks.groupBy { it.category ?: "Uncategorized" }
                            val completedCount = updatedTasks.count { it.isCompleted }
                            val pendingCount = updatedTasks.count { !it.isCompleted }

                            state.copy(
                                todaysTasks = updatedTasks,
                                tasksByCategory = updatedTasksByCategory,
                                completedTasksCount = completedCount,
                                pendingTasksCount = pendingCount
                            )
                        }
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            error = result.message.localizedMessage ?: "Failed to update task"
                        ) }
                    }
                    is ResultState.Loading -> {
                        // No need to show loading for quick updates
                    }
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        // Update the UI state without reloading all data
                        _uiState.update { state ->
                            val updatedTasks = state.todaysTasks.filter { it.id != taskId }
                            val updatedTasksByCategory = updatedTasks.groupBy { it.category ?: "Uncategorized" }
                            val completedCount = updatedTasks.count { it.isCompleted }
                            val pendingCount = updatedTasks.count { !it.isCompleted }

                            state.copy(
                                todaysTasks = updatedTasks,
                                tasksByCategory = updatedTasksByCategory,
                                completedTasksCount = completedCount,
                                pendingTasksCount = pendingCount,
                                hasTodaysTasks = updatedTasks.isNotEmpty()
                            )
                        }
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            error = result.message.localizedMessage ?: "Failed to delete task"
                        ) }
                    }
                    is ResultState.Loading -> {
                        // No need to show loading for quick deletes
                    }
                }
            }
        }
    }

    fun updateMood(mood: MoodType, note: String) {
        viewModelScope.launch {
            val moodEntry = MoodEntry(
                id = UUID.randomUUID().toString(),
                mood = mood,
                note = note,
                timestamp = System.currentTimeMillis()
            )

            moodRepository.saveMoodEntry(moodEntry).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        _uiState.update { it.copy(
                            currentMood = moodEntry,
                            showMoodSelector = false
                        ) }
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            error = result.message.localizedMessage ?: "Failed to update mood"
                        ) }
                    }
                    is ResultState.Loading -> {
                        // No need to show loading for quick mood updates
                    }
                }
            }
        }
    }

    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadUserData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}