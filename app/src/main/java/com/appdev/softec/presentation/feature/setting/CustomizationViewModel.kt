package com.appdev.softec.presentation.feature.setting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.utils.CustomizationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val customizationPreferences: CustomizationPreferences
) : ViewModel() {

    private val _customizationState =
        MutableStateFlow(customizationPreferences.loadCustomizationState())
    val customizationState: StateFlow<CustomizationState> = _customizationState.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            customizationPreferences.setDarkMode(enabled)
            _customizationState.update { it.copy(isDarkMode = enabled) }
        }
    }

    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            customizationPreferences.setFontSize(fontSize)
            _customizationState.update { it.copy(fontSize = fontSize) }
        }
    }

    fun setLayoutType(layoutType: LayoutType) {
        viewModelScope.launch {
            customizationPreferences.setLayoutType(layoutType)
            _customizationState.update { it.copy(layoutType = layoutType) }
        }
    }

    fun setNotificationStyle(notificationStyle: NotificationStyle) {
        viewModelScope.launch {
            customizationPreferences.setNotificationStyle(notificationStyle)
            _customizationState.update { it.copy(notificationStyle = notificationStyle) }
        }
    }
}