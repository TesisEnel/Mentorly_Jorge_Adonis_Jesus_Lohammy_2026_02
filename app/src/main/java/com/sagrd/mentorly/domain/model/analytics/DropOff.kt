package com.sagrd.mentorly.domain.model.analytics

data class DropOff(
    val unitId: String,
    val unitTitle: String,
    val themeId: String,
    val themeTitle: String,
    val enrollmentCount: Int,
    val completionCount: Int,
    val completionRate: Double
)
