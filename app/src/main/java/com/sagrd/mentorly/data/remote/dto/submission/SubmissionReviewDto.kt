package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.SubmissionReview

data class SubmissionReviewDto(
    val peerReviewId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String
) {
    fun toDomain() = SubmissionReview(
        id = peerReviewId,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        reviewedAt = createdAtUtc
    )
}

