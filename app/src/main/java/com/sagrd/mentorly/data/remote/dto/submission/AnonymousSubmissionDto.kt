package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission

data class AnonymousSubmissionDto(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val submittedAtUtc: String
) {
    fun toDomain() = AnonymousSubmission(
        submissionId = submissionId,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceUrl = evidenceUrl,
        submittedAtUtc = submittedAtUtc
    )
}