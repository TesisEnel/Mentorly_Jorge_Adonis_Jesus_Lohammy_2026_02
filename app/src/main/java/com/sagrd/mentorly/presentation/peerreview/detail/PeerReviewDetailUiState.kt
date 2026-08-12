package com.sagrd.mentorly.presentation.peerreview.detail

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission

data class PeerReviewDetailUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submission: AnonymousSubmission? = null,
    val isApproved: Boolean? = null,
    val feedbackComment: String = "",
    val decisionError: String? = null,
    val feedbackError: String? = null,
    val errorMessage: String? = null,
    val result: PeerReviewResult? = null,
    val hasSession: Boolean = true
)
