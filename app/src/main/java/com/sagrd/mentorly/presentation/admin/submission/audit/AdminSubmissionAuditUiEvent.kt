package com.sagrd.mentorly.presentation.admin.submission.audit

sealed interface AdminSubmissionAuditUiEvent {
    data object Load : AdminSubmissionAuditUiEvent
    data object Retry : AdminSubmissionAuditUiEvent
    data class RequestDecision(val isApproved: Boolean) : AdminSubmissionAuditUiEvent
    data class DecisionCommentChanged(val value: String) : AdminSubmissionAuditUiEvent
    data object ConfirmDecision : AdminSubmissionAuditUiEvent
    data object DismissDecisionDialog : AdminSubmissionAuditUiEvent
    data object ClearError : AdminSubmissionAuditUiEvent
    data object ClearSuccessMessage : AdminSubmissionAuditUiEvent
}
