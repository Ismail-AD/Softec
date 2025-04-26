// data/repository/LoginRepositoryImpl.kt
package com.appdev.softec.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.appdev.softec.domain.repository.LoginRepository
import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {

    override suspend fun loginWithEmailAndPassword(email: String, password: String): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading) // Emit loading state before starting the operation

        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(ResultState.Success(result.user != null)) // Emit success if user is found
        } catch (e: Exception) {
            emit(ResultState.Failure(e)) // Emit failure in case of an exception
        }
    }
}
