// LoginViewModel.kt
package com.appdev.softec.presentation.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.repository.LoginRepository
import com.appdev.softec.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, isEmailError = false) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, isPasswordError = false) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            loginRepository.loginWithEmailAndPassword(uiState.value.email, uiState.value.password)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is ResultState.Success -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                success = true,
                                errorMessage = ""
                            )}
                        }
                        is ResultState.Failure -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                success = false,
                                errorMessage = result.message.localizedMessage ?: "Login failed"
                            )}
                        }
                    }
                }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (uiState.value.email.isBlank()) {
            _uiState.update { it.copy(isEmailError = true) }
            isValid = false
        }

        if (uiState.value.password.isBlank()) {
            _uiState.update { it.copy(isPasswordError = true) }
            isValid = false
        }

        if (!isValid) {
            _uiState.update { it.copy(errorMessage = "Please fill all required fields") }
        }

        return isValid
    }
}