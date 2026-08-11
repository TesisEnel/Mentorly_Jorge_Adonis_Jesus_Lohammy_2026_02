package com.sagrd.mentorly.domain.model.progress

data class EnrollmentProgress(
    val enrollmentId: String,
    val percentage: Int,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val units: List<EnrollmentUnitProgress>
)
