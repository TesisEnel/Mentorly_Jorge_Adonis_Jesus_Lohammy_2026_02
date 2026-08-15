package com.sagrd.mentorly.presentation.admin.peerreview.list

import com.sagrd.mentorly.domain.model.peerreview.PeerReview

enum class PeerReviewFilter {
    All,
    Approved,
    Rejected
}

data class AdminPeerReviewListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val peerReviews: List<PeerReview> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: PeerReviewFilter = PeerReviewFilter.All,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true
)
