package com.sagrd.mentorly.presentation.submission.list

sealed interface SubmissionListUiEvent {
    data object Load : SubmissionListUiEvent
    data object Refresh : SubmissionListUiEvent
    data class OnSearchQueryChanged(val query: String) : SubmissionListUiEvent
    data object ClearSearch : SubmissionListUiEvent
    data object DismissError : SubmissionListUiEvent
}