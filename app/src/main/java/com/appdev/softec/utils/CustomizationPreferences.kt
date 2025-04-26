package com.appdev.softec.utils

import android.content.Context
import com.appdev.softec.presentation.feature.setting.CustomizationState
import com.appdev.softec.presentation.feature.setting.FontSize
import com.appdev.softec.presentation.feature.setting.LayoutType
import com.appdev.softec.presentation.feature.setting.NotificationStyle

class CustomizationPreferences(private val context: Context) {
    // Separate SharedPreferences for each category
    private val themePrefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
    private val fontPrefs = context.getSharedPreferences("font_preferences", Context.MODE_PRIVATE)
    private val layoutPrefs =
        context.getSharedPreferences("layout_preferences", Context.MODE_PRIVATE)
    private val notificationPrefs =
        context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)

    // Theme preferences
    fun isDarkModeEnabled(): Boolean = themePrefs.getBoolean("is_dark_mode", false)
    fun setDarkMode(enabled: Boolean) {
        themePrefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    // Font preferences
    fun getFontSize(): FontSize {
        val name = fontPrefs.getString("font_size", FontSize.MEDIUM.name) ?: FontSize.MEDIUM.name
        return try {
            FontSize.valueOf(name)
        } catch (e: Exception) {
            FontSize.MEDIUM
        }
    }

    fun setFontSize(fontSize: FontSize) {
        fontPrefs.edit().putString("font_size", fontSize.name).apply()
    }

    // Layout preferences
    fun getLayoutType(): LayoutType {
        val name =
            layoutPrefs.getString("layout_type", LayoutType.DEFAULT.name) ?: LayoutType.DEFAULT.name
        return try {
            LayoutType.valueOf(name)
        } catch (e: Exception) {
            LayoutType.DEFAULT
        }
    }

    fun setLayoutType(layoutType: LayoutType) {
        layoutPrefs.edit().putString("layout_type", layoutType.name).apply()
    }

    // Notification preferences
    fun getNotificationStyle(): NotificationStyle {
        val name =
            notificationPrefs.getString("notification_style", NotificationStyle.STANDARD.name)
                ?: NotificationStyle.STANDARD.name
        return try {
            NotificationStyle.valueOf(name)
        } catch (e: Exception) {
            NotificationStyle.STANDARD
        }
    }

    fun setNotificationStyle(notificationStyle: NotificationStyle) {
        notificationPrefs.edit().putString("notification_style", notificationStyle.name).apply()
    }

    // Load all preferences at once
    fun loadCustomizationState(): CustomizationState {
        return CustomizationState(
            isDarkMode = isDarkModeEnabled(),
            fontSize = getFontSize(),
            layoutType = getLayoutType(),
            notificationStyle = getNotificationStyle()
        )
    }
}