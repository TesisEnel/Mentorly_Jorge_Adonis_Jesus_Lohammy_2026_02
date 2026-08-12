package com.sagrd.mentorly.presentation.enrollment.detail

sealed interface EnrollmentDetailUiEvent {
    data object Refresh : EnrollmentDetailUiEvent
    data object LoadCertificate : EnrollmentDetailUiEvent
    data object ShowRestartConfirmation : EnrollmentDetailUiEvent
    data object DismissRestartConfirmation : EnrollmentDetailUiEvent
    data object ConfirmRestart : EnrollmentDetailUiEvent
    data object ClearError : EnrollmentDetailUiEvent
}
