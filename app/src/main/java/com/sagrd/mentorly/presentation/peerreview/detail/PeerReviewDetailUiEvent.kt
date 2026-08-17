package com.sagrd.mentorly.presentation.peerreview.detail

sealed interface PeerReviewDetailUiEvent {
    data class CriterionScoreChanged(val criterionId: String, val score: Int) : PeerReviewDetailUiEvent
    data class DecisionChanged(val isApproved: Boolean) : PeerReviewDetailUiEvent
    data class FeedbackChanged(val value: String) : PeerReviewDetailUiEvent
    data object Submit : PeerReviewDetailUiEvent
    data object Retry : PeerReviewDetailUiEvent
    data object ClearError : PeerReviewDetailUiEvent
}
