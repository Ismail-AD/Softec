package com.appdev.softec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appdev.softec.data.local.dao.TaskDao
import com.appdev.softec.domain.model.TaskData

@Database(entities = [TaskData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}