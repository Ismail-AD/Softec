package com.appdev.softec.presentation.feature.setting

data class CustomizationState(
    val isDarkMode: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val layoutType: LayoutType = LayoutType.DEFAULT,
    val notificationStyle: NotificationStyle = NotificationStyle.STANDARD
)

enum class FontSize(val size: Float) {
    SMALL(14f),
    MEDIUM(16f),
    LARGE(18f),
    EXTRA_LARGE(20f)
}

enum class LayoutType {
    DEFAULT,
    COMPACT,
    GRID,
    LIST
}

enum class NotificationStyle {
    STANDARD,
    MINIMAL,
    DETAILED,
    SILENT
}