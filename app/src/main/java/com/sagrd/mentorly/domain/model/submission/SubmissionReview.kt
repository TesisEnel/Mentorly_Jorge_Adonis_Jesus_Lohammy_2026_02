package com.sagrd.mentorly.domain.model.submission

data class SubmissionReview(
    val id: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val reviewedAt: String
)
