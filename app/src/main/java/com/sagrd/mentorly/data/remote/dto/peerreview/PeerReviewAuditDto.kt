package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class PeerReviewAuditDto(
    val peerReviewId: String,
    val submissionId: String,
    val authorStudentId: String,
    val authorDisplayName: String = "",
    val authorEmail: String = "",
    val reviewerStudentId: String,
    val reviewerDisplayName: String = "",
    val reviewerEmail: String = "",
    val activityId: String = "",
    val activityTitle: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val isApproved: Boolean,
    val feedbackComment: String,
    val criterionScores: List<PeerReviewCriterionScoreDto>,
    val createdAtUtc: String,
    val evidenceType: Int,
    val evidenceContent: String
) {
    fun toDomain() = PeerReviewAudit(
        peerReviewId = peerReviewId,
        submissionId = submissionId,
        authorStudentId = authorStudentId,
        authorDisplayName = authorDisplayName,
        authorEmail = authorEmail,
        reviewerStudentId = reviewerStudentId,
        reviewerDisplayName = reviewerDisplayName,
        reviewerEmail = reviewerEmail,
        activityId = activityId,
        activityTitle = activityTitle,
        courseId = courseId,
        courseTitle = courseTitle,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        criterionScores = criterionScores.map { it.toDomain() },
        createdAt = createdAtUtc,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent
    )
}
