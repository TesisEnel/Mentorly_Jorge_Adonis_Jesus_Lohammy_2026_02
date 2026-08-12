package com.sagrd.mentorly.domain.model.analytics

data class AnalyticsOverview(
    val totalStudents: Int,
    val totalCourses: Int,
    val totalEnrollments: Int,
    val activeStudents: Int,
    val completionRate: Double
)
