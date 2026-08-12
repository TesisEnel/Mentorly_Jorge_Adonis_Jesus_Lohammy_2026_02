package com.sagrd.mentorly.domain.model.course

import com.sagrd.mentorly.domain.model.content.CourseUnit

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val isPublished: Boolean,
    val requiredPeerReviews: Int,
    val units: List<CourseUnit> = emptyList()
)
