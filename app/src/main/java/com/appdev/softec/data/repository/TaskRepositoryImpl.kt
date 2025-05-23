package com.appdev.softec.data.repository

import android.util.Log
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.repository.TaskRepository
import com.appdev.softec.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskRepository {


    override fun getTasksByDateRange(startDate: Long, endDate: Long): Flow<ResultState<List<TaskData>>> = callbackFlow {
        trySend(ResultState.Loading)

        val userId = auth.currentUser?.uid ?: run {
            trySend(ResultState.Failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        // Get all tasks for the user and filter in memory
        val listenerRegistration = firestore.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(ResultState.Failure(e))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allTasks = snapshot.documents.mapNotNull { doc ->
                        // Your mapping code here...
                        try {
                            val id = doc.getString("id") ?: return@mapNotNull null
                            val text = doc.getString("text") ?: return@mapNotNull null
                            val category = doc.getString("category") ?: return@mapNotNull null
                            val createdAt = doc.getLong("createdAt") ?: return@mapNotNull null
                            val dueDate = doc.getLong("dueDate")
                            val isCompleted = doc.getBoolean("isCompleted") ?: false

                            TaskData(
                                id = id,
                                text = text,
                                category = category,
                                createdAt = createdAt,
                                dueDate = dueDate,
                                isCompleted = isCompleted
                            )
                        } catch (e: Exception) {
                            Log.d("CHAZQ","${e.localizedMessage}")
                            null
                        }
                    }
                    Log.d("CHAZQ","${allTasks}")


                    // Filter tasks in memory
                    val tasksInRange = allTasks.filter { task ->
                        task.dueDate != null && task.dueDate >= startDate && task.dueDate <= endDate
                    }

                    trySend(ResultState.Success(tasksInRange))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getTasksByUser(): Flow<ResultState<List<TaskData>>> = callbackFlow {
        trySend(ResultState.Loading)

        val userId = auth.currentUser?.uid ?: run {
            trySend(ResultState.Failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(ResultState.Failure(e))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: return@mapNotNull null
                        val text = doc.getString("text") ?: return@mapNotNull null
                        val category = doc.getString("category") ?: return@mapNotNull null
                        val createdAt = doc.getLong("createdAt") ?: return@mapNotNull null
                        val dueDate = doc.getLong("dueDate")
                        val isCompleted = doc.getBoolean("isCompleted") ?: false

                        TaskData(
                            id = id,
                            text = text,
                            category = category,
                            createdAt = createdAt,
                            dueDate = dueDate,
                            isCompleted = isCompleted
                        )
                    }
                    trySend(ResultState.Success(tasks))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun saveTask(taskData: TaskData): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        val userId = auth.currentUser?.uid ?: run {
            trySend(ResultState.Failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        val taskMap = mapOf(
            "id" to taskData.id,
            "text" to taskData.text,
            "category" to taskData.category,
            "createdAt" to taskData.createdAt,
            "dueDate" to taskData.dueDate,
            "isCompleted" to taskData.isCompleted,
            "userId" to userId
        )

        firestore.collection("tasks")
            .document(taskData.id)
            .set(taskMap)
            .addOnSuccessListener {
                trySend(ResultState.Success("Task saved successfully"))
                close()
            }
            .addOnFailureListener { e ->
                trySend(ResultState.Failure(e))
                close()
            }

        awaitClose()
    }

    // New method: updateTask
    override fun updateTask(taskData: TaskData): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        val userId = auth.currentUser?.uid ?: run {
            trySend(ResultState.Failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        val taskMap = mapOf(
            "id" to taskData.id,
            "text" to taskData.text,
            "category" to taskData.category,
            "createdAt" to taskData.createdAt,
            "dueDate" to taskData.dueDate,
            "isCompleted" to taskData.isCompleted,
            "userId" to userId
        )

        firestore.collection("tasks")
            .document(taskData.id)
            .set(taskMap)  // Using set() instead of update()
            .addOnSuccessListener {
                trySend(ResultState.Success("Task updated successfully"))
                close()
            }
            .addOnFailureListener { e ->
                Log.d("AJZ","${e.localizedMessage}")
                trySend(ResultState.Failure(e))
                close()
            }

        awaitClose()
    }

    override fun updateTaskStatus(taskId: String, isCompleted: Boolean): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        firestore.collection("tasks")
            .document(taskId)
            .update("isCompleted", isCompleted)
            .addOnSuccessListener {
                trySend(ResultState.Success("Task status updated"))
                close()
            }
            .addOnFailureListener { e ->
                trySend(ResultState.Failure(e))
                close()
            }

        awaitClose()
    }

    override fun deleteTask(taskId: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        firestore.collection("tasks")
            .document(taskId)
            .delete()
            .addOnSuccessListener {
                trySend(ResultState.Success("Task deleted"))
                close()
            }
            .addOnFailureListener { e ->
                trySend(ResultState.Failure(e))
                close()
            }

        awaitClose()
    }
}