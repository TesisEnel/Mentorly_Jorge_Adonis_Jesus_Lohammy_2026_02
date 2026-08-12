package com.sagrd.mentorly.presentation.submission.form

sealed interface SubmissionFormUiEvent {
    data class Load(
        val enrollmentId: String,
        val activityId: String,
        val submissionId: String?
    ) : SubmissionFormUiEvent
    data class EvidenceUrlChanged(val value: String) : SubmissionFormUiEvent
    data object Save : SubmissionFormUiEvent
    data object DismissError : SubmissionFormUiEvent
}