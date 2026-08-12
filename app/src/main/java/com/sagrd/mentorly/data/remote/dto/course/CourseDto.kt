package com.sagrd.mentorly.data.remote.dto.course

import com.sagrd.mentorly.data.remote.dto.content.CourseUnitDto
import com.sagrd.mentorly.domain.model.course.Course

data class CourseDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val isPublished: Boolean,
    val requiredPeerReviews: Int,
    val units: List<CourseUnitDto> = emptyList()
) {
    fun toDomain() = Course(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        isPublished = isPublished,
        requiredPeerReviews = requiredPeerReviews,
        units = units.map { unit ->
            unit.toDomain(parentCourseId = id)
        }
    )
}
