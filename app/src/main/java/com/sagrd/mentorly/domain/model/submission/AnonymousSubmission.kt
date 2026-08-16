package com.sagrd.mentorly.domain.model.submission

data class AnonymousSubmission(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: EvidenceType,
    val evidenceContent: String,
    val submittedAtUtc: String
) {
}
