package com.sagrd.mentorly.presentation.admin.peerreview.audit

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit

data class PeerReviewAuditUiState(
    val isLoading: Boolean = false,
    val audit: PeerReviewAudit? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true
)
