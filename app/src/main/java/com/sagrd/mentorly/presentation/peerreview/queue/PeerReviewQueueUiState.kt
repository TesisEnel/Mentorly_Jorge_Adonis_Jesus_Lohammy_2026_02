package com.sagrd.mentorly.presentation.peerreview.queue

import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem

data class PeerReviewQueueUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val queueItems: List<ReviewQueueItem> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
