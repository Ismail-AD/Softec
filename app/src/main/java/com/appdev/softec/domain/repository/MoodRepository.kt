package com.appdev.softec.domain.repository

import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun saveMoodEntry(entry: MoodEntry): Flow<ResultState<String>>
    suspend fun getMoodEntries(): Flow<ResultState<List<MoodEntry>>>
    suspend fun getMoodEntriesByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<ResultState<List<MoodEntry>>>

    suspend fun getMoodEntryById(id: String): Flow<ResultState<MoodEntry?>>
    suspend fun deleteMoodEntry(id: String): Flow<ResultState<Unit>>
}