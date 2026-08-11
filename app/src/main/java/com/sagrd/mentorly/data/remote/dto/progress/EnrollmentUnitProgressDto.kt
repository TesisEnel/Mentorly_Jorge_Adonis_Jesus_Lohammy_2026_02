package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentUnitProgress

data class EnrollmentUnitProgressDto(
    val unitId: String,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val canSubmitNextUnit: Boolean,
    val blockedReason: String?,
    val activities: List<EnrollmentActivityProgressDto>
) {
    fun toDomain() = EnrollmentUnitProgress(
        unitId = unitId,
        completedThemes = completedThemes,
        totalThemes = totalThemes,
        approvedMandatoryActivities = approvedMandatoryActivities,
        totalMandatoryActivities = totalMandatoryActivities,
        canSubmitNextUnit = canSubmitNextUnit,
        blockedReason = blockedReason,
        activities = activities.map { it.toDomain() }
    )
}
