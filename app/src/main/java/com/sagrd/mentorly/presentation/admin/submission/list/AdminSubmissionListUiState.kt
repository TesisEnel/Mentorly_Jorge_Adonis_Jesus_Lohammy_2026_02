package com.sagrd.mentorly.presentation.admin.submission.list

import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission

enum class EscalatedSubmissionFilter {
    All,
    MostApproved,
    MostRejected
}

data class AdminSubmissionListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val submissions: List<AdminEscalatedSubmission> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: EscalatedSubmissionFilter = EscalatedSubmissionFilter.All,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
)
