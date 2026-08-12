package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress

data class EnrollmentThemeProgressDto(
    val themeId: String,
    val title: String,
    val contentText: String,
    val orderIndex: Int,
    val isCompleted: Boolean,
    val activities: List<EnrollmentActivityProgressDto>
) {
    fun toDomain() = EnrollmentThemeProgress(
        themeId = themeId,
        title = title,
        contentText = contentText,
        orderIndex = orderIndex,
        isCompleted = isCompleted,
        activities = activities.map { it.toDomain() }
    )
}
