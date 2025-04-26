package com.appdev.softec.presentation.navigation

sealed class Routes(val route: String) {

    // Authentication
    data object Login : Routes("login_screen")
    data object Register : Routes("register_screen")

    // Home & Dashboard
    data object Dashboard : Routes("dashboard_screen")

    // Task Creation
    data object NaturalLanguageTaskCreation : Routes("natural_language_task_creation_screen")

    // Smart Task Categorization
    data object SmartTaskCategorization : Routes("smart_task_categorization_screen")

    // Planner / Calendar
    data object PlannerCalendarView : Routes("planner_calendar_view_screen")

    // Mood-Based Suggestions
    data object MoodBasedSuggestions : Routes("mood_based_suggestions_screen")

    // Voice Input for Tasks
    data object VoiceTaskInput : Routes("voice_task_input_screen")

    // Auto Notes Summarizer
    data object AutoNotesSummarizer : Routes("auto_notes_summarizer_screen")

    // Checklist Generator from Goal
    data object ChecklistGenerator : Routes("checklist_generator_screen")

    // Smart Reminders & Nudges
    data object SmartReminders : Routes("smart_reminders_screen")

    // Quick Add from Camera (OCR)
    data object QuickAddFromCamera : Routes("quick_add_from_camera_screen")

    // Mood Journal / Emotional Tracker
    data object MoodJournal : Routes("mood_journal_screen")

    // Personal Progress Tracker
    data object ProgressTracker : Routes("progress_tracker_screen")

    // Offline Usability
    data object OfflineMode : Routes("offline_mode_screen")

    // Customization Options
    data object CustomizationSettings : Routes("customization_settings_screen")

    // Notification Center
    data object NotificationCenter : Routes("notification_center_screen")

    // Adaptive Suggestions Engine
    data object AdaptiveSuggestions : Routes("adaptive_suggestions_screen")
}
