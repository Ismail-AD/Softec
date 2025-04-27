package com.appdev.softec.domain.repository

import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.domain.model.UserProfile
import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<ResultState<UserProfile>>
}