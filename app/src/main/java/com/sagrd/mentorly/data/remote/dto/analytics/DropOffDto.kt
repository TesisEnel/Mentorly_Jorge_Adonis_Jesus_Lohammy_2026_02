package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.DropOff

data class DropOffDto(
    val courseId: String,
    val courseTitle: String,
    val unitId: String,
    val unitTitle: String,
    val studentsStarted: Int,
    val studentsDropped: Int,
    val dropOffPercentage: Double
) {
    fun toDomain() = DropOff(
        courseId = courseId,
        courseTitle = courseTitle,
        unitId = unitId,
        unitTitle = unitTitle,
        studentsStarted = studentsStarted,
        studentsDropped = studentsDropped,
        dropOffPercentage = dropOffPercentage
    )
}
