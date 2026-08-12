package com.sagrd.mentorly.data.remote.dto.content

import com.sagrd.mentorly.domain.model.content.Theme

data class ThemeDto(
    val id: String,
    val unitId: String? = null,
    val title: String,
    val contentText: String,
    val orderIndex: Int,
    val activities: List<ActivityDto> = emptyList()
) {
    fun toDomain(parentUnitId: String) = Theme(
        id = id,
        unitId = unitId ?: parentUnitId,
        title = title,
        contentText = contentText,
        orderIndex = orderIndex,
        activities = activities.map { activity ->
            activity.toDomain(parentThemeId = id)
        }
    )
}

