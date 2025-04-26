package com.appdev.softec.domain.model

import com.appdev.softec.presentation.feature.Mood.MoodType

data class MoodEntry(
    val id: String = "",
    val timestamp: Long = 0L,
    val mood: MoodType = MoodType.NEUTRAL,
    val note: String = ""
)