package com.sagrd.mentorly.domain.model.submission

data class AdminPeerReviewAuditItem(
    val peerReviewId: String,
    val reviewerStudentId: String,
    val reviewerDisplayName: String,
    val reviewerEmail: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String
)
