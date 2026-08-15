package com.sagrd.mentorly.presentation.admin.submission.list

sealed interface AdminSubmissionListUiEvent {
    data object Load : AdminSubmissionListUiEvent
    data object Refresh : AdminSubmissionListUiEvent
    data class SearchChanged(val value: String) : AdminSubmissionListUiEvent
    data class FilterChanged(val filter: EscalatedSubmissionFilter) : AdminSubmissionListUiEvent
    data object ClearError : AdminSubmissionListUiEvent
}
