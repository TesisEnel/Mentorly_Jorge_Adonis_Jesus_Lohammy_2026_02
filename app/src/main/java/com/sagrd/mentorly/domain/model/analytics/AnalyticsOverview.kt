package com.sagrd.mentorly.domain.model.analytics

data class AnalyticsOverview(
    val courses: Int,
    val activeEnrollments: Int,
    val completedEnrollments: Int,
    val expiredEnrollments: Int,
    val pendingPeerReviewSubmissions: Int
)
