package com.sagrd.mentorly.presentation.enrollment.list

sealed interface EnrollmentListUiEvent {
    data object Refresh : EnrollmentListUiEvent
    data object ClearError : EnrollmentListUiEvent
}
