package com.sagrd.mentorly.presentation.admin.peerreview.list

sealed interface AdminPeerReviewListUiEvent {
    data object Load : AdminPeerReviewListUiEvent
    data object Refresh : AdminPeerReviewListUiEvent
    data class SearchChanged(val value: String) : AdminPeerReviewListUiEvent
    data class FilterChanged(val filter: PeerReviewFilter) : AdminPeerReviewListUiEvent
    data object ClearError : AdminPeerReviewListUiEvent
}
