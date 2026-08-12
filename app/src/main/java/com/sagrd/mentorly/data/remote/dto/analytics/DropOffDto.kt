package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.DropOff

data class DropOffDto(
    val unitId: String,
    val unitTitle: String,
    val themeId: String,
    val themeTitle: String,
    val enrollmentCount: Int,
    val completionCount: Int,
    val completionRate: Double
) {
    fun toDomain() = DropOff(
        unitId = unitId,
        unitTitle = unitTitle,
        themeId = themeId,
        themeTitle = themeTitle,
        enrollmentCount = enrollmentCount,
        completionCount = completionCount,
        completionRate = completionRate
    )
}
