package com.appdev.softec.presentation.navigation

sealed class Routes(val route: String) {

    // Authentication
    data object Login : Routes("login_screen")
    data object Register : Routes("register_screen")

    // Home & Dashboard
    data object Dashboard : Routes("dashboard_screen")
    data object HomePage : Routes("home_screen")

    data object TaskCreation : Routes("task_creation_screen")
    data object TaskList : Routes("task_list_screen")
    data object Calendar : Routes("calendar_screen")
    data object MoodJournal : Routes("mood_journal_screen")
    data object Notes : Routes("notes_screen")
    data object ChecklistGenerator : Routes("checklist_generator_screen")
    data object NotificationCenter : Routes("notification_center_screen")
    data object Settings : Routes("settings_screen")

    // Additional utility routes
    data object TaskDetail : Routes("task_detail_screen/{taskId}") {
        fun createRoute(taskId: String) = "task_detail_screen/$taskId"
    }

    data object MoodDetail : Routes("mood_detail_screen/{date}") {
        fun createRoute(date: String) = "mood_detail_screen/$date"
    }

    data object NoteDetail : Routes("note_detail_screen/{noteId}") {
        fun createRoute(noteId: String) = "note_detail_screen/$noteId"
    }
}
