package com.sagrd.mentorly.domain.model.analytics

data class DropOff(
    val courseId: String,
    val courseTitle: String,
    val unitId: String,
    val unitTitle: String,
    val studentsStarted: Int,
    val studentsDropped: Int,
    val dropOffPercentage: Double
)
