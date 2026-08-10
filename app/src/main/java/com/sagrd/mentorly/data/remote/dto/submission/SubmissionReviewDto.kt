package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.SubmissionReview

data class SubmissionReviewDto(
    val id: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val reviewedAt: String
) {
    fun toDomain() = SubmissionReview(
        id = id,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        reviewedAt = reviewedAt
    )
}
