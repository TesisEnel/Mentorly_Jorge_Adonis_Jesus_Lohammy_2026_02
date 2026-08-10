package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReview

data class PeerReviewDto(
    val id: String,
    val submissionId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String
) {
    fun toDomain() = PeerReview(
        id = id,
        submissionId = submissionId,
        reviewerStudentId = reviewerStudentId,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        createdAt = createdAt
    )
}
