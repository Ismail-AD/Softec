package com.appdev.softec.domain.model

data class UserProfile(
    val userId: String = "",
    val name: String? = null,
    val email: String? = null,
)