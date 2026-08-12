package com.sagrd.mentorly.presentation.submission.detail

sealed interface SubmissionDetailUiEvent {
    data class Load(val submissionId: String) : SubmissionDetailUiEvent
    data object Refresh : SubmissionDetailUiEvent
    data object Escalate : SubmissionDetailUiEvent
    data object EditClicked : SubmissionDetailUiEvent
    data object DismissError : SubmissionDetailUiEvent
}