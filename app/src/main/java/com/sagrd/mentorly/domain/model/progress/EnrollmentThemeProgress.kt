package com.sagrd.mentorly.domain.model.progress

data class EnrollmentThemeProgress(
    val themeId: String,
    val title: String,
    val contentText: String,
    val orderIndex: Int,
    val isCompleted: Boolean,
    val activities: List<EnrollmentActivityProgress>
)
