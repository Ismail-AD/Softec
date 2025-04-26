package com.appdev.softec.presentation.feature.auth.signup


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.domain.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, isNameError = false) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, isEmailError = false) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, isPasswordError = false) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(
            confirmPassword = confirmPassword,
            isConfirmPasswordError = false
        )}
    }

    fun updateImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun updateImageBytes(bytes: ByteArray?) {
        _uiState.update { it.copy(imageBytes = bytes) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun signUp() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true) }

        val userEntity = UserEntity(
            name = uiState.value.name,
            email = uiState.value.email,
            password = uiState.value.password
        )

        viewModelScope.launch {
            signUpRepository.signUp(
                userEntity,
                uiState.value.imageUri,
                uiState.value.imageBytes
            ) { message, success, imageUrl ->
                if (success) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        success = true,
                        errorMessage = "",
                        profileImageUrl = imageUrl ?: ""
                    )}
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        success = false,
                        errorMessage = message
                    )}
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Name validation
        if (uiState.value.name.isBlank()) {
            _uiState.update { it.copy(isNameError = true) }
            isValid = false
        }

        // Email validation
        if (uiState.value.email.isBlank() || !isValidEmail(uiState.value.email)) {
            _uiState.update { it.copy(isEmailError = true) }
            isValid = false
        }

        // Password validation
        if (uiState.value.password.isBlank() || uiState.value.password.length < 6) {
            _uiState.update { it.copy(isPasswordError = true) }
            isValid = false
        }

        // Confirm password validation
        if (uiState.value.confirmPassword != uiState.value.password) {
            _uiState.update { it.copy(isConfirmPasswordError = true) }
            isValid = false
        }

        if (!isValid) {
            _uiState.update { it.copy(errorMessage = "Please check all required fields") }
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
}