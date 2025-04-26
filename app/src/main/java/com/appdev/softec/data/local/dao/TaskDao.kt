package com.appdev.softec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appdev.softec.domain.model.TaskData

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskData)

    @Update
    suspend fun updateTask(task: TaskData)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnSyncedTasks(): List<TaskData>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)
}