package com.sagrd.mentorly.domain.model.student

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val grantedAt: String
)