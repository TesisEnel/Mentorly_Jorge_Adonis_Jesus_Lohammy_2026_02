package com.sagrd.mentorly.domain.model.peerreview

import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class PeerReviewAudit(
    val peerReviewId: String,
    val submissionId: String,
    val authorStudentId: String,
    val authorDisplayName: String,
    val authorEmail: String,
    val reviewerStudentId: String,
    val reviewerDisplayName: String,
    val reviewerEmail: String,
    val courseTitle: String,
    val activityTitle: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val criterionScores: List<PeerReviewCriterionScore>,
    val createdAt: String,
    val evidenceType: EvidenceType,
    val evidenceContent: String
)
