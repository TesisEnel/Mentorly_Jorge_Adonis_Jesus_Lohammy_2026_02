package com.sagrd.mentorly.domain.model.peerreview

data class PeerReview(
    val id: String,
    val submissionId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String
)
