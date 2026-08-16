package com.sagrd.mentorly.domain.model.submission

data class AdminEscalatedSubmission(
    val submissionId: String,
    val enrollmentId: String,
    val authorStudentId: String,
    val authorDisplayName: String,
    val courseId: String,
    val courseTitle: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: EvidenceType,
    val evidenceContent: String,
    val submittedAtUtc: String,
    val escalatedAtUtc: String,
    val positiveReviews: Int,
    val rejectedReviews: Int
) {
}
