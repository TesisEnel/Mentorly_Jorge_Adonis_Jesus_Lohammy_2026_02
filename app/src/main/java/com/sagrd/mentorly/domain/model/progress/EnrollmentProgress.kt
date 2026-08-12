package com.sagrd.mentorly.domain.model.progress

data class EnrollmentProgress(
    val enrollmentId: String,
    val percentage: Int,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val canSubmitNextUnit: Boolean,
    val blockedReason: String?,
    val units: List<EnrollmentUnitProgress>
)
