package com.sagrd.mentorly.domain.model.analytics

data class UnitCompletionTime(
    val unitId: String,
    val unitTitle: String,
    val averageDays: Double
)
