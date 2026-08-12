package com.sagrd.mentorly.domain.model.analytics

data class CompletionTimeReport(
    val courseId: String,
    val courseTitle: String,
    val averageCompletionDays: Double,
    val fastestCompletionDays: Double,
    val slowestCompletionDays: Double,
    val unitCompletionTimes: List<UnitCompletionTime>
)
