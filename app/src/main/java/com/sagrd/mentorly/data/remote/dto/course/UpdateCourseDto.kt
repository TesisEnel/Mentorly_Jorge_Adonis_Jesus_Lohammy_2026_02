package com.sagrd.mentorly.data.remote.dto.course

data class UpdateCourseDto(
    val title: String,
    val description: String,
    val requiredPeerReviews: Int,
    val imageUrl: String?
)