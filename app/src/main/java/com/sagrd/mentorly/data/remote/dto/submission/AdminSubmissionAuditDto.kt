package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus

data class AdminSubmissionAuditDto(
    val submissionId: String,
    val enrollmentId: String,
    val authorStudentId: String,
    val authorDisplayName: String,
    val authorEmail: String,
    val courseId: String,
    val courseTitle: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: Int,
    val evidenceContent: String,
    val status: Int,
    val submittedAtUtc: String,
    val reviewedAtUtc: String?,
    val peerReviews: List<AdminPeerReviewAuditItemDto>
) {
    fun toDomain() = AdminSubmissionAudit(
        submissionId = submissionId,
        enrollmentId = enrollmentId,
        authorStudentId = authorStudentId,
        authorDisplayName = authorDisplayName,
        authorEmail = authorEmail,
        courseId = courseId,
        courseTitle = courseTitle,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent,
        status = SubmissionStatus.fromApi(status),
        submittedAtUtc = submittedAtUtc,
        reviewedAtUtc = reviewedAtUtc,
        peerReviews = peerReviews.map { it.toDomain() }
    )
}
