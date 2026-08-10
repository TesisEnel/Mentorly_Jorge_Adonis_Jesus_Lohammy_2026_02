package com.sagrd.mentorly.domain.model.peerreview

data class ReviewQueueItem(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val submittedAtUtc: String
)