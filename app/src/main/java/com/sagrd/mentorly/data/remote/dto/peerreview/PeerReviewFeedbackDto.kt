package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewFeedback

data class PeerReviewFeedbackDto(
    val peerReviewId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String
) {
    fun toDomain() = PeerReviewFeedback(
        peerReviewId = peerReviewId,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        createdAt = createdAtUtc
    )
}
