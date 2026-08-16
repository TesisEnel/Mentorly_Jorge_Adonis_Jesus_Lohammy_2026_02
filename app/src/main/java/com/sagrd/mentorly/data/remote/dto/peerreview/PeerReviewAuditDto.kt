package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class PeerReviewAuditDto(
    val peerReviewId: String,
    val submissionId: String,
    val authorStudentId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String,
    val evidenceType: Int,
    val evidenceContent: String
) {
    fun toDomain() = PeerReviewAudit(
        peerReviewId = peerReviewId,
        submissionId = submissionId,
        authorStudentId = authorStudentId,
        reviewerStudentId = reviewerStudentId,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        createdAt = createdAtUtc,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent
    )
}
