package com.appdev.softec.presentation.feature.taskManagement.TasksList

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.repository.TaskRepository
import com.appdev.softec.utils.ResultState
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategory
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategoryGeneral
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class GetTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // Initialize the view model by fetching tasks
    init {
        fetchTasks()
    }

    // Fetch tasks from the repository
    fun fetchTasks() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            taskRepository.getTasksByUser().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update {
                            it.copy(
                                tasks = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is ResultState.Failure -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message.localizedMessage ?: "Error fetching tasks",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Update the task text in the UI state
    fun updateTaskText(text: String) {
        _uiState.update { it.copy(taskText = text) }
    }

    // Update selected category
    fun updateCategory(category: TaskCategoryGeneral) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    // In GetTaskViewModel
    fun deleteTask(task: TaskData) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        // Refresh tasks after deletion
                        fetchTasks()
                    }
                    is ResultState.Failure -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message.localizedMessage ?: "Error deleting task",
                                isLoading = false
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    // Toggle task completion status
    fun toggleTaskCompletion(task: TaskData) {
        viewModelScope.launch {
            // Create a new task with toggled completion status
            val updatedTask = task.copy(isCompleted = !task.isCompleted)

            // Update the task in the repository
            taskRepository.updateTask(updatedTask).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        // Refresh the task list to reflect changes
                        fetchTasks()
                    }
                    is ResultState.Failure -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message.localizedMessage ?: "Error updating task",
                                isLoading = false
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Clear error messages
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}

