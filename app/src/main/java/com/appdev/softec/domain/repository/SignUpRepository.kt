package com.appdev.softec.domain.repository

import android.net.Uri
import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface SignUpRepository {
    suspend fun signUp(
        userEntity: UserEntity,
        result: (message: String, success: Boolean) -> Unit
    )
}