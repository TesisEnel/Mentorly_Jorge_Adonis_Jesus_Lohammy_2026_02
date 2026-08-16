package com.sagrd.mentorly.presentation.submission.form

import com.sagrd.mentorly.domain.model.submission.EvidenceType

sealed interface SubmissionFormUiEvent {
    data class Load(
        val enrollmentId: String,
        val activityId: String,
        val submissionId: String?
    ) : SubmissionFormUiEvent
    data class EvidenceTypeChanged(val value: EvidenceType) : SubmissionFormUiEvent
    data class EvidenceContentChanged(val value: String) : SubmissionFormUiEvent
    data object Save : SubmissionFormUiEvent
    data object DismissError : SubmissionFormUiEvent
}
