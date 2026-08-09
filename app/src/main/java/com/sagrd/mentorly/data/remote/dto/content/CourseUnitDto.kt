package com.sagrd.mentorly.data.remote.dto.content

import com.sagrd.mentorly.domain.model.content.CourseUnit

data class CourseUnitDto(
    val id: String,
    val courseId: String? = null,
    val title: String,
    val orderIndex: Int,
    val themes: List<ThemeDto> = emptyList()
) {
    fun toDomain(parentCourseId: String) = CourseUnit (
            id = id,
            courseId = courseId ?: parentCourseId,
            title = title,
            orderIndex = orderIndex,
            themes = themes.map { theme ->
                theme.toDomain(parentUnitId = id)
            }
        )
    }

