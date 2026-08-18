package com.sagrd.mentorly.presentation.admin.student.detail

sealed interface AdminStudentDetailUiEvent {
    data object Load : AdminStudentDetailUiEvent
    data object Refresh : AdminStudentDetailUiEvent
    data class ToggleEnrollmentExpansion(val enrollmentId: String) : AdminStudentDetailUiEvent
    data object ClearError : AdminStudentDetailUiEvent
}
