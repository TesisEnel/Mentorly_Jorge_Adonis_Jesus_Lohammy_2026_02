package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.CompletionTimeReport

data class CompletionTimeReportDto(
    val courseAverageDays: Double?,
    val units: List<UnitCompletionTimeDto>
) {
    fun toDomain() = CompletionTimeReport(
        courseAverageDays = courseAverageDays,
        units = units.map { it.toDomain() }
    )
}
