package com.sagrd.mentorly.presentation.peerreview.history

sealed interface PeerReviewHistoryUiEvent {
    data object Refresh : PeerReviewHistoryUiEvent
    data object ClearError : PeerReviewHistoryUiEvent
}
