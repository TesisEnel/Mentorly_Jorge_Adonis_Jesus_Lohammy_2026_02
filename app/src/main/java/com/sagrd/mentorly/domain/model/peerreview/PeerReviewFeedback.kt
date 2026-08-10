package com.sagrd.mentorly.domain.model.peerreview

data class PeerReviewFeedback(
    val peerReviewId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String
)
