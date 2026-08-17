package com.sagrd.mentorly.presentation.submission.detail

import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionReview

data class SubmissionDetailUiState(
    val isLoading: Boolean = true,
    val isEscalating: Boolean = false,
    val submission: Submission? = null,
    val reviews: List<SubmissionReview> = emptyList(),
    val requiredReviewsCount: Int = 3,
    val approvalStrategy: ApprovalStrategy? = null,
    val approvalStrategyText: String = "Cargando información...",
    val canEscalate: Boolean = false,
    val errorMessage: String? = null
)