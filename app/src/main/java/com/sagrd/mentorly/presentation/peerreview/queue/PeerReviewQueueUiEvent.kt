package com.sagrd.mentorly.presentation.peerreview.queue

sealed interface PeerReviewQueueUiEvent {
    data object Refresh : PeerReviewQueueUiEvent
    data object ClearError : PeerReviewQueueUiEvent
}
