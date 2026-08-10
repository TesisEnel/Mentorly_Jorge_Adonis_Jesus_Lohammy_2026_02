package com.sagrd.mentorly.data.remote.dto.peerreview

data class UpdatePeerReviewDto(
    val isApproved: Boolean,
    val feedbackComment: String
)