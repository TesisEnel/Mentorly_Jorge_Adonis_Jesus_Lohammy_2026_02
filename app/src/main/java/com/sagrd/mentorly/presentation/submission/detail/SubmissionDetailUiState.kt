package com.sagrd.mentorly.presentation.submission.detail

import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionReview

data class SubmissionDetailUiState(
    val isLoading: Boolean = true,
    val isEscalating: Boolean = false,
    val submission: Submission? = null,
    val reviews: List<SubmissionReview> = emptyList(),
    val errorMessage: String? = null
)