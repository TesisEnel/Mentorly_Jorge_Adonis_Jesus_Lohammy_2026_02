package com.sagrd.mentorly.domain.model.content

data class CourseUnit(
    val id: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int,
    val themes: List<Theme> = emptyList()
)
