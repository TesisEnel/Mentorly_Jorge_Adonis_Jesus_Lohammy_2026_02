package com.sagrd.mentorly.presentation.admin.peerreview.audit

sealed interface PeerReviewAuditUiEvent {
    data object Load : PeerReviewAuditUiEvent
    data object Retry : PeerReviewAuditUiEvent
    data object ClearError : PeerReviewAuditUiEvent
}
