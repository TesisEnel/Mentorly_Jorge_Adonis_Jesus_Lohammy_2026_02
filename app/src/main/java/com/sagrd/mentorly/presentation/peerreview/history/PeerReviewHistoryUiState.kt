package com.sagrd.mentorly.presentation.peerreview.history

import com.sagrd.mentorly.domain.model.peerreview.PeerReview

data class PeerReviewHistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val reviews: List<PeerReview> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
