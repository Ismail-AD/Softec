package com.appdev.softec.data.repository

import android.util.Log
import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.domain.repository.MoodRepository
import com.appdev.softec.presentation.feature.Mood.MoodType
import com.appdev.softec.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MoodRepository {

    private val moodCollection = firestore.collection("moodEntries")

    override suspend fun saveMoodEntry(entry: MoodEntry): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val moodMap = hashMapOf(
                "userId" to userId,
                "timestamp" to entry.timestamp,
                "mood" to entry.mood.name,
                "note" to entry.note
            )

            if (entry.id.isBlank()) {
                moodCollection.add(moodMap).await()
            } else {
                moodCollection.document(entry.id).set(moodMap).await()
            }

            emit(ResultState.Success("Mood entry saved successfully!"))
        } catch (e: Exception) {
            Log.d("CHKAZ","${e.localizedMessage}")
            emit(ResultState.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMoodEntries(): Flow<ResultState<List<MoodEntry>>> = flow {
        emit(ResultState.Loading)
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            val snapshot = moodCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val moodList = snapshot.documents.mapNotNull { document ->
                try {
                    MoodEntry(
                        id = document.id,
                        timestamp = document.getLong("timestamp") ?: 0L,
                        mood = MoodType.valueOf(document.getString("mood") ?: MoodType.NEUTRAL.name),
                        note = document.getString("note") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(ResultState.Success(moodList))
        } catch (e: Exception) {
            Log.d("CHKAZ","${e.localizedMessage}")
            emit(ResultState.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMoodEntriesByDateRange(startDate: Long, endDate: Long): Flow<ResultState<List<MoodEntry>>> = flow {
        emit(ResultState.Loading)
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            val snapshot = moodCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val moodList = snapshot.documents.mapNotNull { document ->
                try {
                    MoodEntry(
                        id = document.id,
                        timestamp = document.getLong("timestamp") ?: 0L,
                        mood = MoodType.valueOf(document.getString("mood") ?: MoodType.NEUTRAL.name),
                        note = document.getString("note") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(ResultState.Success(moodList))
        } catch (e: Exception) {
            emit(ResultState.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMoodEntryById(id: String): Flow<ResultState<MoodEntry?>> = flow {
        emit(ResultState.Loading)
        try {
            val document = moodCollection.document(id).get().await()

            if (document.exists()) {
                val moodEntry = MoodEntry(
                    id = document.id,
                    timestamp = document.getLong("timestamp") ?: 0L,
                    mood = MoodType.valueOf(document.getString("mood") ?: MoodType.NEUTRAL.name),
                    note = document.getString("note") ?: ""
                )
                emit(ResultState.Success(moodEntry))
            } else {
                emit(ResultState.Success(null))
            }
        } catch (e: Exception) {
            emit(ResultState.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteMoodEntry(id: String): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            moodCollection.document(id).delete().await()
            emit(ResultState.Success(Unit))
        } catch (e: Exception) {
            emit(ResultState.Failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
