package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class AdminEscalatedSubmissionDto(
    val submissionId: String,
    val enrollmentId: String,
    val authorStudentId: String,
    val authorDisplayName: String,
    val courseId: String,
    val courseTitle: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: Int,
    val evidenceContent: String,
    val submittedAtUtc: String,
    val escalatedAtUtc: String,
    val positiveReviews: Int,
    val rejectedReviews: Int
) {
    fun toDomain() = AdminEscalatedSubmission(
        submissionId = submissionId,
        enrollmentId = enrollmentId,
        authorStudentId = authorStudentId,
        authorDisplayName = authorDisplayName,
        courseId = courseId,
        courseTitle = courseTitle,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent,
        submittedAtUtc = submittedAtUtc,
        escalatedAtUtc = escalatedAtUtc,
        positiveReviews = positiveReviews,
        rejectedReviews = rejectedReviews
    )
}
