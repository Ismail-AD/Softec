package com.appdev.softec.presentation.feature.auth.signup

import android.net.Uri

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val imageUri: Uri? = null,
    val imageBytes: ByteArray? = null,
    val profileImageUrl: String = "",
    val isNameError: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignUpUiState

        if (name != other.name) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (confirmPassword != other.confirmPassword) return false
        if (imageUri != other.imageUri) return false
        if (imageBytes != null) {
            if (other.imageBytes == null) return false
            if (!imageBytes.contentEquals(other.imageBytes)) return false
        } else if (other.imageBytes != null) return false
        if (profileImageUrl != other.profileImageUrl) return false
        if (isNameError != other.isNameError) return false
        if (isEmailError != other.isEmailError) return false
        if (isPasswordError != other.isPasswordError) return false
        if (isConfirmPasswordError != other.isConfirmPasswordError) return false
        if (isPasswordVisible != other.isPasswordVisible) return false
        if (isConfirmPasswordVisible != other.isConfirmPasswordVisible) return false
        if (isLoading != other.isLoading) return false
        if (success != other.success) return false
        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + confirmPassword.hashCode()
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        result = 31 * result + profileImageUrl.hashCode()
        result = 31 * result + isNameError.hashCode()
        result = 31 * result + isEmailError.hashCode()
        result = 31 * result + isPasswordError.hashCode()
        result = 31 * result + isConfirmPasswordError.hashCode()
        result = 31 * result + isPasswordVisible.hashCode()
        result = 31 * result + isConfirmPasswordVisible.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + errorMessage.hashCode()
        return result
    }
}