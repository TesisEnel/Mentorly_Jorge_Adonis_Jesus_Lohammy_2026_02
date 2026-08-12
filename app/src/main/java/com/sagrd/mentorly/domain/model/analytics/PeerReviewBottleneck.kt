package com.sagrd.mentorly.domain.model.analytics

data class PeerReviewBottleneck(
    val activityId: String,
    val activityTitle: String,
    val courseTitle: String,
    val pendingSubmissionsCount: Int,
    val averageWaitTimeHours: Double
)
