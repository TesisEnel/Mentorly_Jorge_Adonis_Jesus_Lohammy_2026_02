package com.sagrd.mentorly.domain.model.peerreview

import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class PeerReviewAudit(
    val peerReviewId: String,
    val submissionId: String,
    val authorStudentId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String,
    val evidenceType: EvidenceType,
    val evidenceContent: String
) {
}
