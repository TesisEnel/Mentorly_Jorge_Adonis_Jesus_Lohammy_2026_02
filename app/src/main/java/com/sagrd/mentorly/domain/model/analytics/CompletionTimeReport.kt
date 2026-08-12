package com.sagrd.mentorly.domain.model.analytics

data class CompletionTimeReport(
    val courseAverageDays: Double?,
    val units: List<UnitCompletionTime>
)
