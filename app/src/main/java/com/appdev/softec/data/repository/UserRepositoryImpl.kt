package com.appdev.softec.data.repository


import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.domain.model.UserProfile
import com.appdev.softec.domain.repository.UserRepository
import com.appdev.softec.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<ResultState<UserProfile>> = callbackFlow {
        trySend(ResultState.Loading)

        val userDocRef = firestore.collection("profiles").document(userId)

        val listenerRegistration = userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(ResultState.Failure(e))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val profile = UserProfile(
                        userId = snapshot.id,
                        name = snapshot.getString("name"),
                        email = snapshot.getString("email"),
                    )
                    trySend(ResultState.Success(profile))
                } catch (ex: Exception) {
                    trySend(ResultState.Failure(ex))
                }
            } else {
                // Create a default profile if none exists
                val currentUser = auth.currentUser
                val defaultProfile = UserProfile(
                    userId = currentUser?.uid ?: "",
                    name = currentUser?.displayName,
                    email = currentUser?.email,
                )
                trySend(ResultState.Success(defaultProfile))
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}