package com.sagrd.mentorly.data.remote.dto.student

import com.sagrd.mentorly.domain.model.student.Badge

data class BadgeDto(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val grantedAt: String
) {
    fun toDomain() = Badge (
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            grantedAt = grantedAt
        )
    }