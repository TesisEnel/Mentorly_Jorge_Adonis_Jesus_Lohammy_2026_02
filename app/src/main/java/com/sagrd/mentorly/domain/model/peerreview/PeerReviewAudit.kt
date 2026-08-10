package com.sagrd.mentorly.domain.model.peerreview

data class PeerReviewAudit(
    val peerReviewId: String,
    val submissionId: String,
    val authorStudentId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String,
    val evidenceUrl: String
)
