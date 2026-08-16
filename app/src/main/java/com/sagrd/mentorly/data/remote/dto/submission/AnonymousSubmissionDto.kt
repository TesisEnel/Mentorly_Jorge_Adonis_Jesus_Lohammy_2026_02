package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class AnonymousSubmissionDto(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: Int,
    val evidenceContent: String,
    val submittedAtUtc: String
) {
    fun toDomain() = AnonymousSubmission(
        submissionId = submissionId,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent,
        submittedAtUtc = submittedAtUtc
    )
}
