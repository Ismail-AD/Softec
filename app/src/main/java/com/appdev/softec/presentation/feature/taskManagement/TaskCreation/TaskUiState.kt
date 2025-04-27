package com.appdev.softec.presentation.feature.taskManagement.TaskCreation

import java.util.UUID


data class TaskUiState(
    val taskId: String = UUID.randomUUID().toString(),
    val taskText: String = "",
    val category: TaskCategory = TaskCategory.GENERAL,
    val dueDate: Long? = null,
    val isVoiceInputActive: Boolean = false,
    val isCameraActive: Boolean = false,
    val isProcessingInput: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String = "",
    val availableCategories: List<TaskCategory> = TaskCategory.entries
)

enum class TaskCategory {
    GENERAL,
    WORK,
    PERSONAL,
    SHOPPING,
    HEALTH,
    EDUCATION,
    FINANCE,
    OTHER
}

enum class TaskCategoryGeneral {
    ALL,
    GENERAL,
    WORK,
    PERSONAL,
    SHOPPING,
    HEALTH,
    EDUCATION,
    FINANCE,
    OTHER
}