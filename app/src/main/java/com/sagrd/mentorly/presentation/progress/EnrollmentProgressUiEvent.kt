package com.sagrd.mentorly.presentation.progress

sealed interface EnrollmentProgressUiEvent {
    data object Refresh : EnrollmentProgressUiEvent
    data object ClearError : EnrollmentProgressUiEvent
}
