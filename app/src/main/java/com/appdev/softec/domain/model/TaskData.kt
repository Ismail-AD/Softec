package com.appdev.softec.domain.model


data class TaskData(
    val id: String,
    val text: String,
    val category: String,
    val createdAt: Long,
    val dueDate: Long?,
    val isCompleted: Boolean
)