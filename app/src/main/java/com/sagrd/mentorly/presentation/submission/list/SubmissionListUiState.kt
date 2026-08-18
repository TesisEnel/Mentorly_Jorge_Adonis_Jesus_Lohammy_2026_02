package com.sagrd.mentorly.presentation.submission.list

import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.Submission

data class SubmissionItemUiState(
    val submission: Submission,
    val courseTitle: String = "",
    val approvalStrategy: ApprovalStrategy? = null,
    val positiveReviewsCount: Int = 0,
    val requiredReviewsCount: Int = 0,
    val hasReviewsInfo: Boolean = false
)

data class SubmissionListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val submissions: List<SubmissionItemUiState> = emptyList(),
    val filteredSubmissions: List<SubmissionItemUiState> = emptyList(),
    val errorMessage: String? = null
)