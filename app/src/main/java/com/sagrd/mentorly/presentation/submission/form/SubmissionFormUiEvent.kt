package com.sagrd.mentorly.presentation.submission.form

sealed interface SubmissionFormUiEvent {
    data class Load(
        val enrollmentId: String,
        val activityId: String,
        val submissionId: String? = null
    ) : SubmissionFormUiEvent

    data class UrlContentChanged(val value: String) : SubmissionFormUiEvent
    data class CommentsContentChanged(val value: String) : SubmissionFormUiEvent
    data class TextContentChanged(val value: String) : SubmissionFormUiEvent
    data object Save : SubmissionFormUiEvent
    data object DismissError : SubmissionFormUiEvent
}
