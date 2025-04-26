package com.appdev.softec.presentation.feature.taskManagement.TasksList

import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategory
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategoryGeneral

data class TaskUiState(
    val tasks: List<TaskData> = emptyList(),
    val categories: List<TaskCategoryGeneral> = TaskCategoryGeneral.entries,
    val selectedCategory: TaskCategoryGeneral = TaskCategoryGeneral.ALL,
    val taskText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)