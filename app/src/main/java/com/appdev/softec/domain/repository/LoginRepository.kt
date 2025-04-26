package com.appdev.softec.domain.repository


import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    suspend fun loginWithEmailAndPassword(email: String, password: String): Flow<ResultState<Boolean>>
}