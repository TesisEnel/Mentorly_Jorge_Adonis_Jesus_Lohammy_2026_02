package com.sagrd.mentorly.domain.model.course

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val isPublished: Boolean,
    val requiredPeerReviews: Int
)
