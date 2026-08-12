package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.AnalyticsOverview

data class AnalyticsOverviewDto(
    val totalStudents: Int,
    val totalCourses: Int,
    val totalEnrollments: Int,
    val activeStudents: Int,
    val completionRate: Double
) {
    fun toDomain() = AnalyticsOverview(
        totalStudents = totalStudents,
        totalCourses = totalCourses,
        totalEnrollments = totalEnrollments,
        activeStudents = activeStudents,
        completionRate = completionRate
    )
}
