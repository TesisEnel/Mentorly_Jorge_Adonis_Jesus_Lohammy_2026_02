package com.sagrd.mentorly.domain.model.submission

data class Submission(
    val id: String,
    val enrollmentId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val status: SubmissionStatus,
    val submittedAt: String
)
