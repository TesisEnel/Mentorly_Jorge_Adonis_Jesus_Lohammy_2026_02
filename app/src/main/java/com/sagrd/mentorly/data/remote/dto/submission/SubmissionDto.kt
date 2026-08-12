package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus

data class SubmissionDto(
    val id: String,
    val enrollmentId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val status: Int,
    val submittedAt: String
) {
    fun toDomain() = Submission(
        id = id,
        enrollmentId = enrollmentId,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceUrl = evidenceUrl,
        status = SubmissionStatus.fromApi(status),
        submittedAt = submittedAt
    )
}
