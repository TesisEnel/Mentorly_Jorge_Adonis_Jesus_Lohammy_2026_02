package com.sagrd.mentorly.domain.model.submission

data class AdminSubmissionAudit(
    val submissionId: String,
    val enrollmentId: String,
    val authorStudentId: String,
    val authorDisplayName: String,
    val authorEmail: String,
    val courseId: String,
    val courseTitle: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val status: SubmissionStatus,
    val submittedAtUtc: String,
    val reviewedAtUtc: String?,
    val peerReviews: List<AdminPeerReviewAuditItem>
)
