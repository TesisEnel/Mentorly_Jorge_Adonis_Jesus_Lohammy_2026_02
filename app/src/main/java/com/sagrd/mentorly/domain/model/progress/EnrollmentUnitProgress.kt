package com.sagrd.mentorly.domain.model.progress

data class EnrollmentUnitProgress(
    val unitId: String,
    val title: String,
    val completedThemes: Int,
    val totalThemes: Int,
    val approvedMandatoryActivities: Int,
    val totalMandatoryActivities: Int,
    val themes: List<EnrollmentThemeProgress>
)
