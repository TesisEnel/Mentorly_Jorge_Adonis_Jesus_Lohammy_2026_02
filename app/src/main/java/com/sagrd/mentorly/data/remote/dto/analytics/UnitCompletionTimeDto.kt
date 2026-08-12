package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.UnitCompletionTime

data class UnitCompletionTimeDto(
    val unitId: String,
    val unitTitle: String,
    val averageDays: Double?
) {
    fun toDomain() = UnitCompletionTime(
        unitId = unitId,
        unitTitle = unitTitle,
        averageDays = averageDays
    )
}
