package com.sagrd.mentorly.domain.model.analytics

data class PeerReviewBottleneck(
    val activityId: String,
    val activityTitle: String,
    val pendingSubmissions: Int,
    val escalatedSubmissions: Int,
    val oldestPendingAtUtc: String?
)
