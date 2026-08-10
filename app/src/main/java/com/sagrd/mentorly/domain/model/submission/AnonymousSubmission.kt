package com.sagrd.mentorly.domain.model.submission

data class AnonymousSubmission(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val submittedAtUtc: String
)