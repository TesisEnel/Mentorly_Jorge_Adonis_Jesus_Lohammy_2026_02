package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.AnalyticsOverview

data class AnalyticsOverviewDto(
    val courses: Int,
    val activeEnrollments: Int,
    val completedEnrollments: Int,
    val expiredEnrollments: Int,
    val pendingPeerReviewSubmissions: Int
) {
    fun toDomain() = AnalyticsOverview(
        courses = courses,
        activeEnrollments = activeEnrollments,
        completedEnrollments = completedEnrollments,
        expiredEnrollments = expiredEnrollments,
        pendingPeerReviewSubmissions = pendingPeerReviewSubmissions
    )
}
