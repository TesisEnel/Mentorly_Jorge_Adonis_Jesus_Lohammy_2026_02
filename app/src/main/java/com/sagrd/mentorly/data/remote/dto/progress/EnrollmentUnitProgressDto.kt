package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentUnitProgress

data class EnrollmentUnitProgressDto(
    val unitId: String,
    val title: String,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val activities: List<EnrollmentActivityProgressDto>
) {
    fun toDomain() = EnrollmentUnitProgress(
        unitId = unitId,
        title = title,
        completedThemes = completedThemes,
        totalThemes = totalThemes,
        approvedMandatoryActivities = approvedMandatoryActivities,
        totalMandatoryActivities = totalMandatoryActivities,
        activities = activities.map { it.toDomain() }
    )
}
