package com.sagrd.mentorly.presentation.admin.submission.audit

import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit

data class AdminSubmissionAuditUiState(
    val isLoading: Boolean = false,
    val isDeciding: Boolean = false,
    val audit: AdminSubmissionAudit? = null,
    val pendingDecision: Boolean? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
)
