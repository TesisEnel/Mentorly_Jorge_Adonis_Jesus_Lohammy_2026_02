package com.sagrd.mentorly.presentation.submission.list

sealed interface SubmissionListUiEvent {
    data object Load : SubmissionListUiEvent
    data object Refresh : SubmissionListUiEvent
    data class SubmissionClicked(val submissionId: String) : SubmissionListUiEvent
    data object DismissError : SubmissionListUiEvent
}