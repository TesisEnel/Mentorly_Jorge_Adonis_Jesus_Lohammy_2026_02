package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress

data class EnrollmentProgressDto(
    val enrollmentId: String,
    val percentage: Int,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val canSubmitNextUnit: Boolean,
    val blockedReason: String?,
    val units: List<EnrollmentUnitProgressDto>
) {
    fun toDomain() = EnrollmentProgress(
        enrollmentId = enrollmentId,
        percentage = percentage,
        completedThemes = completedThemes,
        totalThemes = totalThemes,
        approvedMandatoryActivities = approvedMandatoryActivities,
        totalMandatoryActivities = totalMandatoryActivities,
        canSubmitNextUnit = canSubmitNextUnit,
        blockedReason = blockedReason,
        units = units.map { it.toDomain() }
    )
}
