package com.appdev.softec.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tasks")
data class TaskData(
    @PrimaryKey val id: String,
    val text: String,
    val category: String,
    val createdAt: Long,
    val dueDate: Long?,
    val isCompleted: Boolean,
    val isSynced: Boolean = false // Added field to track sync status
)