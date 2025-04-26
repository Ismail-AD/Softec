package com.appdev.softec.domain.repository

import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun saveTask(taskData: TaskData): Flow<ResultState<String>>
    fun getTasksByUser(): Flow<ResultState<List<TaskData>>>
    fun updateTaskStatus(taskId: String, isCompleted: Boolean): Flow<ResultState<String>>
    fun deleteTask(taskId: String): Flow<ResultState<String>>
}