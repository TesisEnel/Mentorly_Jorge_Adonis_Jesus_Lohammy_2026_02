package com.sagrd.mentorly.data.remote.dto.community

import com.sagrd.mentorly.domain.model.community.CourseMember

data class CourseMemberDto(
    val studentId: String,
    val displayName: String,
    val totalPoints: Int
) {
    fun toDomain() = CourseMember(
        studentId = studentId,
        displayName = displayName,
        totalPoints = totalPoints
    )
}
