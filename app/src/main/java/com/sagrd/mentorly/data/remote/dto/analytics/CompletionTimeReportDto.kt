package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.CompletionTimeReport

data class CompletionTimeReportDto(
    val courseId: String,
    val courseTitle: String,
    val averageCompletionDays: Double,
    val fastestCompletionDays: Double,
    val slowestCompletionDays: Double,
    val unitCompletionTimes: List<UnitCompletionTimeDto>
) {
    fun toDomain() = CompletionTimeReport(
        courseId = courseId,
        courseTitle = courseTitle,
        averageCompletionDays = averageCompletionDays,
        fastestCompletionDays = fastestCompletionDays,
        slowestCompletionDays = slowestCompletionDays,
        unitCompletionTimes = unitCompletionTimes.map { it.toDomain() }
    )
}
