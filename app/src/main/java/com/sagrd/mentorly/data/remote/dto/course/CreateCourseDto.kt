package com.sagrd.mentorly.data.remote.dto.course

data class CreateCourseDto(
    val title: String,
    val description: String,
    val requiredPeerReviews: Int,
    val imageUrl: String?
)