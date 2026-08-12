package com.sagrd.mentorly.presentation.progress

sealed interface EnrollmentProgressUiEvent {
    data object Refresh : EnrollmentProgressUiEvent
    data class CompleteTheme(val themeId: String) : EnrollmentProgressUiEvent
    data object ClearError : EnrollmentProgressUiEvent
}
